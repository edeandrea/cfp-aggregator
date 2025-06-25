package com.redhat.cfpaggregator.repository;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import com.redhat.cfpaggregator.domain.Event;

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
public class EventRepository implements PanacheRepository<Event> {
}
