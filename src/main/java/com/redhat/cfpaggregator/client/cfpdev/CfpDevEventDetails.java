package com.redhat.cfpaggregator.client.cfpdev;

import java.net.URL;
import java.time.Instant;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the details of an event as retrieved from the cfp.dev API.
 *
 * It holds information about an event, such as its name, description, dates, associated URLs, and timezone.
 * It is primarily used in the context of interfacing with the cfp.dev API via clients like {@link CfpDevClient}.
 *
 * @author Eric Deandrea
 */
public record CfpDevEventDetails(
    @JsonProperty("cfpClosing") Instant cfpClosing,
    @JsonProperty("cfpOpening") Instant cfpOpening,
    @JsonProperty("description") String description,
    @JsonProperty("flickrURL") URL flickrUrl,
    @JsonProperty("fromDate") Instant fromDate,
    @JsonProperty("name") String name,
    @JsonProperty("timezone") TimeZone timeZone,
    @JsonProperty("toDate") Instant toDate,
    @JsonProperty("website") URL websiteUrl,
    @JsonProperty("youTubeURL") URL youTubeUrl
) {
}
