package com.redhat.cfpaggregator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestTransaction
class TalkRepositoryTests extends BaseRepositoryTests {
  @Test
  void itWorks() {
    var portalRepoCount = this.portalRepository.count();
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();

    // Add the event and the speaker
    var event = createEvent(true);
    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isOne();
    assertThat(this.talkRepository.count()).isZero();
    assertThat(event).isNotNull();
    assertThat(event.getPortalName()).isNotBlank();
    assertThat(event.getSpeakers()).hasSize(1);

    var speaker = event.getSpeakers().getFirst();
    assertThat(speaker)
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "event", "talks")
        .isEqualTo(SPEAKER);

    // Clone the sample data so as to not modify it
    // Persist the talk
    var talk = TALK.cloneAsNew();
    speaker.addTalks(talk);

    this.talkRepository.persist(talk);
    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isOne();
    assertThat(this.talkRepository.count()).isOne();
    assertThat(event.getSpeakerCount()).isOne();
    assertThat(event.getTalkCount()).isOne();

    var speakers = this.speakerRepository.listAll();
    assertThat(speakers)
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "event", "talks")
        .isEqualTo(SPEAKER);

    var firstSpeaker = speakers.getFirst();
    assertThat(firstSpeaker.getTalkCount()).isOne();
    assertThat(firstSpeaker.getTalks())
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "speakers")
        .isEqualTo(TALK);

    var talks = this.talkRepository.listAll();
    assertThat(talks)
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "speakers")
        .isEqualTo(TALK);

    var firstTalk = talks.getFirst();
    assertThat(firstTalk.getId()).isPositive();
    assertThat(firstTalk.getSpeakers())
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("id", "event", "talks")
        .isEqualTo(SPEAKER);
  }
}