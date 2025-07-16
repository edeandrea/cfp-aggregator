package com.redhat.cfpaggregator.client.sessionize;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionizeEventDetails(
    @JsonProperty("sessions") List<SessionizeSessionDetails> sessions,
    @JsonProperty("speakers") List<SessionizeSpeakerDetails> speakers
) {
}
