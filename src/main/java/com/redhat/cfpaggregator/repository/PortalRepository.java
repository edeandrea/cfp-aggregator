package com.redhat.cfpaggregator.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.Talk;

@ApplicationScoped
@Transactional
public class PortalRepository implements PanacheRepositoryBase<Portal, String> {
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

      flush();
    }
  }
}
