package com.redhat.cfpaggregator.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevSpeakerDetails;
import com.redhat.cfpaggregator.domain.Speaker;

@QuarkusTest
class SpeakerMapperTests {
  @Inject
  SpeakerMapper speakerMapper;

  @Test
  void speakerFromCfpDev() throws MalformedURLException {
    var speakerDetails = new CfpDevSpeakerDetails(
        1234567890,
        "John",
        "Doe",
        "This is John's bio",
        "Fake Company",
        new URL("https://flikr.com/1"),
        "@johndoe",
        "johndoe",
        "johndoe.bsky.social",
        "USA"
    );

    var expectedSpeaker = Speaker.builder()
        .eventSpeakerId("1234567890")
        .firstName("John")
        .lastName("Doe")
        .company("Fake Company")
        .imageUrl("https://flikr.com/1")
        .twitterHandle("@johndoe")
        .linkedInUsername("johndoe")
        .blueskyUsername("johndoe.bsky.social")
        .countryName("USA")
        .bio("This is John's bio")
        .build();

    assertThat(this.speakerMapper.fromCfpDev(speakerDetails))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(expectedSpeaker);
  }
}