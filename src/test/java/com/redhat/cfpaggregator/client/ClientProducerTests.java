package com.redhat.cfpaggregator.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.redhat.cfpaggregator.client.ClientProducerTests.ConfigTestProfile;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevClient;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevEventDetails;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.smallrye.common.annotation.Identifier;

@QuarkusTest
@TestProfile(ConfigTestProfile.class)
@ConnectWireMock
class ClientProducerTests {
  @Inject
  @Identifier(ClientProducer.CFP_DEV_CLIENTS)
  Map<String, CfpDevClient> cfpDevClients;

  WireMock wireMock;

  @BeforeEach
  void beforeEach() {
    this.wireMock.resetToDefaultMappings();
  }

  @Test
  void hasCorrectClients() {
    assertThat(this.cfpDevClients)
        .isNotNull()
        .hasSizeGreaterThanOrEqualTo(3)
        .containsKeys("portal1");
  }

  @Test
  void cfpDevClientWorks() throws MalformedURLException {
    var client = this.cfpDevClients.get("portal1");
    assertThat(client)
        .isNotNull()
        .isInstanceOf(CfpDevClient.class);

    this.wireMock.register(get(urlPathEqualTo("/api/public/event"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .willReturn(jsonResponse("""
            {
              "cfpClosing": "2025-01-10T23:59:49Z",
              "cfpOpening": "2024-11-04T00:01:49Z",
              "codeOfConduct": "https://www.devoxx.co.uk/code-of-respect/",
              "description": null,
              "enableTags": false,
              "eventImageURL": "",
              "flickrURL": "https://www.flickr.com/photos/125714253@N02/albums/",
              "fromDate": "2025-05-07T07:00:00Z",
              "id": 1151,
              "live": false,
              "location": {
                "id": 1401,
                "name": "Business Design Centre"
              },
              "locationAddress": "52 Upper Street",
              "locationCity": "London",
              "locationCountry": "United Kingdom",
              "locationId": 1401,
              "locationName": "Business Design Centre",
              "maxDescriptionLength": 1500,
              "maxProposals": 4,
              "maxTitleLength": 200,
              "myBadgeActive": null,
              "name": "Devoxx UK 2025",
              "proposalLanguages": [
                {
                  "alpha2": "en",
                  "flag32": "GB-32.png",
                  "id": 39,
                  "name": "English"
                }
              ],
              "sessionTypes": [],
              "slug": "devoxxuk25",
              "tenant": {
                "id": 1,
                "name": "DMJE Limited"
              },
              "theme": "DEVOXX",
              "timezone": "Europe/London",
              "toDate": "2025-05-09T16:00:00Z",
              "tracks": [],
              "twitterHandle": "@DevoxxUK",
              "venueLatitude": null,
              "venueLongitude": null,
              "website": "https://www.devoxx.co.uk/",
              "websiteLanguage": {
                "alpha2": "en",
                "flag32": "GB-32.png",
                "id": 39,
                "name": "English"
              },
              "youTubeURL": "https://www.youtube.com/channel/UCxIamwHotqAAdmecaKT9WpA"
            }
            """, 200)));

    assertThat(client.getEventDetails())
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(new CfpDevEventDetails(
            Instant.parse("2025-01-10T23:59:49Z"),
            Instant.parse("2024-11-04T00:01:49Z"),
            null,
            new URL("https://www.flickr.com/photos/125714253@N02/albums/"),
            Instant.parse("2025-05-07T07:00:00Z"),
            "Devoxx UK 2025",
            TimeZone.getTimeZone("Europe/London"),
            Instant.parse("2025-05-09T16:00:00Z"),
            new URL("https://www.devoxx.co.uk/"),
            new URL("https://www.youtube.com/channel/UCxIamwHotqAAdmecaKT9WpA")
        ));

    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathEqualTo("/api/public/event"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
    );
  }

  public static class ConfigTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var params = new HashMap<>(Map.of(
          "cfps.log-requests", "true",
          "cfps.log-responses", "true"
      ));

      params.putAll(Map.of(
          "cfps.portals.portal1.base-url", "http://localhost:${quarkus.wiremock.devservices.port}",
          "cfps.portals.portal1.portal-type", "CFP_DEV",
          "cfps.portals.portal1.description", "CFP Portal 1"
      ));

      return params;
    }
  }
}