package com.redhat.cfpaggregator.repository;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import com.redhat.cfpaggregator.domain.Speaker;

@ApplicationScoped
public class SpeakerRepository implements PanacheRepository<Speaker> {
}
