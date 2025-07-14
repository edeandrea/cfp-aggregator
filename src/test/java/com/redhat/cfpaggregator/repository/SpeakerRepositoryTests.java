package com.redhat.cfpaggregator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestTransaction
class SpeakerRepositoryTests extends BaseRepositoryTests {
  @Test
  void itWorks() {
    var portalRepoCount = this.portalRepository.count();
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();

    // Create the event
    var event = createEvent(false);
    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();

    // Persist the speaker
    // Clone the sample data so as to not modify it
    this.speakerRepository.persist(SPEAKER.cloneAsNewWithNewEvent(event));
    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isOne();
    assertThat(this.talkRepository.count()).isZero();
    assertThat(event.getSpeakerCount()).isOne();
    assertThat(event.getTalkCount()).isZero();

    var speakers = this.speakerRepository.listAll();
    assertThat(speakers)
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "event", "talks")
        .isEqualTo(SPEAKER);

    var firstSpeaker = speakers.getFirst();
    assertThat(firstSpeaker.getId()).isPositive();
    assertThat(firstSpeaker.getEvent())
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "speakers", "portal")
        .isEqualTo(EVENT);
    assertThat(firstSpeaker.getTalks()).isEmpty();
    assertThat(firstSpeaker.getTalkCount()).isZero();
  }
}