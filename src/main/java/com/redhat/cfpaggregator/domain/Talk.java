package com.redhat.cfpaggregator.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "talks")
public class Talk {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "talk_seq")
  @SequenceGenerator(name = "talk_seq", sequenceName = "talk_seq", allocationSize = 1)
  private Long id;

  @NotEmpty
  private String title;
  private String description;
  private String videoUrl;

  @ManyToMany(mappedBy = "talks")
  private List<Speaker> speakers = new ArrayList<>();

  // Default constructor for JPA
  public Talk() {
  }

  // Private constructor for builder
  private Talk(Builder builder) {
    this.id = builder.id;
    this.title = builder.title;
    this.description = builder.description;
    this.videoUrl = builder.videoUrl;

    if (builder.speakers != null) {
      this.speakers.addAll(builder.speakers);
      this.speakers.forEach(speaker -> speaker.addTalks(this));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public Talk cloneAsNew() {
    return cloneAsNewWithNewSpeakers();
  }

  public Talk cloneAsNewWithNewSpeakers(Speaker... speakers) {
    return toBuilder()
        .id(null)
        .speakers(speakers)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Talk talk)) return false;
    return Objects.equals(id, talk.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Talk{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", videoUrl='" + videoUrl + '\'' +
        '}';
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public void setVideoUrl(String videoUrl) {
    this.videoUrl = videoUrl;
  }

  public List<Speaker> getSpeakers() {
    return speakers;
  }

  public void setSpeakers(List<Speaker> speakers) {
    this.speakers.clear();

    if (speakers != null) {
      this.speakers.addAll(speakers);
    }
  }

  public void setSpeakers(Speaker... speakers) {
    if (speakers != null) {
      setSpeakers(List.of(speakers));
    }
  }

  public void addSpeakers(Speaker... speakers) {
    if (speakers != null) {
      Arrays.stream(speakers)
          .filter(Objects::nonNull)
          .forEach(this.speakers::add);
    }
  }

  public static final class Builder {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private List<Speaker> speakers = new ArrayList<>();

    public Builder() {
    }

    public Builder(Talk talk) {
      this.id = talk.id;
      this.title = talk.title;
      this.description = talk.description;
      this.videoUrl = talk.videoUrl;
      this.speakers = new ArrayList<>(talk.speakers);
    }

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder videoUrl(String videoUrl) {
      this.videoUrl = videoUrl;
      return this;
    }

    public Builder speakers(List<Speaker> speakers) {
      this.speakers.clear();

      if (speakers != null) {
        this.speakers.addAll(speakers);
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

    public Talk build() {
      return new Talk(this);
    }
  }
}