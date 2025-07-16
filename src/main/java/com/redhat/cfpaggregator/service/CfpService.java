package com.redhat.cfpaggregator.service;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import com.redhat.cfpaggregator.client.ClientManager;
import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;
import com.redhat.cfpaggregator.mapping.EventMapper;
import com.redhat.cfpaggregator.mapping.PortalMapper;
import com.redhat.cfpaggregator.mapping.SpeakerMapper;
import com.redhat.cfpaggregator.mapping.TalkMapper;
import com.redhat.cfpaggregator.mapping.TalkSearchCriteriaMapper;
import com.redhat.cfpaggregator.repository.EventRepository;
import com.redhat.cfpaggregator.repository.PortalRepository;

@ApplicationScoped
@Transactional
public class CfpService {
  private final ClientManager clientManager;
  private final CfpPortalsConfig config;
  private final PortalMapper portalMapper;
  private final TalkSearchCriteriaMapper talkSearchCriteriaMapper;
  private final PortalRepository portalRepository;
  private final EventRepository eventRepository;

  public CfpService(
      ClientManager clientManager,
      CfpPortalsConfig config,
      EventMapper eventMapper,
      SpeakerMapper speakerMapper,
      TalkMapper talkMapper, PortalMapper portalMapper,
      TalkSearchCriteriaMapper talkSearchCriteriaMapper,
      PortalRepository portalRepository,
      EventRepository eventRepository) {

    this.clientManager = clientManager;
    this.config = config;
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

  public void deletePortal(String portalName) {
    Log.debugf("Deleting portal %s", portalName);
    Optional.ofNullable(portalName)
        .map(String::strip)
        .filter(name -> !name.isBlank())
        .ifPresent(name -> {
          this.portalRepository.deleteById(name);
          this.portalRepository.flush();
        });
  }

  public void deletePortal(Portal portal) {
    Optional.ofNullable(portal)
        .map(Portal::getPortalName)
        .ifPresent(this::deletePortal);
  }

  public Optional<Portal> getPortal(String portalName) {
    return this.portalRepository.findByIdOptional(portalName);
  }

  public Portal savePortal(Portal updatedPortal) {
    Log.debugf("Updating portal %s", updatedPortal.getPortalName());
    var portal = this.portalRepository.updatePortal(updatedPortal);
    this.clientManager.clearClient(portal);

    return portal;
  }

  public Portal createPortal(Portal portal) {
    Log.debugf("Creating portal %s", portal.getPortalName());
    this.portalRepository.persist(portal);
    return portal;
  }

  public List<Portal> getPortals() {
    return this.portalRepository.listAll();
  }

  public void createPortals() {
    Log.debugf("Creating portals: %s", this.config.portals());

    this.config.portals()
        .entrySet()
        .stream()
        .map(entry -> this.portalMapper.fromConfig(entry.getKey(), entry.getValue()))
        .forEach(this.portalRepository::persistAndFlush);
  }

  public void recreateEvents(TalkSearchCriteria searchCriteria) {
    Log.debugf("Recreating events with search criteria: %s", searchCriteria);
    this.eventRepository.deleteAllWithCascade();
    createEvents(searchCriteria);
    Log.debug("Successfully recreated events");
  }

  public boolean doesPortalNameExist(String portalName) {
    return this.portalRepository.findByIdOptional(portalName).isPresent();
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
    var portals = getPortals();
    var unis = portals.stream()
        .map(portal ->
            Uni.createFrom()
                .item(() -> this.clientManager.createEvent(portal, searchCriteria))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        )
        .toList();

    Uni.join()
        .all(unis)
        .andCollectFailures()
        .onFailure().invoke(t -> Log.errorf(t, "Failed to create events"))
//        .andFailFast()
        .await().atMost(this.config.timeout().multipliedBy(portals.size()));

    this.portalRepository.persist(portals);
    Log.info("Successfully created events");
  }

  public List<Event> getEvents() {
    return this.eventRepository.listAll();
  }

  public List<Event> getFullyPopulatedEvents() {
    var events = this.eventRepository.listAll();

    // Need to trigger lazy initialization
    events.forEach(Event::getTalkCount);

    return events;
  }
}
