package com.redhat.cfpaggregator.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevSpeakerDetails;
import com.redhat.cfpaggregator.client.sessionize.SessionizeSpeakerDetails;
import com.redhat.cfpaggregator.client.sessionize.SessionizeSpeakerDetails.Link;
import com.redhat.cfpaggregator.domain.Speaker;
import net.datafaker.Faker;

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
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .isEqualTo(expectedSpeaker);
  }

  @Test
  void speakerFromSessionize() throws MalformedURLException {
    var fakeData = new Faker();
    var speakerDetails = new SessionizeSpeakerDetails(
        fakeData.idNumber().valid(),
        fakeData.name().firstName(),
        fakeData.name().lastName(),
        fakeData.job().keySkills(),
        fakeData.job().seniority(),
        new URL(fakeData.internet().url()),
        fakeData.collection(
            () -> createLink("Twitter", "https://x.com/someTwitterHandle"),
            () -> createLink("LinkedIn", "https://linkedin.com/in/someLinkedinUsername"),
            () -> createLink("BlueSky", "https://bsky.app/profile/bsky-user.social")
        ).generate()
    );

    var expectedSpeaker = Speaker.builder()
        .eventSpeakerId(speakerDetails.eventSpeakerId())
        .firstName(speakerDetails.firstName())
        .lastName(speakerDetails.lastName())
        .imageUrl(speakerDetails.profilePicture().toString())
        .bio(speakerDetails.bio())
        .twitterHandle("someTwitterHandle")
        .linkedInUsername("someLinkedinUsername")
        .blueskyUsername("bsky-user.social")
        .build();

    assertThat(this.speakerMapper.fromSessionize(speakerDetails))
        .isNotNull()
        .usingRecursiveComparison()
        .ignoringFieldsMatchingRegexes(".*hibernate.*")
        .isEqualTo(expectedSpeaker);
  }

  private static Optional<Link> findLink(String linkType, List<Link> links) {
    return links.stream()
        .filter(link -> link.linkType() != null)
        .filter(link -> linkType.toLowerCase().equals(link.linkType()))
        .findFirst();
  }

  private static Link createLink(String linkType, String url) {
    try {
      return new Link(linkType, linkType, new URL(url));
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}