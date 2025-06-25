package com.redhat.cfpaggregator.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

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

  @NotEmpty(message = "first_name can not be null or empty")
  private String firstName;

  @NotEmpty(message = "last_name can not be null or empty")
  private String lastName;
  private String company;
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

  // Default constructor for JPA
  public Speaker() {
  }

  // Private constructor for builder
  private Speaker(Builder builder) {
    this.id = builder.id;
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
      this.event.addSpeaker(this);
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

  public Speaker cloneAsNewWithNewEvent(Event event) {
    return toBuilder()
        .id(null)
        .event(event)
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
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", company='" + company + '\'' +
        ", imageUrl='" + imageUrl + '\'' +
        ", twitterHandle='" + twitterHandle + '\'' +
        ", linkedInUsername='" + linkedInUsername + '\'' +
        ", blueskyUsername='" + blueskyUsername + '\'' +
        ", countryName='" + countryName + '\'' +
        ", bio='" + bio + '\'' +
        ", eventId=" + event.getId() +
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

  public static final class Builder {
    private Long id;
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

    public Builder() {
    }

    public Builder(Speaker speaker) {
      this.id = speaker.id;
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

    public Speaker build() {
      return new Speaker(this);
    }
  }
}