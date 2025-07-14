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
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.ui.views.EventViews.EventName;

@QuarkusTest
class EventMapperTests {
  @Inject
  EventMapper eventMapper;

  @Test
  void eventNameFromEvent() {
    var event = Event.builder()
        .cfpClosing(Instant.parse("2025-01-10T23:59:49Z"))
        .cfpOpening(Instant.parse("2024-11-04T00:01:49Z"))
        .flickrUrl("https://www.flickr.com/photos/125714253@N02/albums/")
        .fromDate(Instant.parse("2025-05-07T07:00:00Z"))
        .name("Devoxx UK 2025")
        .timeZone("Europe/London")
        .toDate(Instant.parse("2025-05-09T16:00:00Z"))
        .websiteUrl("https://www.devoxx.co.uk/")
        .youTubeUrl("https://www.youtube.com/channel/UCxIamwHotqAAdmecaKT9WpA")
        .portalName("testPortal")
        .build();

    var expectedEventName = new EventName(event.getPortalName(), event.getName(), event.getTimeZone(), event.getFromDate(), event.getCfpOpening(), event.getCfpClosing());

    assertThat(this.eventMapper.fromEvent(event))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(expectedEventName);
  }

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
        .timeZone(eventDetails.timeZone().getID())
        .toDate(eventDetails.toDate())
        .websiteUrl(eventDetails.websiteUrl().toString())
        .youTubeUrl(eventDetails.youTubeUrl().toString())
        .portalName("testPortal")
        .build();

    assertThat(this.eventMapper.fromCfpDev("testPortal", eventDetails))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(expectedEvent);
  }
}