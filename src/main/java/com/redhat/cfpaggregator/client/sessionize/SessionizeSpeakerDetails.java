package com.redhat.cfpaggregator.client.sessionize;

import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionizeSpeakerDetails(
  @JsonProperty("id") String id,
  @JsonProperty("firstName") String firstName,
  @JsonProperty("lastName") String lastName,
  @JsonProperty("bio") String bio,
  @JsonProperty("tagLine") String tagLine,
  @JsonProperty("profilePicture") URL profilePicture,
  @JsonProperty("links") List<Link> links
) {
  public record Link(
      @JsonProperty("title") String title,
      @JsonProperty("linkType") String linkType,
      @JsonProperty("url") URL url) {}
}
