package com.redhat.cfpaggregator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestTransaction
class EventRepositoryTests extends BaseRepositoryTests {
  @Test
  void deleteAllWithCascade() {
    var portalRepoCount = this.portalRepository.count();
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();

    createEvent(true, true);

    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isOne();
    assertThat(this.talkRepository.count()).isOne();

    this.eventRepository.deleteAllWithCascade();

    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();
  }

  @Test
  void itWorks() {
    var portalRepoCount = this.portalRepository.count();
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();

    var event = createEvent(false);

    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();
    assertThat(event).isNotNull();
    assertThat(event.getPortalName()).isNotBlank();

    // Clone the sample data so as to not modify it
    var speaker = SPEAKER.cloneAsNew();
    event.addSpeakers(speaker);

    this.eventRepository.persist(event);
    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isOne();
    assertThat(this.talkRepository.count()).isZero();

    var events = this.eventRepository.listAll();
    assertThat(events)
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("speakers", "portal")
        .isEqualTo(EVENT);

    var firstEvent = events.getFirst();
    assertThat(firstEvent.getSpeakers())
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "event", "talks")
        .isEqualTo(SPEAKER);

    assertThat(firstEvent.getSpeakers().getFirst().getId()).isPositive();
    assertThat(event.getSpeakerCount()).isOne();
    assertThat(event.getTalkCount()).isZero();
  }
}