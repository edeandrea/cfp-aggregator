package com.redhat.cfpaggregator.repository;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.logging.Log;

import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.Talk;

/**
 * Repository class for managing {@link Event} entities.
 *
 * This class provides data access operations for the {@link Event} entity,
 * leveraging Quarkus' PanacheRepository for simplifying common persistence logic.
 * The {@link EventRepository} is annotated with {@code @ApplicationScoped},
 * indicating that it is a CDI bean with application scope.
 *
 * The {@link Event} entity represents an event with properties like name, description,
 * date range, associated speakers, and portal type, among others. This repository facilitates
 * CRUD operations and additional query methods, if needed, for the {@link Event} entity.
 *
 * @author Eric Deandrea
 */
@ApplicationScoped
@Transactional
public class EventRepository implements PanacheRepositoryBase<Event, String> {
  private record PortalName(String portalName) {}

  /**
   * Deletes all {@link Event} entities from the repository and cascades the operation
   * to related entities such as {@link Speaker} and {@link Talk}.
   *
   * It is recommended to use this method in scenarios where all events and their
   * associated data need to be removed.
   */
  public void deleteAllWithCascade() {
    if (count() > 0) {
      findAll()
          .project(PortalName.class)
          .list()
          .stream()
          .map(PortalName::portalName)
          .forEach(this::deleteById);
    }
  }

  /**
   * Persists a collection of {@link Event} entities to the data repository.
   *
   * @param events the collection of {@link Event} entities to be persisted
   */
  public void saveEvents(Collection<Event> events) {
    events.forEach(event -> {
      persist(event);
      Log.debugf("Persisted event:\n%s", event);
    });
  }
}
