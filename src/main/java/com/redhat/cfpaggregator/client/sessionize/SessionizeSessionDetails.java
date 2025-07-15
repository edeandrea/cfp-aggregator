package com.redhat.cfpaggregator.client.sessionize;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionizeSessionDetails(
    @JsonProperty("id") String id,
    @JsonProperty("title") String title,
    @JsonProperty("description") String description,
    @JsonProperty("startsAt") OffsetDateTime startsAt,
    @JsonProperty("endsAt") OffsetDateTime endsAt,
    @JsonProperty("speakers") List<String> speakers,
    @JsonProperty("liveUrl") URL liveUrl,
    @JsonProperty("recordingUrl") URL recordingUrl,
    @JsonProperty("status") String status
) {
}
