package com.redhat.cfpaggregator.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;

import jakarta.inject.Inject;

import com.redhat.cfpaggregator.config.CfpPortalsConfig.PortalType;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.Talk;

abstract class BaseRepositoryTests {
  protected static final TimeZone TIMEZONE = TimeZone.getDefault();
  protected static final Event EVENT = Event.builder()
      .name("Test Event")
      .description("Some event somewhere")
      .fromDate(Instant.now().minusSeconds(Duration.ofDays(2).toSeconds()))
      .toDate(Instant.now())
      .timeZone(TIMEZONE.getDisplayName())
      .websiteUrl("https://www.example.com")
      .portalType(PortalType.CFP_DEV)
      .build();

  protected static final Speaker SPEAKER = Speaker.builder()
      .bio("""
            This is my bio!
            
            I hope you like it!
            """)
      .blueskyUsername("ericdeandrea.dev")
      .twitterHandle("@edeandrea")
      .linkedInUsername("edeandrea")
      .company("Red Hat")
      .firstName("Eric")
      .lastName("Deandrea")
      .countryName("USA")
      .build();

  protected static final Talk TALK = Talk.builder()
      .title("Some talk title")
      .description("Some fun talk")
      .videoUrl("https://www.youtube.com")
      .build();

  @Inject
  EventRepository eventRepository;

  @Inject
  SpeakerRepository speakerRepository;

  @Inject
  TalkRepository talkRepository;

  protected Event createEvent(boolean withSpeaker) {
    var event = EVENT.cloneAsNew();

    if (withSpeaker) {
      event.addSpeakers(SPEAKER.cloneAsNew());
    }

    this.eventRepository.persist(event);

    return event;
  }
}
