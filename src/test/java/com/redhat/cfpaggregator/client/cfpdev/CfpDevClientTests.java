package com.redhat.cfpaggregator.client.cfpdev;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
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
import com.redhat.cfpaggregator.client.ClientProducer;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevClient.Company;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevClientTests.ConfigTestProfile;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.smallrye.common.annotation.Identifier;

@QuarkusTest
@TestProfile(ConfigTestProfile.class)
@ConnectWireMock
class CfpDevClientTests {
  @Inject
  @Identifier(ClientProducer.CFP_DEV_CLIENTS)
  Map<String, CfpDevClient> cfpDevClients;

  WireMock wireMock;

  @BeforeEach
  void beforeEach() {
    this.wireMock.resetToDefaultMappings();
  }

  private CfpDevClient getClient() {
    return assertThat(this.cfpDevClients.get("portal1"))
        .isNotNull()
        .isInstanceOf(CfpDevClient.class)
        .actual();
  }

  @Test
  void hasCorrectClients() {
    assertThat(this.cfpDevClients)
        .isNotNull()
        .hasSizeGreaterThanOrEqualTo(3)
        .containsKeys("portal1")
        .extractingByKey("portal1")
        .isInstanceOf(CfpDevClient.class);
  }

  @Test
  void eventDetails() throws MalformedURLException {
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

    assertThat(getClient().getEventDetails())
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

  @Test
  void getAllSpeakers() throws MalformedURLException {
    this.wireMock.register(get(urlPathEqualTo("/api/public/speakers"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .willReturn(jsonResponse("""
            [
              {
                "id": 3729,
                "firstName": "Alexander",
                "lastName": "Chatzizacharias",
                "fullName": "Alexander Chatzizacharias",
                "bio": "\\u003Cp\\u003EAlexander, a 34-year-old Software Engineer at JDriven, holds dual Dutch and Greek nationality. He earned his master’s degree in Game Studies from the University of Amsterdam, where he discovered his passion for gamification and software engineering. Alexander aims to bridge the gap between game development and software engineering, believing that both industries have much to learn from each other. He is dedicated to integrating technologies and methodologies from both fields. Additionally, he enjoys experimenting with new technologies and cutting-edge sdk&#39;s.\\u003C/p\\u003E",
                "anonymizedBio": null,
                "company": "JDriven",
                "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-2d4302da-4a51-4826-a215-5846aa20bb42.jpg",
                "twitterHandle": "@alex90_ch",
                "linkedInUsername": "alexander-chatzizacharias",
                "blueskyUsername": null,
                "mastodonUsername": null,
                "countryName": null
              },
              {
                "id": 13619,
                "firstName": "Alina",
                "lastName": "Yurenko",
                "fullName": "Alina Yurenko",
                "bio": "\\u003Cp\\u003EAlina is a developer advocate for GraalVM at Oracle Labs, a research &amp; development organization at Oracle. Loves both programming and natural languages, compilers, and open source.\\u003C/p\\u003E",
                "anonymizedBio": null,
                "company": "Oracle",
                "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-92942fe9-dea6-432e-b4d6-c1396f07ab38.jpg",
                "twitterHandle": "@alina_yurenko",
                "linkedInUsername": "alinayurenko",
                "blueskyUsername": "alina-yurenko.bsky.social",
                "mastodonUsername": "",
                "countryName": null
              },
              {
                  "id": 15374,
                  "firstName": "Ben",
                  "lastName": "Evans",
                  "fullName": "Ben Evans",
                  "bio": "\\u003Cp\\u003EBen Evans is an author, architect and educator. He is currently Observability Lead and Senior Principal Software Engineer at Red Hat Runtimes.\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EPreviously he was Lead Architect for Instrumentation at New Relic, and co-founded jClarity, a performance tools startup acquired by Microsoft. He has also worked as Chief Architect for Listed Derivatives at Deutsche Bank and as Senior Technical Instructor for Morgan Stanley. He served for 6 years on the Java Community Process Executive Committee, helping define new Java standards.\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EHe is a Java Champion and 3-time JavaOne Rockstar Speaker. Ben is the author of six books, including &quot;Optimizing Cloud Native Java&quot; (O&#39;Reilly), the new editions of “Java in a Nutshell” and the recently-updated “The Well-Grounded Java Developer” (Maning) and his technical articles are read by thousands of developers every month.\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EBen is a regular speaker and educator on topics such as the Java platform, systems architecture, performance and concurrency for companies and conferences all over the world.\\u003C/p\\u003E",
                  "anonymizedBio": null,
                  "company": "Red Hat",
                  "imageUrl": "https://lh3.googleusercontent.com/a-/AFdZucqTSqdIcl52deUIO4MP6RPU04VEAKtZlgxXOXhO=s96-c",
                  "twitterHandle": "",
                  "linkedInUsername": "",
                  "blueskyUsername": "",
                  "mastodonUsername": "https://mastodon.social/@kittylyst",
                  "countryName": null
                },
                {
                  "id": 22346,
                  "firstName": "Bruno",
                  "lastName": "Meseguer",
                  "fullName": "Bruno Meseguer",
                  "bio": "\\u003Cp\\u003E\\u003Cspan style=\\"background-color: transparent; color: rgb(0, 0, 0);\\"\\u003EBruno Meseguer is a Technical Marketing Manager for Red Hat in the integration space. Before joining Red Hat in 2015, he worked as a developer and architect for over 15 years in various industries, designing and building large throughput integration platforms. He advocates for cloud-native and distributed integration best practices &amp; technologies.\\u003C/span\\u003E\\u003C/p\\u003E",
                  "anonymizedBio": null,
                  "company": "Red Hat",
                  "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-0981259c-c8b2-41a6-a70b-77f0c8a4ecd3.png",
                  "twitterHandle": "",
                  "linkedInUsername": "bmeseguer",
                  "blueskyUsername": "",
                  "mastodonUsername": "",
                  "countryName": null
                },
                {
                  "id": 1,
                  "firstName": "Eric",
                  "lastName": "Deandrea",
                  "fullName": "Eric Deandrea",
                  "bio": "Eric works for IBM",
                  "anonymizedBio": null,
                  "company": "IBM",
                  "imageUrl": "",
                  "twitterHandle": "@edeandrea",
                  "linkedInUsername": "edeandrea",
                  "blueskyUsername": "ericdeandrea.dev",
                  "mastodonUsername": "",
                  "countryName": null
                }
            ]
            """, 200)));

    assertThat(getClient().findSpeakersByCompany())
        .isNotNull()
        .hasSize(5)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            new CfpDevSpeakerDetails(
                3729,
                "Alexander",
                "Chatzizacharias",
                "\u003Cp\u003EAlexander, a 34-year-old Software Engineer at JDriven, holds dual Dutch and Greek nationality. He earned his master’s degree in Game Studies from the University of Amsterdam, where he discovered his passion for gamification and software engineering. Alexander aims to bridge the gap between game development and software engineering, believing that both industries have much to learn from each other. He is dedicated to integrating technologies and methodologies from both fields. Additionally, he enjoys experimenting with new technologies and cutting-edge sdk&#39;s.\u003C/p\u003E",
                "JDriven",
                new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-2d4302da-4a51-4826-a215-5846aa20bb42.jpg"),
                "@alex90_ch",
                "alexander-chatzizacharias",
                null,
                null
            ),
            new CfpDevSpeakerDetails(
                13619,
                "Alina",
                "Yurenko",
                "\u003Cp\u003EAlina is a developer advocate for GraalVM at Oracle Labs, a research &amp; development organization at Oracle. Loves both programming and natural languages, compilers, and open source.\u003C/p\u003E",
                "Oracle",
                new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-92942fe9-dea6-432e-b4d6-c1396f07ab38.jpg"),
                "@alina_yurenko",
                "alinayurenko",
                "alina-yurenko.bsky.social",
                null
            ),
            new CfpDevSpeakerDetails(
                15374,
                "Ben",
                "Evans",
                "\u003Cp\u003EBen Evans is an author, architect and educator. He is currently Observability Lead and Senior Principal Software Engineer at Red Hat Runtimes.\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003EPreviously he was Lead Architect for Instrumentation at New Relic, and co-founded jClarity, a performance tools startup acquired by Microsoft. He has also worked as Chief Architect for Listed Derivatives at Deutsche Bank and as Senior Technical Instructor for Morgan Stanley. He served for 6 years on the Java Community Process Executive Committee, helping define new Java standards.\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003EHe is a Java Champion and 3-time JavaOne Rockstar Speaker. Ben is the author of six books, including &quot;Optimizing Cloud Native Java&quot; (O&#39;Reilly), the new editions of “Java in a Nutshell” and the recently-updated “The Well-Grounded Java Developer” (Maning) and his technical articles are read by thousands of developers every month.\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003EBen is a regular speaker and educator on topics such as the Java platform, systems architecture, performance and concurrency for companies and conferences all over the world.\u003C/p\u003E",
                "Red Hat",
                new URL("https://lh3.googleusercontent.com/a-/AFdZucqTSqdIcl52deUIO4MP6RPU04VEAKtZlgxXOXhO=s96-c"),
                null,
                null,
                null,
                null
            ),
            new CfpDevSpeakerDetails(
                22346,
                "Bruno",
                "Meseguer",
                "\u003Cp\u003E\u003Cspan style=\"background-color: transparent; color: rgb(0, 0, 0);\"\u003EBruno Meseguer is a Technical Marketing Manager for Red Hat in the integration space. Before joining Red Hat in 2015, he worked as a developer and architect for over 15 years in various industries, designing and building large throughput integration platforms. He advocates for cloud-native and distributed integration best practices &amp; technologies.\u003C/span\u003E\u003C/p\u003E",
                "Red Hat",
                new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-0981259c-c8b2-41a6-a70b-77f0c8a4ecd3.png"),
                null,
                "bmeseguer",
                null,
                null
            ),
            new CfpDevSpeakerDetails(
                1,
                "Eric",
                "Deandrea",
                "Eric works for IBM",
                "IBM",
                null,
                "@edeandrea",
                "edeandrea",
                "ericdeandrea.dev",
                null
            )
        );

    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathEqualTo("/api/public/speakers"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
    );
  }

  @Test
  void getSpeakersByCompany() throws MalformedURLException {
    this.wireMock.register(get(urlPathTemplate("/api/public/speakers/search/{query}"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .withPathParam("query", equalTo(Company.RED_HAT.getStrVersion()))
        .willReturn(jsonResponse("""
            [
              {
                  "id": 15374,
                  "firstName": "Ben",
                  "lastName": "Evans",
                  "fullName": "Ben Evans",
                  "bio": "\\u003Cp\\u003EBen Evans is an author, architect and educator. He is currently Observability Lead and Senior Principal Software Engineer at Red Hat Runtimes.\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EPreviously he was Lead Architect for Instrumentation at New Relic, and co-founded jClarity, a performance tools startup acquired by Microsoft. He has also worked as Chief Architect for Listed Derivatives at Deutsche Bank and as Senior Technical Instructor for Morgan Stanley. He served for 6 years on the Java Community Process Executive Committee, helping define new Java standards.\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EHe is a Java Champion and 3-time JavaOne Rockstar Speaker. Ben is the author of six books, including &quot;Optimizing Cloud Native Java&quot; (O&#39;Reilly), the new editions of “Java in a Nutshell” and the recently-updated “The Well-Grounded Java Developer” (Maning) and his technical articles are read by thousands of developers every month.\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EBen is a regular speaker and educator on topics such as the Java platform, systems architecture, performance and concurrency for companies and conferences all over the world.\\u003C/p\\u003E",
                  "anonymizedBio": null,
                  "company": "Red Hat",
                  "imageUrl": "https://lh3.googleusercontent.com/a-/AFdZucqTSqdIcl52deUIO4MP6RPU04VEAKtZlgxXOXhO=s96-c",
                  "twitterHandle": "",
                  "linkedInUsername": "",
                  "blueskyUsername": "",
                  "mastodonUsername": "https://mastodon.social/@kittylyst",
                  "countryName": null
                },
                {
                  "id": 22346,
                  "firstName": "Bruno",
                  "lastName": "Meseguer",
                  "fullName": "Bruno Meseguer",
                  "bio": "\\u003Cp\\u003E\\u003Cspan style=\\"background-color: transparent; color: rgb(0, 0, 0);\\"\\u003EBruno Meseguer is a Technical Marketing Manager for Red Hat in the integration space. Before joining Red Hat in 2015, he worked as a developer and architect for over 15 years in various industries, designing and building large throughput integration platforms. He advocates for cloud-native and distributed integration best practices &amp; technologies.\\u003C/span\\u003E\\u003C/p\\u003E",
                  "anonymizedBio": null,
                  "company": "Red Hat",
                  "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-0981259c-c8b2-41a6-a70b-77f0c8a4ecd3.png",
                  "twitterHandle": "",
                  "linkedInUsername": "bmeseguer",
                  "blueskyUsername": "",
                  "mastodonUsername": "",
                  "countryName": null
                }
            ]
            """, 200)));

    this.wireMock.register(get(urlPathTemplate("/api/public/speakers/search/{query}"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .withPathParam("query", equalTo(Company.IBM.getStrVersion()))
        .willReturn(jsonResponse("""
            [
              {
                  "id": 1,
                  "firstName": "Eric",
                  "lastName": "Deandrea",
                  "fullName": "Eric Deandrea",
                  "bio": "Eric works for IBM",
                  "anonymizedBio": null,
                  "company": "IBM",
                  "imageUrl": "",
                  "twitterHandle": "@edeandrea",
                  "linkedInUsername": "edeandrea",
                  "blueskyUsername": "ericdeandrea.dev",
                  "mastodonUsername": "",
                  "countryName": null
                }
            ]
            """, 200)));

    assertThat(getClient().findSpeakersByCompany(Company.RED_HAT, Company.IBM))
        .isNotNull()
        .hasSize(3)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            new CfpDevSpeakerDetails(
                15374,
                "Ben",
                "Evans",
                "\u003Cp\u003EBen Evans is an author, architect and educator. He is currently Observability Lead and Senior Principal Software Engineer at Red Hat Runtimes.\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003EPreviously he was Lead Architect for Instrumentation at New Relic, and co-founded jClarity, a performance tools startup acquired by Microsoft. He has also worked as Chief Architect for Listed Derivatives at Deutsche Bank and as Senior Technical Instructor for Morgan Stanley. He served for 6 years on the Java Community Process Executive Committee, helping define new Java standards.\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003EHe is a Java Champion and 3-time JavaOne Rockstar Speaker. Ben is the author of six books, including &quot;Optimizing Cloud Native Java&quot; (O&#39;Reilly), the new editions of “Java in a Nutshell” and the recently-updated “The Well-Grounded Java Developer” (Maning) and his technical articles are read by thousands of developers every month.\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003EBen is a regular speaker and educator on topics such as the Java platform, systems architecture, performance and concurrency for companies and conferences all over the world.\u003C/p\u003E",
                "Red Hat",
                new URL("https://lh3.googleusercontent.com/a-/AFdZucqTSqdIcl52deUIO4MP6RPU04VEAKtZlgxXOXhO=s96-c"),
                null,
                null,
                null,
                null
            ),
            new CfpDevSpeakerDetails(
                22346,
                "Bruno",
                "Meseguer",
                "\u003Cp\u003E\u003Cspan style=\"background-color: transparent; color: rgb(0, 0, 0);\"\u003EBruno Meseguer is a Technical Marketing Manager for Red Hat in the integration space. Before joining Red Hat in 2015, he worked as a developer and architect for over 15 years in various industries, designing and building large throughput integration platforms. He advocates for cloud-native and distributed integration best practices &amp; technologies.\u003C/span\u003E\u003C/p\u003E",
                "Red Hat",
                new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-0981259c-c8b2-41a6-a70b-77f0c8a4ecd3.png"),
                null,
                "bmeseguer",
                null,
                null
            ),
            new CfpDevSpeakerDetails(
                1,
                "Eric",
                "Deandrea",
                "Eric works for IBM",
                "IBM",
                null,
                "@edeandrea",
                "edeandrea",
                "ericdeandrea.dev",
                null
            )
        );

    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathTemplate("/api/public/speakers/search/{query}"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
            .withPathParam("query", equalTo(Company.RED_HAT.getStrVersion()))
    );

    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathTemplate("/api/public/speakers/search/{query}"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
            .withPathParam("query", equalTo(Company.IBM.getStrVersion()))
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