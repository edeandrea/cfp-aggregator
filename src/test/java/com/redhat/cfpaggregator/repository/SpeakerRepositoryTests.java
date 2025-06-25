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
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    var event = createEvent();
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isZero();

    // Clone the sample data so as to not modify it
    this.speakerRepository.persist(SPEAKER.cloneAsNewWithNewEvent(event));
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isOne();

    var speakers = this.speakerRepository.listAll();
    assertThat(speakers)
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "event")
        .isEqualTo(SPEAKER);

    var firstSpeaker = speakers.getFirst();
    assertThat(firstSpeaker.getId()).isPositive();
    assertThat(firstSpeaker.getEvent())
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "speakers")
        .isEqualTo(EVENT);
  }
}