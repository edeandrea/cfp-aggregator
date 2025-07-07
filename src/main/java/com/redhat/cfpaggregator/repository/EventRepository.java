package com.redhat.cfpaggregator.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
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
  private record PortalEntity(Portal portal) {};

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
          .project(PortalEntity.class)
          .list()
          .stream()
          .forEach(portal -> {
            portal.portal().setEvent(null);
            deleteById(portal.portal().getPortalName());
          });
    }
  }
}
