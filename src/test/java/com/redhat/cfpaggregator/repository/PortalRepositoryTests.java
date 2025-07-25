package com.redhat.cfpaggregator.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestTransaction
public class PortalRepositoryTests extends BaseRepositoryTests {
  @Test
  void deleteAllWithCascade() {
    this.portalRepository.deleteAllWithCascade();
    var portalRepoCount = this.portalRepository.count();
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();

    createEvent(true, true);

    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);
    assertThat(this.eventRepository.count()).isOne();
    assertThat(this.speakerRepository.count()).isOne();
    assertThat(this.talkRepository.count()).isOne();

    this.portalRepository.deleteAllWithCascade();

    assertThat(this.portalRepository.count()).isZero();
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();
  }

  @Test
  void itWorks() {
    this.portalRepository.deleteAllWithCascade();
    var portalRepoCount = this.portalRepository.count();
    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();

    this.portalRepository.persist(PORTAL.cloneAsNew());

    assertThat(this.eventRepository.count()).isZero();
    assertThat(this.speakerRepository.count()).isZero();
    assertThat(this.talkRepository.count()).isZero();
    assertThat(this.portalRepository.count()).isEqualTo(portalRepoCount + 1);

    assertThat(this.portalRepository.listAll())
        .singleElement()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .ignoringFields("event")
        .isEqualTo(PORTAL);
  }
}
