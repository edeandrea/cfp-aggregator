package com.redhat.cfpaggregator.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import io.quarkus.runtime.StartupEvent;

import com.redhat.cfpaggregator.client.ClientProducer;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevClient;
import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.config.CfpPortalsConfig.CfpPortalConfig;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;
import com.redhat.cfpaggregator.mapping.EventMapper;
import com.redhat.cfpaggregator.mapping.SpeakerMapper;
import com.redhat.cfpaggregator.mapping.TalkMapper;
import com.redhat.cfpaggregator.mapping.TalkSearchCriteriaMapper;
import com.redhat.cfpaggregator.repository.EventRepository;
import com.redhat.cfpaggregator.ui.views.EventViews.EventName;
import io.smallrye.common.annotation.Identifier;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@ApplicationScoped
@Transactional
public class CfpService {
  private final Map<String, CfpDevClient> cfpDevClients;
  private final CfpPortalsConfig config;
  private final EventMapper eventMapper;
  private final SpeakerMapper speakerMapper;
  private final TalkMapper talkMapper;
  private final TalkSearchCriteriaMapper talkSearchCriteriaMapper;
  private final EventRepository eventRepository;

  public CfpService(
      @Identifier(ClientProducer.CFP_DEV_CLIENTS) Map<String, CfpDevClient> cfpDevClients,
      CfpPortalsConfig config,
      EventMapper eventMapper,
      SpeakerMapper speakerMapper,
      TalkMapper talkMapper,
      TalkSearchCriteriaMapper talkSearchCriteriaMapper,
      EventRepository eventRepository) {
    this.cfpDevClients = cfpDevClients;
    this.config = config;
    this.eventMapper = eventMapper;
    this.speakerMapper = speakerMapper;
    this.talkMapper = talkMapper;
    this.talkSearchCriteriaMapper = talkSearchCriteriaMapper;
    this.eventRepository = eventRepository;
  }

  void onStartup(@Observes StartupEvent startupEvent) {
    if (this.config.reloadOnStartup()) {
      Log.debug("Reloading events on startup");
      this.eventRepository.deleteAllWithCascade();
      createEvents(this.talkSearchCriteriaMapper.fromConfig(this.config.defaultSearchCriteria()));
    }
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
    var unis = this.cfpDevClients.entrySet()
        .stream()
        .map(entry -> createEvent(entry.getKey(), this.config.portals().get(entry.getKey()), entry.getValue(), searchCriteria))
        .map(Uni.createFrom()::item)
        .toList();

    Uni.join()
        .all(unis)
        .andFailFast()
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .invoke(() -> Log.info("Successfully created events"))
        .await().atMost(this.config.timeout().multipliedBy(this.config.portalNames().size()));
  }

  private Event createEvent(String portalName, CfpPortalConfig portalConfig, CfpDevClient client, TalkSearchCriteria searchCriteria) {
    // The object model from cfp.dev is a bit backwards
    // You search for talks, which have references to speakers
    // We are flipping the model
    var eventDetails = client.getEventDetails();
    var event = this.eventMapper.fromCfpDev(portalName, portalConfig.portalType(), eventDetails);

    if (eventDetails != null) {
      var talks = client.findTalks(searchCriteria);

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

    this.eventRepository.persist(event);
    Log.debugf("Persisted event:\n%s", event);

    return event;
  }

  public List<EventName> getEventNamesOrderedByMostRecent() {
    return this.eventRepository.findAll(Sort.by("toDate", Direction.Descending))
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
