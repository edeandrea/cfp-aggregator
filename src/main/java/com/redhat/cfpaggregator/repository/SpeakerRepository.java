package com.redhat.cfpaggregator.repository;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import com.redhat.cfpaggregator.domain.Speaker;

/**
 * Repository class for managing {@link Speaker} entities.
 *
 * This class provides data access operations for the {@link Speaker} entity,
 * utilizing Quarkus' PanacheRepository to enable convenient persistence,
 * query, and transactional logic handling.
 *
 * The {@link SpeakerRepository} is annotated with {@code @ApplicationScoped},
 * denoting it as a CDI bean with application scope. This ensures that the
 * repository is available as a singleton throughout the application lifecycle.
 *
 * The {@link Speaker} entity represents a speaker with attributes including
 * personal details (e.g., first name, last name), professional information
 * (e.g., company), social media handles, biography, and an associated event.
 * This repository simplifies CRUD operations and allows extending custom
 * data operations for the {@link Speaker} entity if needed.
 *
 * @author Eric Deandrea
 */
@ApplicationScoped
public class SpeakerRepository implements PanacheRepository<Speaker> {
}
