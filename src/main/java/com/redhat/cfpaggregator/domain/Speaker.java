package com.redhat.cfpaggregator.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a speaker entity with various attributes such as personal
 * details, company details, social media handles, and a biography.
 * This class is annotated for persistence with JPA and supports a builder
 * pattern for constructing instances.
 *
 * @author Eric Deandrea
 */
@Entity
@Table(name = "speakers")
public class Speaker {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "speaker_seq")
  @SequenceGenerator(name = "speaker_seq", sequenceName = "speaker_seq", allocationSize = 1)
  private Long id;

  @NotNull(message = "event_speaker_id can not be null")
  private String eventSpeakerId;

  @NotEmpty(message = "first_name can not be null or empty")
  private String firstName;

  @NotEmpty(message = "last_name can not be null or empty")
  private String lastName;
  private String company;

  @Column(columnDefinition = "TEXT")
  private String imageUrl;
  private String twitterHandle;
  private String linkedInUsername;
  private String blueskyUsername;
  private String countryName;

  @Column(columnDefinition = "TEXT")
  private String bio;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Event event;

  @ManyToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY
  )
  @JoinTable(
      name = "speaker_talks",
      joinColumns = @JoinColumn(name = "speaker_id"),
      inverseJoinColumns = @JoinColumn(name = "talk_id")
  )
  private List<Talk> talks = new ArrayList<>();

  // Default constructor for JPA
  public Speaker() {
  }

  // Private constructor for builder
  private Speaker(Builder builder) {
    this.id = builder.id;
    this.eventSpeakerId = builder.eventSpeakerId;
    this.firstName = builder.firstName;
    this.lastName = builder.lastName;
    this.company = builder.company;
    this.imageUrl = builder.imageUrl;
    this.twitterHandle = builder.twitterHandle;
    this.linkedInUsername = builder.linkedInUsername;
    this.blueskyUsername = builder.blueskyUsername;
    this.countryName = builder.countryName;
    this.bio = builder.bio;
    this.event = builder.event;

    if (this.event != null) {
      this.event.addSpeakers(this);
    }

    if (builder.talks != null) {
      this.talks.addAll(builder.talks);
      this.talks.forEach(talk -> talk.addSpeakers(this));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public Speaker cloneAsNew() {
    return cloneAsNewWithNewEvent(null);
  }

  /**
   * Gets the number of talks by this speaker for a given event
   */
  public int getTalkCount() {
    return this.talks.size();
  }

  public String getFullName() {
    return "%s %s".formatted(this.firstName, this.lastName);
  }

  public Speaker cloneAsNewWithNewEvent(Event event) {
    return toBuilder()
        .id(null)
        .event(event)
        .talks()
        .build();
  }

  public Speaker cloneAsNewWithNewTalks(Talk... talks) {
    return toBuilder()
        .id(null)
        .event(null)
        .talks(talks)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Speaker speaker)) return false;
    return Objects.equals(id, speaker.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Speaker{" +
        "id=" + id +
        ", eventSpeakerId=" + eventSpeakerId +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", company='" + company + '\'' +
        ", imageUrl='" + imageUrl + '\'' +
        ", twitterHandle='" + twitterHandle + '\'' +
        ", linkedInUsername='" + linkedInUsername + '\'' +
        ", blueskyUsername='" + blueskyUsername + '\'' +
        ", countryName='" + countryName + '\'' +
        ", bio='" + bio + '\'' +
        Optional.ofNullable(event)
            .map(Event::getPortalName)
            .map(portalName -> ", eventPortalName=" + portalName)
            .orElse("") +
        ", talks=" + talks +
        '}';
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getTwitterHandle() {
    return twitterHandle;
  }

  public void setTwitterHandle(String twitterHandle) {
    this.twitterHandle = twitterHandle;
  }

  public String getLinkedInUsername() {
    return linkedInUsername;
  }

  public void setLinkedInUsername(String linkedInUsername) {
    this.linkedInUsername = linkedInUsername;
  }

  public String getBlueskyUsername() {
    return blueskyUsername;
  }

  public void setBlueskyUsername(String blueskyUsername) {
    this.blueskyUsername = blueskyUsername;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  public void addTalks(Talk... talks) {
    if (talks != null) {
      Arrays.stream(talks)
          .filter(Objects::nonNull)
          .forEach(talk -> {
            this.talks.add(talk);
            talk.addSpeakers(this);
          });
    }
  }

  public List<Talk> getTalks() {
    return this.talks;
  }

  public String getEventSpeakerId() {
    return eventSpeakerId;
  }

  public void setEventSpeakerId(String speakerId) {
    this.eventSpeakerId = speakerId;
  }

  public static final class Builder {
    private Long id;
    private String eventSpeakerId;
    private String firstName;
    private String lastName;
    private String company;
    private String imageUrl;
    private String twitterHandle;
    private String linkedInUsername;
    private String blueskyUsername;
    private String countryName;
    private String bio;
    private Event event;
    private List<Talk> talks = new ArrayList<>();

    private Builder() {
    }

    private Builder(Speaker speaker) {
      this.id = speaker.id;
      this.eventSpeakerId = speaker.eventSpeakerId;
      this.firstName = speaker.firstName;
      this.lastName = speaker.lastName;
      this.company = speaker.company;
      this.imageUrl = speaker.imageUrl;
      this.twitterHandle = speaker.twitterHandle;
      this.linkedInUsername = speaker.linkedInUsername;
      this.blueskyUsername = speaker.blueskyUsername;
      this.countryName = speaker.countryName;
      this.bio = speaker.bio;
      this.event = speaker.event;
    }

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder eventSpeakerId(String eventSpeakerId) {
      this.eventSpeakerId = eventSpeakerId;
      return this;
    }

    public Builder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder company(String company) {
      this.company = company;
      return this;
    }

    public Builder imageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    public Builder twitterHandle(String twitterHandle) {
      this.twitterHandle = twitterHandle;
      return this;
    }

    public Builder linkedInUsername(String linkedInUsername) {
      this.linkedInUsername = linkedInUsername;
      return this;
    }

    public Builder blueskyUsername(String blueskyUsername) {
      this.blueskyUsername = blueskyUsername;
      return this;
    }

    public Builder countryName(String countryName) {
      this.countryName = countryName;
      return this;
    }

    public Builder bio(String bio) {
      this.bio = bio;
      return this;
    }

    public Builder event(Event event) {
      this.event = event;
      return this;
    }

    public Builder talks(List<Talk> talks) {
      this.talks.clear();

      if (talks != null) {
        this.talks.addAll(talks);
      }

      return this;
    }

    public Builder talks(Talk... talks) {
      return (talks != null) ? talks(List.of(talks)) : this;
    }

    public Builder addTalk(Talk talk) {
      if (talk != null) {
        this.talks.add(talk);
      }

      return this;
    }

    public Speaker build() {
      return new Speaker(this);
    }
  }
}