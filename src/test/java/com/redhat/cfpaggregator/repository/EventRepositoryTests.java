package com.redhat.cfpaggregator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestTransaction
class EventRepositoryTests extends BaseRepositoryTests {
  @Test
  void itWorks() {
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();

    var event = createEvent(false);

    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(event).isNotNull();
    assertThat(event.getPortalName()).isNotBlank();

    // Clone the sample data so as to not modify it
    var speaker = SPEAKER.cloneAsNew();
    event.addSpeakers(speaker);

    this.eventRepository.persist(event);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isOne();

    var events = this.eventRepository.listAll();
    assertThat(events)
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("speakers")
        .isEqualTo(EVENT);

    var firstEvent = events.getFirst();
    assertThat(firstEvent.getSpeakers())
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "event", "talks")
        .isEqualTo(SPEAKER);

    assertThat(firstEvent.getSpeakers().getFirst().getId()).isPositive();
  }
}