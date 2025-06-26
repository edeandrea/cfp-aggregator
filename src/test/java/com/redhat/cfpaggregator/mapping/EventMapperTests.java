package com.redhat.cfpaggregator.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.TimeZone;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevEventDetails;
import com.redhat.cfpaggregator.config.CfpPortalsConfig.PortalType;
import com.redhat.cfpaggregator.domain.Event;

@QuarkusTest
class EventMapperTests {
  @Inject
  EventMapper eventMapper;

  @Test
  void eventFromCfpDev() throws MalformedURLException {
    var eventDetails = new CfpDevEventDetails(
            Instant.parse("2025-01-10T23:59:49Z"),
            Instant.parse("2024-11-04T00:01:49Z"),
            null,
            new URL("https://www.flickr.com/photos/125714253@N02/albums/"),
            Instant.parse("2025-05-07T07:00:00Z"),
            "Devoxx UK 2025",
            TimeZone.getTimeZone("Europe/London"),
            Instant.parse("2025-05-09T16:00:00Z"),
            new URL("https://www.devoxx.co.uk/"),
            new URL("https://www.youtube.com/channel/UCxIamwHotqAAdmecaKT9WpA")
        );

    var expectedEvent = Event.builder()
        .cfpClosing(eventDetails.cfpClosing())
        .cfpOpening(eventDetails.cfpOpening())
        .flickrUrl(eventDetails.flickrUrl().toString())
        .fromDate(eventDetails.fromDate())
        .name(eventDetails.name())
        .timeZone(eventDetails.timeZone().getDisplayName())
        .toDate(eventDetails.toDate())
        .websiteUrl(eventDetails.websiteUrl().toString())
        .youTubeUrl(eventDetails.youTubeUrl().toString())
        .portalName("testPortal")
        .portalType(PortalType.CFP_DEV)
        .build();

    var event = this.eventMapper.fromCfpDev("testPortal", PortalType.CFP_DEV, eventDetails);

    assertThat(event)
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(expectedEvent);
  }
}