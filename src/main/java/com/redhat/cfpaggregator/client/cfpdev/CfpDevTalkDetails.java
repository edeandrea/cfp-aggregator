package com.redhat.cfpaggregator.client.cfpdev;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CfpDevTalkDetails(
    @JsonProperty("id") long eventTalkId,
    @JsonProperty("title") String title,
    @JsonProperty("description") String description,
    @JsonProperty("summary") String summary,
    @JsonProperty("afterVideoURL") URL videoUrl,
    @JsonProperty("keywords") List<Keyword> keywords,
    @JsonProperty("speakers") List<CfpDevSpeakerDetails> speakers
) {

  /**
   * Clones an instance but only keeping the speaker details that match companies
   */
  public CfpDevTalkDetails(CfpDevTalkDetails toClone, Collection<String> companies) {
    this(
        toClone,
        Optional.ofNullable(companies)
            .orElseGet(List::of)
            .toArray(String[]::new)
    );
  }

  /**
   * Clones an instance but only keeping the speaker details that match companies
   */
  public CfpDevTalkDetails(CfpDevTalkDetails toClone, String... companies) {
    this(
        toClone.eventTalkId(),
        toClone.title(),
        toClone.description(),
        toClone.summary(),
        toClone.videoUrl(),
        toClone.keywords(),
        toClone.getSpeakersFromCompanies(companies)
    );
  }

  public record Keyword(@JsonProperty("name") String name) {}

  public List<String> getKeywords() {
    return this.keywords.stream()
        .map(Keyword::name)
        .toList();
  }

  public List<CfpDevSpeakerDetails> getSpeakersFromCompanies(String... companies) {
    var companyNames = Optional.ofNullable(companies)
        .map(Set::of)
        .orElseGet(Set::of)
        .stream()
        .filter(Objects::nonNull)
        .map(String::strip)
        .collect(Collectors.toSet());

    return this.speakers.stream()
        .filter(speaker -> companyNames.contains(speaker.company()))
        .toList();
  }
}
