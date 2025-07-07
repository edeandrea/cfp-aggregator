package com.redhat.cfpaggregator.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;

import jakarta.inject.Inject;

import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.PortalType;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.Talk;

abstract class BaseRepositoryTests {
  protected static final TimeZone TIMEZONE = TimeZone.getDefault();

  protected static final Portal PORTAL = Portal.builder()
      .portalName("Test Portal")
      .baseUrl("http://somewhere.com")
      .description("Some portal")
      .portalType(PortalType.CFP_DEV)
      .build();

  protected static final Event EVENT = Event.builder()
      .portalName("Test Portal")
      .name("Test Event")
      .description("Some event somewhere")
      .fromDate(Instant.now().minusSeconds(Duration.ofDays(2).toSeconds()))
      .toDate(Instant.now())
      .timeZone(TIMEZONE.getDisplayName())
      .websiteUrl("https://www.example.com")
      .cfpOpening(Instant.now().minusSeconds(Duration.ofDays(90).toSeconds()))
      .cfpClosing(Instant.now().minusSeconds(Duration.ofDays(75).toSeconds()))
      .build();

  protected static final Speaker SPEAKER = Speaker.builder()
      .bio("""
            This is my bio!
            
            I hope you like it!
            """)
      .eventSpeakerId(1234567890L)
      .blueskyUsername("ericdeandrea.dev")
      .twitterHandle("@edeandrea")
      .linkedInUsername("edeandrea")
      .company("Red Hat")
      .firstName("Eric")
      .lastName("Deandrea")
      .countryName("USA")
      .build();

  protected static final Talk TALK = Talk.builder()
      .eventTalkId(6586L)
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

  @Inject
  PortalRepository portalRepository;

  protected Event createEvent(boolean withSpeaker) {
    return createEvent(withSpeaker, false);
  }

  protected Event createEvent(boolean withSpeaker, boolean withTalk) {
    var event = EVENT.cloneAsNew();

    if (withSpeaker || withTalk) {
      var speaker = SPEAKER.cloneAsNew();

      if (withTalk) {
        speaker.addTalks(TALK.cloneAsNew());
      }

      event.addSpeakers(speaker);
    }

    this.portalRepository.persist(PORTAL.cloneAsNewWithNewEvent(event));
    return event;
  }
}
