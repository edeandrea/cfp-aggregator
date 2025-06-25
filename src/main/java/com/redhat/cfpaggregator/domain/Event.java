package com.redhat.cfpaggregator.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.redhat.cfpaggregator.config.CfpPortalsConfig.PortalType;

@Entity
@Table(name = "events")
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "speaker_seq")
  @SequenceGenerator(name = "speaker_seq", sequenceName = "speaker_seq", allocationSize = 1)
  private Long id;
  
  @NotEmpty(message = "name can not be null or empty")
  private String name;
  private String description;
  private String flickrUrl;
  private Instant fromDate;
  private String timeZone;
  private Instant toDate;
  private String websiteUrl;
  private String youTubeUrl;
  
  @Enumerated(EnumType.STRING)
  @NotNull(message = "portal_type can not be null")
  private PortalType portalType;

  @OneToMany(
      mappedBy = "event",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<Speaker> speakers = new ArrayList<>();

  // Default constructor for JPA
  public Event() {
  }

  // Private constructor for builder
  private Event(Builder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.description = builder.description;
    this.flickrUrl = builder.flickrUrl;
    this.fromDate = builder.fromDate;
    this.timeZone = builder.timeZone;
    this.toDate = builder.toDate;
    this.websiteUrl = builder.websiteUrl;
    this.youTubeUrl = builder.youTubeUrl;
    this.portalType = builder.portalType;

    if (builder.speakers != null) {
      this.speakers.addAll(builder.speakers);
      this.speakers.forEach(speaker -> speaker.setEvent(this));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public Event cloneAsNew() {
    return cloneAsNewWithNewSpeakers();
  }

  public Event cloneAsNewWithNewSpeakers(Speaker... speakers) {
    return toBuilder()
        .id(null)
        .speakers(speakers)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Event event)) return false;
    return Objects.equals(id, event.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFlickrUrl() {
    return flickrUrl;
  }

  public void setFlickrUrl(String flickrUrl) {
    this.flickrUrl = flickrUrl;
  }

  public Instant getFromDate() {
    return fromDate;
  }

  public void setFromDate(Instant fromDate) {
    this.fromDate = fromDate;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public Instant getToDate() {
    return toDate;
  }

  public void setToDate(Instant toDate) {
    this.toDate = toDate;
  }

  public String getWebsiteUrl() {
    return websiteUrl;
  }

  public void setWebsiteUrl(String websiteUrl) {
    this.websiteUrl = websiteUrl;
  }

  public String getYouTubeUrl() {
    return youTubeUrl;
  }

  public void setYouTubeUrl(String youTubeUrl) {
    this.youTubeUrl = youTubeUrl;
  }

  public PortalType getPortalType() {
    return portalType;
  }

  public void setPortalType(PortalType portalType) {
    this.portalType = portalType;
  }

  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", flickrUrl='" + flickrUrl + '\'' +
        ", fromDate=" + fromDate +
        ", timeZone='" + timeZone + '\'' +
        ", toDate=" + toDate +
        ", websiteUrl='" + websiteUrl + '\'' +
        ", youTubeUrl='" + youTubeUrl + '\'' +
        ", portalType=" + portalType +
        ", speakers=" + speakers +
        '}';
  }

  public void addSpeaker(Speaker speaker) {
    if (speaker != null) {
      this.speakers.add(speaker);
      speaker.setEvent(this);
    }
  }

  public void addSpeakers(Speaker... speakers) {
    if (speakers != null) {
      Arrays.stream(speakers)
          .filter(Objects::nonNull)
          .forEach(this::addSpeaker);
    }
  }

  public List<Speaker> getSpeakers() {
    return speakers;
  }

  public void setSpeakers(List<Speaker> speakers) {
    this.speakers.clear();

    if (speakers != null) {
      this.speakers.addAll(speakers);
      this.speakers.forEach(speaker -> speaker.setEvent(this));
    }
  }

  public void setSpeakers(Speaker... speakers) {
    if (speakers != null) {
      setSpeakers(List.of(speakers));
    }
  }

  public static final class Builder {
    private Long id;
    private String name;
    private String description;
    private String flickrUrl;
    private Instant fromDate;
    private String timeZone;
    private Instant toDate;
    private String websiteUrl;
    private String youTubeUrl;
    private PortalType portalType;
    private List<Speaker> speakers = new ArrayList<>();

    public Builder() {
    }

    public Builder(Event event) {
      this.id = event.id;
      this.name = event.name;
      this.description = event.description;
      this.flickrUrl = event.flickrUrl;
      this.fromDate = event.fromDate;
      this.timeZone = event.timeZone;
      this.toDate = event.toDate;
      this.websiteUrl = event.websiteUrl;
      this.youTubeUrl = event.youTubeUrl;
      this.portalType = event.portalType;
      this.speakers = event.speakers;
    }

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder flickrUrl(String flickrUrl) {
      this.flickrUrl = flickrUrl;
      return this;
    }

    public Builder fromDate(Instant fromDate) {
      this.fromDate = fromDate;
      return this;
    }

    public Builder timeZone(String timeZone) {
      this.timeZone = timeZone;
      return this;
    }

    public Builder toDate(Instant toDate) {
      this.toDate = toDate;
      return this;
    }

    public Builder websiteUrl(String websiteUrl) {
      this.websiteUrl = websiteUrl;
      return this;
    }

    public Builder youTubeUrl(String youTubeUrl) {
      this.youTubeUrl = youTubeUrl;
      return this;
    }

    public Builder portalType(PortalType portalType) {
      this.portalType = portalType;
      return this;
    }

    public Builder speakers(List<Speaker> speakers) {
      this.speakers = speakers;

      if (this.speakers == null) {
        this.speakers = new ArrayList<>();
      }

      return this;
    }

    public Builder speakers(Speaker... speakers) {
      return (speakers != null) ? speakers(List.of(speakers)) : this;
    }

    public Builder addSpeaker(Speaker speaker) {
      if (speaker != null) {
        this.speakers.add(speaker);
      }

      return this;
    }

    public Event build() {
      return new Event(this);
    }
  }
}