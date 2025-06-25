package com.redhat.cfpaggregator.repository;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import com.redhat.cfpaggregator.domain.Talk;

/**
 * Repository class for managing {@link Talk} entities.
 *
 * This class provides data access operations for the {@link Talk} entity,
 * leveraging Quarkus' PanacheRepository to streamline common persistence functionality.
 * The {@link TalkRepository} is marked with {@code @ApplicationScoped}, designating it
 * as a CDI bean with application scope, ensuring its availability as a singleton
 * during the application's lifecycle.
 *
 * @author Eric Deandrea
 */
@ApplicationScoped
public class TalkRepository implements PanacheRepository<Talk> {
}
