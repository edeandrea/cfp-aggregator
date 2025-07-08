package com.redhat.cfpaggregator.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import com.redhat.cfpaggregator.client.ClientProducer;
import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;
import com.redhat.cfpaggregator.mapping.EventMapper;
import com.redhat.cfpaggregator.mapping.PortalMapper;
import com.redhat.cfpaggregator.mapping.SpeakerMapper;
import com.redhat.cfpaggregator.mapping.TalkMapper;
import com.redhat.cfpaggregator.mapping.TalkSearchCriteriaMapper;
import com.redhat.cfpaggregator.repository.EventRepository;
import com.redhat.cfpaggregator.repository.PortalRepository;
import com.redhat.cfpaggregator.ui.views.EventViews.EventName;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@ApplicationScoped
@Transactional
public class CfpService {
  private final ClientProducer clientProducer;
  private final CfpPortalsConfig config;
  private final EventMapper eventMapper;
  private final SpeakerMapper speakerMapper;
  private final TalkMapper talkMapper;
  private final PortalMapper portalMapper;
  private final TalkSearchCriteriaMapper talkSearchCriteriaMapper;
  private final PortalRepository portalRepository;
  private final EventRepository eventRepository;

  public CfpService(
      ClientProducer clientProducer,
      CfpPortalsConfig config,
      EventMapper eventMapper,
      SpeakerMapper speakerMapper,
      TalkMapper talkMapper, PortalMapper portalMapper,
      TalkSearchCriteriaMapper talkSearchCriteriaMapper,
      PortalRepository portalRepository,
      EventRepository eventRepository) {

    this.clientProducer = clientProducer;
    this.config = config;
    this.eventMapper = eventMapper;
    this.speakerMapper = speakerMapper;
    this.talkMapper = talkMapper;
    this.portalMapper = portalMapper;
    this.talkSearchCriteriaMapper = talkSearchCriteriaMapper;
    this.portalRepository = portalRepository;
    this.eventRepository = eventRepository;
  }

  void onStartup(@Observes StartupEvent startupEvent) {
    if (this.config.reloadOnStartup()) {
      Log.debug("Reloading events on startup");
      this.portalRepository.deleteAllWithCascade();

      createPortals();
      createEvents(this.talkSearchCriteriaMapper.fromConfig(this.config.defaultSearchCriteria()));
    }
  }

  public void createPortals() {
    Log.debugf("Creating portals: %s", this.config.portals());

    this.config.portals()
        .entrySet()
        .stream()
        .map(entry -> this.portalMapper.fromConfig(entry.getKey(), entry.getValue()))
        .forEach(this.portalRepository::persistAndFlush);
  }

  /**
   * Fetches events based on the specified search criteria. The method utilizes
   * the provided {@code TalkSearchCriteria} to filter and retrieve events that
   * match the given conditions such as talk keywords and speaker companies.
   *
   * @param searchCriteria the criteria used to search for events, including
   *                       filters for talk keywords and speaker companies
   */
  public void createEvents(TalkSearchCriteria searchCriteria) {
    Log.debugf("Creating events with search criteria: %s", searchCriteria);

    // Let's parallelize this
    var portals = this.portalRepository.listAll();
    var unis = portals.stream()
        .map(portal ->
            Uni.createFrom()
                .item(() -> createEvent(portal, searchCriteria))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        )
        .toList();

    Uni.join()
        .all(unis)
        .andFailFast()
        .await().atMost(this.config.timeout().multipliedBy(portals.size()));

    this.portalRepository.persist(portals);
    Log.info("Successfully created events");
  }

  private Event createEvent(Portal portal, TalkSearchCriteria searchCriteria) {
    // The object model from cfp.dev is a bit backwards
    // You search for talks, which have references to speakers
    // We are flipping the model
    var portalName = portal.getPortalName();
    Log.debugf("Creating event for portal %s", portalName);

    var client = this.clientProducer.getCfpDevClient(portal);
    var eventDetails = client.getEventDetails(portalName);
    var event = this.eventMapper.fromCfpDev(portalName, eventDetails);

    if (eventDetails != null) {
      var talks = client.findTalks(searchCriteria, portalName);

      if (talks != null) {
        var uniqueSpeakers = talks.stream()
            .flatMap(talk -> talk.speakers().stream())
            .distinct()
            .map(this.speakerMapper::fromCfpDev)
            .collect(Collectors.toMap(Speaker::getEventSpeakerId, Function.identity()));

        talks.forEach(talk -> {
          var speakers = talk.speakers();

          if (speakers != null) {
            var mappedTalk = this.talkMapper.fromCfpDev(talk);
            speakers.stream()
                .map(speaker -> uniqueSpeakers.get(speaker.eventSpeakerId()))
                .filter(Objects::nonNull)
                .forEach(speaker -> {
                  event.addSpeakers(speaker);
                  speaker.addTalks(mappedTalk);
                });
          }
        });
      }
    }

    portal.setEvent(event);

    return event;
  }

  public List<EventName> getEvents() {
    return this.eventRepository.findAll()
        .project(EventName.class)
        .list();
  }

  public Event getFullyPopulatedEvent(String portalName) {
    var event = this.eventRepository.findById(portalName);

    // Need to trigger lazy initialization
    event.getTalkCount();

    return event;
  }
}
