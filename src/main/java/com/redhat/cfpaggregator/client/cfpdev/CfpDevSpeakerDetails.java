package com.redhat.cfpaggregator.client.cfpdev;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CfpDevSpeakerDetails(
    @JsonProperty("id") long eventSpeakerId,
    @JsonProperty("firstName") String firstName,
    @JsonProperty("lastName") String lastName,
    @JsonProperty("bio") String bio,
    @JsonProperty("company") String company,
    @JsonProperty("imageUrl") URL imageUrl,
    @JsonProperty("twitterHandle") String twitterHandle,
    @JsonProperty("linkedInUsername") String linkedInUsername,
    @JsonProperty("blueskyUsername") String blueskyUsername,
    @JsonProperty("countryName") String countryName
    ) {
}
