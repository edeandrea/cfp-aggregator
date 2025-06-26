package com.redhat.cfpaggregator.client.cfpdev;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CfpDevTalkSearchResults(
    @JsonProperty("searchQuery") String searchQuery,
    @JsonProperty("proposals") List<CfpDevTalkDetails> talks
    ) {
}
