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
import java.util.List;
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
import com.redhat.cfpaggregator.client.cfpdev.CfpDevClientTests.ConfigTestProfile;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevTalkDetails.Keyword;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;
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
        .hasSizeGreaterThanOrEqualTo(1)
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

    assertThat(getClient().getEventDetails("portal1"))
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
            .withHeader(ClientProducer.PORTAL_NAME_HEADER, equalTo("portal1"))
    );
  }

  @Test
  void findTalksNoKeywordsNoSpeakers() throws MalformedURLException {
    this.wireMock.register(get(urlPathEqualTo("/api/public/talks"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .willReturn(jsonResponse("""
            [
              {
                "id": 40215,
                "title": "Agentic AI-driven unit test generation you can trust",
                "description": "\\u003Cstrong style=\\"color: rgb(0, 0, 0);\\"\\u003EWhat use are AI for code tools to developers if they have low levels of accuracy, test coverage and are non-deterministic in their outcomes?\\u003C/strong\\u003E\\u003Cbr\\u003E\\u003Cbr\\u003EIn the fast-evolving landscape of software development, AI-driven tools promise to revolutionize unit testing by enhancing speed, accuracy, and coverage. Yet, many developers remain skeptical, questioning the utility of AI-for-code solutions plagued by low accuracy, inadequate test coverage, and non-deterministic behavior. This presentation explores the transformative potential of Diffblue Cover, an agentic AI solution designed to overcome these challenges.\\u003Cbr\\u003E\\u003Cbr\\u003EUnlike LLM-driven generative AI coding assistants, learn how advanced AI Agents can use reinforcement learning to autonomously generate reliable, deterministic unit tests that are guaranteed to compile and run correctly every time. \\u003Cbr\\u003E\\u003Cbr\\u003EBy addressing critical developer concerns—such as trustworthiness, scalability, and integration into CI/CD pipelines. As an AI Diffblue Cover redefines unit test generation for Java codebases. \\u003Cbr\\u003E\\u003Cbr\\u003EThe talk will delve into the pitfalls of conventional AI tools and demonstrate how agentic AI can deliver higher test coverage, faster development cycles, and reduced manual effort without compromising reliability.\\u003Cbr\\u003E\\u003Cbr\\u003EJoin us as we pose the question: \\u003Cstrong style=\\"color: rgb(0, 0, 0);\\"\\u003EWhat use are AI tools if they fail to meet developers&#39; expectations for accuracy and consistency?\\u003C/strong\\u003E \\u003Cbr\\u003E\\u003Cbr\\u003E",
                "summary": "In the rapidly evolving software development landscape, AI tools promise to enhance unit testing speed and accuracy. However, skepticism remains due to issues like low accuracy and non-deterministic behavior. This presentation explores Diffblue Cover, an AI solution using reinforcement learning to generate reliable, deterministic unit tests, addressing developer concerns about trustworthiness and integration.",
                "afterVideoURL": "https://www.youtube.com/embed/1i29XN4WoQc?si=o4t-fb1DjKI5vpHZ",
                "podcastURL": null,
                "audienceLevel": "INTERMEDIATE",
                "language": null,
                "totalFavourites": null,
                "track": {
                  "id": 1701,
                  "name": "Development Practices",
                  "description": "Methodologies, developer experience, practices, testing, tools, tips and guidance to be a better developer",
                  "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_Dev-Practices-Colour-1.png"
                },
                "sessionType": {
                  "id": 951,
                  "slug": "conf50",
                  "name": "Conference",
                  "duration": 50,
                  "pause": false,
                  "invitationOnly": null,
                  "freeTickets": null,
                  "minSpeakers": 1,
                  "description": "50 minute sessions on a range of different technologies, practices and methodologies",
                  "cssColor": null,
                  "scheduleInSmallRoom": false
                },
                "speakers": [
                  {
                    "id": 40058,
                    "firstName": "Paul",
                    "lastName": "Crane",
                    "fullName": "Paul Crane",
                    "bio": "Paul Crane is a Senior Architect at Diffblue. With a passion for solving complex problems, and pushing technical boundaries he has many years experience of developing software solutions for small companies, governmental departments, and university research laboratories. He holds a PhD in Computer Science from the University of Otago, New Zealand.",
                    "anonymizedBio": "This speaker is a Senior Architect at a leading software company. With a passion for solving complex problems and pushing technical boundaries, they have extensive experience in developing software solutions for small businesses, governmental departments, and university research labs. They hold a PhD in Computer Science from a prestigious university in New Zealand.",
                    "company": "Diffblue",
                    "imageUrl": "https://lh3.googleusercontent.com/a/ACg8ocJvRGXbzqNiwQfXESt_BU0-BE1nISRwbwNWSkg3fXAgMQeElgc=s96-c",
                    "twitterHandle": "",
                    "linkedInUsername": "paulscrane/",
                    "blueskyUsername": "",
                    "mastodonUsername": "",
                    "countryName": "United Kingdom"
                  }
                ],
                "keywords": [
                  {
                    "name": "reinforcement"
                  },
                  {
                    "name": "diffblue"
                  },
                  {
                    "name": "unit"
                  },
                  {
                    "name": "deterministic"
                  }
                ],
                "timeSlots": []
              },
              {
                "id": 5551,
                "title": "Falling in Love with Platform Engineering: The CUPID Way",
                "description": "In recent years, large organizations have adopted Internal Developer Portals (IDPs) to balance governance and autonomy. Despite their promise, many platforms prioritize compliance over usability. This talk explores enhancing platform engineering through principles like product mindset, user experience, and key metrics, while questioning the optimal granularity of services in the “Golden Path.”\\u003Cbr\\u003EInspired by Dan North’s “Joyful Software”, during this talk the concept of “Joyful Engineering” and the CUPID principles are proposed to transform service building approaches. By focusing on self-containment, idempotency and single purpose, organizations can improve service acceptance and foster collaboration.\\u003Cbr\\u003EKey takeaways include:\\u003Cbr\\u003E\\u003Cul\\u003E\\u003Cli\\u003EUnderstanding why IDPs aren’t a magic solution for developer experience.\\u003C/li\\u003E\\u003Cli\\u003EFinding the balance between autonomy and governance.\\u003C/li\\u003E\\u003Cli\\u003EAdoption of Cupid Principles (Composable, Unix Philosophy, Predictable, Idiomatic, Domain-based) to Platform Engineering.\\u003C/li\\u003E\\u003Cli\\u003ELeveraging the “Everything as Code” mindset with Agentic AI to shape the future of developer experience.\\u003C/li\\u003E\\u003C/ul\\u003E",
                "summary": "This talk addresses enhancing Internal Developer Portals by balancing governance and usability through a product mindset and key metrics. It introduces \\"Joyful Engineering\\" and CUPID principles (Composable, Unix Philosophy, Predictable, Idiomatic, Domain-based) for service building, emphasizing self-containment and idempotency to boost acceptance and collaboration, while leveraging \\"Everything as Code\\" and Agentic AI.",
                "afterVideoURL": "https://www.youtube.com/embed/oGkngxEbxPg?si=we32YRwwvByE9MZX",
                "podcastURL": null,
                "audienceLevel": "INTERMEDIATE",
                "language": null,
                "totalFavourites": null,
                "track": {
                  "id": 1254,
                  "name": "Build & Deploy",
                  "description": "DevOps, cloud delivery, build pipelines, orchestration, observability, monitoring, resilience, compliance",
                  "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_BuildDeploy-Colour-1.png"
                },
                "sessionType": {
                  "id": 951,
                  "slug": "conf50",
                  "name": "Conference",
                  "duration": 50,
                  "pause": false,
                  "invitationOnly": null,
                  "freeTickets": null,
                  "minSpeakers": 1,
                  "description": "50 minute sessions on a range of different technologies, practices and methodologies",
                  "cssColor": null,
                  "scheduleInSmallRoom": false
                },
                "speakers": [
                  {
                    "id": 5501,
                    "firstName": "Makan",
                    "lastName": "Sepehrifar",
                    "fullName": "Makan Sepehrifar",
                    "bio": "Makan Sepehrifar is a software architect whose fascination with the intricacies of organizational behavior and cognitive psychology has led him to redefine the way technology interfaces with human understanding. Looking at software architecture as a craftsmanship process, he realized that the more abstract you look at it, the more complexities arises. His career has been a testament to his commitment to bridging the gap between technology and human behavior, resulting in innovative software solutions that not only work efficiently but also self-adaptive teams that embrace the mindset of agile.",
                    "anonymizedBio": "This individual is a software architect whose fascination with the intricacies of organizational behavior and cognitive psychology has led them to redefine the way technology interfaces with human understanding. Viewing software architecture as a craftsmanship process, they realized that the more abstract the perspective, the more complexities arise. Their career has been a testament to their commitment to bridging the gap between technology and human behavior, resulting in innovative software solutions that not only work efficiently but also foster self-adaptive teams that embrace an agile mindset.",
                    "company": "Code Nomads",
                    "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-176e18eb-2e7e-4cad-bb7f-5a448a1b4faf.png",
                    "twitterHandle": "@makan1869",
                    "linkedInUsername": "makan1869",
                    "blueskyUsername": null,
                    "mastodonUsername": null,
                    "countryName": "Netherlands"
                  }
                ],
                "keywords": [
                  {
                    "name": "granularity"
                  },
                  {
                    "name": "usability"
                  },
                  {
                    "name": "cupid"
                  },
                  {
                    "name": "idps"
                  }
                ],
                "timeSlots": []
              }
            ]
            """, 200)));

    assertThat(getClient().findTalks(TalkSearchCriteria.builder().build(), "portal1"))
        .isNotNull()
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            new CfpDevTalkDetails(
                40215L,
                "Agentic AI-driven unit test generation you can trust",
                "\u003Cstrong style=\"color: rgb(0, 0, 0);\"\u003EWhat use are AI for code tools to developers if they have low levels of accuracy, test coverage and are non-deterministic in their outcomes?\u003C/strong\u003E\u003Cbr\u003E\u003Cbr\u003EIn the fast-evolving landscape of software development, AI-driven tools promise to revolutionize unit testing by enhancing speed, accuracy, and coverage. Yet, many developers remain skeptical, questioning the utility of AI-for-code solutions plagued by low accuracy, inadequate test coverage, and non-deterministic behavior. This presentation explores the transformative potential of Diffblue Cover, an agentic AI solution designed to overcome these challenges.\u003Cbr\u003E\u003Cbr\u003EUnlike LLM-driven generative AI coding assistants, learn how advanced AI Agents can use reinforcement learning to autonomously generate reliable, deterministic unit tests that are guaranteed to compile and run correctly every time. \u003Cbr\u003E\u003Cbr\u003EBy addressing critical developer concerns—such as trustworthiness, scalability, and integration into CI/CD pipelines. As an AI Diffblue Cover redefines unit test generation for Java codebases. \u003Cbr\u003E\u003Cbr\u003EThe talk will delve into the pitfalls of conventional AI tools and demonstrate how agentic AI can deliver higher test coverage, faster development cycles, and reduced manual effort without compromising reliability.\u003Cbr\u003E\u003Cbr\u003EJoin us as we pose the question: \u003Cstrong style=\"color: rgb(0, 0, 0);\"\u003EWhat use are AI tools if they fail to meet developers&#39; expectations for accuracy and consistency?\u003C/strong\u003E \u003Cbr\u003E\u003Cbr\u003E",
                "In the rapidly evolving software development landscape, AI tools promise to enhance unit testing speed and accuracy. However, skepticism remains due to issues like low accuracy and non-deterministic behavior. This presentation explores Diffblue Cover, an AI solution using reinforcement learning to generate reliable, deterministic unit tests, addressing developer concerns about trustworthiness and integration.",
                new URL("https://www.youtube.com/embed/1i29XN4WoQc?si=o4t-fb1DjKI5vpHZ"),
                List.of(
                    new Keyword("reinforcement"),
                    new Keyword("diffblue"),
                    new Keyword("unit"),
                    new Keyword("deterministic")
                ),
                List.of(new CfpDevSpeakerDetails(
                        40058L,
                        "Paul",
                        "Crane",
                        "Paul Crane is a Senior Architect at Diffblue. With a passion for solving complex problems, and pushing technical boundaries he has many years experience of developing software solutions for small companies, governmental departments, and university research laboratories. He holds a PhD in Computer Science from the University of Otago, New Zealand.",
                        "Diffblue",
                        new URL("https://lh3.googleusercontent.com/a/ACg8ocJvRGXbzqNiwQfXESt_BU0-BE1nISRwbwNWSkg3fXAgMQeElgc=s96-c"),
                        null,
                        "paulscrane/",
                        null,
                        "United Kingdom"
                    )
                )
            ),
            new CfpDevTalkDetails(
                5551L,
                "Falling in Love with Platform Engineering: The CUPID Way",
                "In recent years, large organizations have adopted Internal Developer Portals (IDPs) to balance governance and autonomy. Despite their promise, many platforms prioritize compliance over usability. This talk explores enhancing platform engineering through principles like product mindset, user experience, and key metrics, while questioning the optimal granularity of services in the “Golden Path.”\u003Cbr\u003EInspired by Dan North’s “Joyful Software”, during this talk the concept of “Joyful Engineering” and the CUPID principles are proposed to transform service building approaches. By focusing on self-containment, idempotency and single purpose, organizations can improve service acceptance and foster collaboration.\u003Cbr\u003EKey takeaways include:\u003Cbr\u003E\u003Cul\u003E\u003Cli\u003EUnderstanding why IDPs aren’t a magic solution for developer experience.\u003C/li\u003E\u003Cli\u003EFinding the balance between autonomy and governance.\u003C/li\u003E\u003Cli\u003EAdoption of Cupid Principles (Composable, Unix Philosophy, Predictable, Idiomatic, Domain-based) to Platform Engineering.\u003C/li\u003E\u003Cli\u003ELeveraging the “Everything as Code” mindset with Agentic AI to shape the future of developer experience.\u003C/li\u003E\u003C/ul\u003E",
                "This talk addresses enhancing Internal Developer Portals by balancing governance and usability through a product mindset and key metrics. It introduces \"Joyful Engineering\" and CUPID principles (Composable, Unix Philosophy, Predictable, Idiomatic, Domain-based) for service building, emphasizing self-containment and idempotency to boost acceptance and collaboration, while leveraging \"Everything as Code\" and Agentic AI.",
                new URL("https://www.youtube.com/embed/oGkngxEbxPg?si=we32YRwwvByE9MZX"),
                List.of(
                    new Keyword("granularity"),
                    new Keyword("usability"),
                    new Keyword("cupid"),
                    new Keyword("idps")
                ),
                List.of(new CfpDevSpeakerDetails(
                        5501L,
                        "Makan",
                        "Sepehrifar",
                        "Makan Sepehrifar is a software architect whose fascination with the intricacies of organizational behavior and cognitive psychology has led him to redefine the way technology interfaces with human understanding. Looking at software architecture as a craftsmanship process, he realized that the more abstract you look at it, the more complexities arises. His career has been a testament to his commitment to bridging the gap between technology and human behavior, resulting in innovative software solutions that not only work efficiently but also self-adaptive teams that embrace the mindset of agile.",
                        "Code Nomads",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-176e18eb-2e7e-4cad-bb7f-5a448a1b4faf.png"),
                        "@makan1869",
                        "makan1869",
                        null,
                        "Netherlands"
                    )
                )
            )
        );

    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathEqualTo("/api/public/talks"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .withHeader(ClientProducer.PORTAL_NAME_HEADER, equalTo("portal1"))
    );
  }

  @Test
  void findTalksKeywordsNoSpeakers() throws MalformedURLException {
    setupFindTalksWithKeywords();

    var talkSearchCriteria = TalkSearchCriteria.builder()
        .talkKeywords("quarkus", "spring")
        .build();

    assertThat(getClient().findTalks(talkSearchCriteria, "portal1"))
        .isNotNull()
        .hasSize(4)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
            new CfpDevTalkDetails(
                23332L,
                "30 minutes to understand MCP (Model Context Protocol)",
                "\u003Cp\u003E\u003Cspan style=\"background-color: transparent; color: rgb(0, 0, 0);\"\u003E2023 was the year of LLMs, 2024 was the year of RAG, 2025 will be the year of MCP ! From the official documentation you can read : “The Model Context Protocol (MCP) is an open protocol that enables seamless integration between LLM applications and external data sources and tools.” But does that really help you to understand what this means ? \u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003Cspan style=\"background-color: transparent; color: rgb(0, 0, 0);\"\u003EJoin me in this Tools-in-action to understand concretely what MCP is and you will see that the concepts behind it are not really new things but more a normalization of existing ones. You will learn how to consume from a MCP server but also how to write one, using the Quarkus implementation and its wonderful developer experience. \u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003Cspan style=\"background-color: transparent; color: rgb(0, 0, 0);\"\u003EExpect a few slides and a lot of live coding ! By the end of the session you will have a complete understanding of this new buzzword of 2025 ! \u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                "MCP\nProtocol\nIntegration\nQuarkus",
                new URL("https://www.youtube.com/embed/hICGtUH7K-4?si=1AEKVT7XXbtwp2kE"),
                List.of(
                    new Keyword("mcp"),
                    new Keyword("protocol"),
                    new Keyword("quarkus"),
                    new Keyword("integration")
                ),
                List.of(new CfpDevSpeakerDetails(
                        18698L,
                        "Sébastien",
                        "Blanc",
                        "\u003Cp\u003E\u003Cspan style=\"color: rgb(0, 0, 0); background-color: rgb(255, 255, 255);\"\u003ESébastien Blanc is a Passion-Driven-Developer with one primary goal : Make the Developers Happy. He likes to share his passion by giving talks that are pragmatic, fun and focused on live coding.\u003C/span\u003E\u003C/p\u003E",
                        "Port",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-3c5b55cc-0cd4-41c4-aae0-90f4294cd04b.jpg"),
                        "sebi2706",
                        null,
                        null,
                        "France"
                    )
                )
            ),
            new CfpDevTalkDetails(
                37706L,
                "Boost Developer Productivity and Speed Up Your Inner Loop with Quarkus",
                "\u003Cp\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003EIn today’s fast-paced development world, slow builds and sluggish feedback loops can cripple productivity—but Quarkus flips the script. Designed for Kubernetes-native Java, it turbocharges the inner loop with live coding, near-instant startup times, and memory efficiency, letting developers iterate faster than ever. Imagine tweaking code and seeing changes immediately without manual redeploys, or testing cloud-native apps locally without resource bloat. This talk includes a live demo showcasing Quarkus’ live coding in action: watch as code edits reflect in real time, feedback loops shrink to seconds, and cloud integrations streamline workflows—proving how Quarkus turns waiting time into productive coding time.\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                "Quarkus enhances productivity in Java development for Kubernetes by enabling live coding, near-instant startup, and memory efficiency. It allows developers to see code changes immediately without redeploys and test cloud-native apps locally. A live demo will showcase real-time code editing and streamlined cloud integrations, reducing feedback loops to seconds.",
                new URL("https://www.youtube.com/embed/Z6BomLweo6c?si=5kIK4FUSSKpwbr5c"),
                List.of(
                    new Keyword("efficiency"),
                    new Keyword("kubernetes"),
                    new Keyword("quarkus"),
                    new Keyword("live coding")
                ),
                List.of(new CfpDevSpeakerDetails(
                        2632L,
                        "Daniel",
                        "Oh",
                        "\u003Cp\u003E\u003Cspan style=\"background-color: rgb(255, 255, 255); color: rgba(0, 0, 0, 0.9);\"\u003EJava Champion, CNCF Ambassador, Developer Advocate, Technical Marketing, International Speaker, Published Author\u003C/span\u003E\u003C/p\u003E",
                        "Red Hat",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-c547f612-71b9-4387-a478-9684944dafde.jpeg"),
                        "@danieloh30",
                        "daniel-oh-083818112",
                        "danieloh30.bsky.social",
                        "United States of America"
                    )
                )
            ),
            new CfpDevTalkDetails(
                2960L,
                "Bootiful Spring Boot: A DOGumentary",
                "\u003Cp\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003ESpring Boot 3.x and Java 21 have arrived, making it an exciting time to be a Java developer! Join me, Josh Long (@starbuxman), as we dive into the future of Spring Boot with Java 21. Discover how to scale your applications and codebases effortlessly. We&#39;ll explore the robust Spring Boot ecosystem, featuring AI, modularity, seamless data access, and cutting-edge production optimizations like Project Loom&#39;s virtual threads, GraalVM, AppCDS, and more. Let&#39;s explore the latest-and-greatest in Spring Boot to build faster, more scalable, more efficient, more modular, more secure, and more intelligent systems and services.\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                "Spring  \nBoot  \nJava  \nModularity",
                new URL("https://www.youtube.com/embed/5Z8FUurjxs4?si=-XZFcezd64IvqkS1"),
                List.of(
                    new Keyword("modularity"),
                    new Keyword("java"),
                    new Keyword("spring"),
                    new Keyword("graalvm")
                ),
                List.of(new CfpDevSpeakerDetails(
                        2615L,
                        "Josh",
                        "Long",
                        "\u003Cp\u003E\u003Ca href=\"http://twitter.com/starbuxman\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003EJosh (@starbuxman)\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E has been the first Spring Developer Advocate since 2010. Josh is a Java Champion, author of 7 books (including \u003C/span\u003E\u003Ca href=\"http://reactivespring.io/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;Reactive Spring&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E) and numerous best-selling video training (including \u003C/span\u003E\u003Ca href=\"https://www.safaribooksonline.com/library/view/building-microservices-with/9780134192468/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;Building Microservices with Spring Boot Livelessons&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E with Spring Boot co-founder Phil Webb), and an open-source contributor (Spring Boot, Spring Integration, Axon, Spring Cloud, Activiti, Vaadin, etc), a Youtuber (\u003C/span\u003E\u003Ca href=\"https://youtube.com/@coffeesoftware\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003ECoffee + Software with Josh Long\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E as well as \u003C/span\u003E\u003Ca href=\"http://bit.ly/spring-tips-playlist\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003Emy Spring Tips series \u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E), and a podcaster (\u003C/span\u003E\u003Ca href=\"http://bootifulpodcast.fm/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;A Bootiful Podcast&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E).\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                        "Broadcom",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-cb234ac6-0b89-48af-baa5-069ff22a27ec.jpg"),
                        "@starbuxman",
                        "joshlong",
                        null,
                        "United States of America"
                    )
                )
            ),
            new CfpDevTalkDetails(
                3000L,
                "Passkeys in practice: implementing passwordless apps",
                "\u003Cp\u003EPasswords. They&#39;re everywhere, they get leaked... A security nightmare! A work-around is to a delegate authentication to a third party, for example using OpenID Connect. But sometimes you can&#39;t or don&#39;t want to do that - can you still go password-less, with user-friendly flows?\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003EPasskeys, and more specifically the WebAuthN spec, is a browser-based technology that allows you to log in using physical devices, such as a Yubikey, or MacOS&#39;s TouchID or iOS&#39; FaceID. It has been well-supported by browsers for multiple years now. With this technology, we can make our apps authenticate users without a password.\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003EIn this presentation, we will discuss the basics of WebAuthN, and use the brand new support for passkeys in Spring Boot 3.4 to integrate it in an existing application.\u003C/p\u003E",
                "This presentation explores password-less authentication using the WebAuthN specification and passkeys, enabling secure login via physical devices like Yubikey, TouchID, or FaceID. It highlights integrating this technology into applications using new support in Spring Boot 3.4, providing user-friendly, password-free authentication solutions.",
                new URL("https://www.youtube.com/embed/z-Fwi-Zf0Dk?si=EwhKluN7s-zMB2fw"),
                List.of(
                    new Keyword("passkeys"),
                    new Keyword("spring boot"),
                    new Keyword("webauthn"),
                    new Keyword("authentication")
                ),
                List.of(new CfpDevSpeakerDetails(
                        2647L,
                        "Daniel",
                        "Garnier-Moiroux",
                        "\u003Cp\u003EDaniel Garnier is a software engineer on the Spring team, working on Spring Security, and more broadly in the identity space and SSO for applications. He is an adjunct professor at Mines Paris, where he teaches CS and software engineering classes.\u003C/p\u003E",
                        "Spring",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-ec6ef8f7-e2c4-4d46-b973-a9abaab5c971.jpg"),
                        "@kehrlann",
                        "garniermoiroux",
                        null,
                        "France"
                    )
                )
            )
        );

    verifyFindTalksWithKeywords();
  }

  @Test
  void findTalksNoKeywordsWithSpeakers() throws MalformedURLException {
    this.wireMock.register(get(urlPathEqualTo("/api/public/talks"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .willReturn(jsonResponse("""
            [
              {
                "id": 40215,
                "title": "Agentic AI-driven unit test generation you can trust",
                "description": "\\u003Cstrong style=\\"color: rgb(0, 0, 0);\\"\\u003EWhat use are AI for code tools to developers if they have low levels of accuracy, test coverage and are non-deterministic in their outcomes?\\u003C/strong\\u003E\\u003Cbr\\u003E\\u003Cbr\\u003EIn the fast-evolving landscape of software development, AI-driven tools promise to revolutionize unit testing by enhancing speed, accuracy, and coverage. Yet, many developers remain skeptical, questioning the utility of AI-for-code solutions plagued by low accuracy, inadequate test coverage, and non-deterministic behavior. This presentation explores the transformative potential of Diffblue Cover, an agentic AI solution designed to overcome these challenges.\\u003Cbr\\u003E\\u003Cbr\\u003EUnlike LLM-driven generative AI coding assistants, learn how advanced AI Agents can use reinforcement learning to autonomously generate reliable, deterministic unit tests that are guaranteed to compile and run correctly every time. \\u003Cbr\\u003E\\u003Cbr\\u003EBy addressing critical developer concerns—such as trustworthiness, scalability, and integration into CI/CD pipelines. As an AI Diffblue Cover redefines unit test generation for Java codebases. \\u003Cbr\\u003E\\u003Cbr\\u003EThe talk will delve into the pitfalls of conventional AI tools and demonstrate how agentic AI can deliver higher test coverage, faster development cycles, and reduced manual effort without compromising reliability.\\u003Cbr\\u003E\\u003Cbr\\u003EJoin us as we pose the question: \\u003Cstrong style=\\"color: rgb(0, 0, 0);\\"\\u003EWhat use are AI tools if they fail to meet developers&#39; expectations for accuracy and consistency?\\u003C/strong\\u003E \\u003Cbr\\u003E\\u003Cbr\\u003E",
                "summary": "In the rapidly evolving software development landscape, AI tools promise to enhance unit testing speed and accuracy. However, skepticism remains due to issues like low accuracy and non-deterministic behavior. This presentation explores Diffblue Cover, an AI solution using reinforcement learning to generate reliable, deterministic unit tests, addressing developer concerns about trustworthiness and integration.",
                "afterVideoURL": "https://www.youtube.com/embed/1i29XN4WoQc?si=o4t-fb1DjKI5vpHZ",
                "podcastURL": null,
                "audienceLevel": "INTERMEDIATE",
                "language": null,
                "totalFavourites": null,
                "track": {
                  "id": 1701,
                  "name": "Development Practices",
                  "description": "Methodologies, developer experience, practices, testing, tools, tips and guidance to be a better developer",
                  "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_Dev-Practices-Colour-1.png"
                },
                "sessionType": {
                  "id": 951,
                  "slug": "conf50",
                  "name": "Conference",
                  "duration": 50,
                  "pause": false,
                  "invitationOnly": null,
                  "freeTickets": null,
                  "minSpeakers": 1,
                  "description": "50 minute sessions on a range of different technologies, practices and methodologies",
                  "cssColor": null,
                  "scheduleInSmallRoom": false
                },
                "speakers": [
                  {
                    "id": 40058,
                    "firstName": "Paul",
                    "lastName": "Crane",
                    "fullName": "Paul Crane",
                    "bio": "Paul Crane is a Senior Architect at Diffblue. With a passion for solving complex problems, and pushing technical boundaries he has many years experience of developing software solutions for small companies, governmental departments, and university research laboratories. He holds a PhD in Computer Science from the University of Otago, New Zealand.",
                    "anonymizedBio": "This speaker is a Senior Architect at a leading software company. With a passion for solving complex problems and pushing technical boundaries, they have extensive experience in developing software solutions for small businesses, governmental departments, and university research labs. They hold a PhD in Computer Science from a prestigious university in New Zealand.",
                    "company": "Diffblue",
                    "imageUrl": "https://lh3.googleusercontent.com/a/ACg8ocJvRGXbzqNiwQfXESt_BU0-BE1nISRwbwNWSkg3fXAgMQeElgc=s96-c",
                    "twitterHandle": "",
                    "linkedInUsername": "paulscrane/",
                    "blueskyUsername": "",
                    "mastodonUsername": "",
                    "countryName": "United Kingdom"
                  }
                ],
                "keywords": [
                  {
                    "name": "reinforcement"
                  },
                  {
                    "name": "diffblue"
                  },
                  {
                    "name": "unit"
                  },
                  {
                    "name": "deterministic"
                  }
                ],
                "timeSlots": []
              },
              {
                "id": 5551,
                "title": "Falling in Love with Platform Engineering: The CUPID Way",
                "description": "In recent years, large organizations have adopted Internal Developer Portals (IDPs) to balance governance and autonomy. Despite their promise, many platforms prioritize compliance over usability. This talk explores enhancing platform engineering through principles like product mindset, user experience, and key metrics, while questioning the optimal granularity of services in the “Golden Path.”\\u003Cbr\\u003EInspired by Dan North’s “Joyful Software”, during this talk the concept of “Joyful Engineering” and the CUPID principles are proposed to transform service building approaches. By focusing on self-containment, idempotency and single purpose, organizations can improve service acceptance and foster collaboration.\\u003Cbr\\u003EKey takeaways include:\\u003Cbr\\u003E\\u003Cul\\u003E\\u003Cli\\u003EUnderstanding why IDPs aren’t a magic solution for developer experience.\\u003C/li\\u003E\\u003Cli\\u003EFinding the balance between autonomy and governance.\\u003C/li\\u003E\\u003Cli\\u003EAdoption of Cupid Principles (Composable, Unix Philosophy, Predictable, Idiomatic, Domain-based) to Platform Engineering.\\u003C/li\\u003E\\u003Cli\\u003ELeveraging the “Everything as Code” mindset with Agentic AI to shape the future of developer experience.\\u003C/li\\u003E\\u003C/ul\\u003E",
                "summary": "This talk addresses enhancing Internal Developer Portals by balancing governance and usability through a product mindset and key metrics. It introduces \\"Joyful Engineering\\" and CUPID principles (Composable, Unix Philosophy, Predictable, Idiomatic, Domain-based) for service building, emphasizing self-containment and idempotency to boost acceptance and collaboration, while leveraging \\"Everything as Code\\" and Agentic AI.",
                "afterVideoURL": "https://www.youtube.com/embed/oGkngxEbxPg?si=we32YRwwvByE9MZX",
                "podcastURL": null,
                "audienceLevel": "INTERMEDIATE",
                "language": null,
                "totalFavourites": null,
                "track": {
                  "id": 1254,
                  "name": "Build & Deploy",
                  "description": "DevOps, cloud delivery, build pipelines, orchestration, observability, monitoring, resilience, compliance",
                  "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_BuildDeploy-Colour-1.png"
                },
                "sessionType": {
                  "id": 951,
                  "slug": "conf50",
                  "name": "Conference",
                  "duration": 50,
                  "pause": false,
                  "invitationOnly": null,
                  "freeTickets": null,
                  "minSpeakers": 1,
                  "description": "50 minute sessions on a range of different technologies, practices and methodologies",
                  "cssColor": null,
                  "scheduleInSmallRoom": false
                },
                "speakers": [
                  {
                    "id": 5501,
                    "firstName": "Makan",
                    "lastName": "Sepehrifar",
                    "fullName": "Makan Sepehrifar",
                    "bio": "Makan Sepehrifar is a software architect whose fascination with the intricacies of organizational behavior and cognitive psychology has led him to redefine the way technology interfaces with human understanding. Looking at software architecture as a craftsmanship process, he realized that the more abstract you look at it, the more complexities arises. His career has been a testament to his commitment to bridging the gap between technology and human behavior, resulting in innovative software solutions that not only work efficiently but also self-adaptive teams that embrace the mindset of agile.",
                    "anonymizedBio": "This individual is a software architect whose fascination with the intricacies of organizational behavior and cognitive psychology has led them to redefine the way technology interfaces with human understanding. Viewing software architecture as a craftsmanship process, they realized that the more abstract the perspective, the more complexities arise. Their career has been a testament to their commitment to bridging the gap between technology and human behavior, resulting in innovative software solutions that not only work efficiently but also foster self-adaptive teams that embrace an agile mindset.",
                    "company": "Code Nomads",
                    "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-176e18eb-2e7e-4cad-bb7f-5a448a1b4faf.png",
                    "twitterHandle": "@makan1869",
                    "linkedInUsername": "makan1869",
                    "blueskyUsername": null,
                    "mastodonUsername": null,
                    "countryName": "Netherlands"
                  }
                ],
                "keywords": [
                  {
                    "name": "granularity"
                  },
                  {
                    "name": "usability"
                  },
                  {
                    "name": "cupid"
                  },
                  {
                    "name": "idps"
                  }
                ],
                "timeSlots": []
              },
              {
                "id": 23332,
                "title": "30 minutes to understand MCP (Model Context Protocol)",
                "description": "\\u003Cp\\u003E\\u003Cspan style=\\"background-color: transparent; color: rgb(0, 0, 0);\\"\\u003E2023 was the year of LLMs, 2024 was the year of RAG, 2025 will be the year of MCP ! From the official documentation you can read : “The Model Context Protocol (MCP) is an open protocol that enables seamless integration between LLM applications and external data sources and tools.” But does that really help you to understand what this means ? \\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003Cspan style=\\"background-color: transparent; color: rgb(0, 0, 0);\\"\\u003EJoin me in this Tools-in-action to understand concretely what MCP is and you will see that the concepts behind it are not really new things but more a normalization of existing ones. You will learn how to consume from a MCP server but also how to write one, using the Quarkus implementation and its wonderful developer experience. \\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003Cspan style=\\"background-color: transparent; color: rgb(0, 0, 0);\\"\\u003EExpect a few slides and a lot of live coding ! By the end of the session you will have a complete understanding of this new buzzword of 2025 ! \\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E",
                "summary": "MCP\\nProtocol\\nIntegration\\nQuarkus",
                "afterVideoURL": "https://www.youtube.com/embed/hICGtUH7K-4?si=1AEKVT7XXbtwp2kE",
                "podcastURL": null,
                "audienceLevel": "BEGINNER",
                "language": null,
                "totalFavourites": null,
                "track": {
                  "id": 1252,
                  "name": "Data & AI",
                  "description": "Data at Scale, High Load, Data Science, Generative AI, LLM, Analytics, ML, Deep Learning, Neural Networks, Automation",
                  "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_Data-AI-Colour-1.png"
                },
                "sessionType": {
                  "id": 957,
                  "slug": "tia30",
                  "name": "Tools-in-Action",
                  "duration": 30,
                  "pause": false,
                  "invitationOnly": null,
                  "freeTickets": null,
                  "minSpeakers": 1,
                  "description": "30 minute sessions focused on demonstrating technical development tools or solutions",
                  "cssColor": null,
                  "scheduleInSmallRoom": false
                },
                "speakers": [
                  {
                    "id": 18698,
                    "firstName": "Sébastien",
                    "lastName": "Blanc",
                    "fullName": "Sébastien Blanc",
                    "bio": "\\u003Cp\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0); background-color: rgb(255, 255, 255);\\"\\u003ESébastien Blanc is a Passion-Driven-Developer with one primary goal : Make the Developers Happy. He likes to share his passion by giving talks that are pragmatic, fun and focused on live coding.\\u003C/span\\u003E\\u003C/p\\u003E",
                    "anonymizedBio": "This speaker is a passion-driven developer with one primary goal: to make developers happy. They enjoy sharing their enthusiasm by giving talks that are pragmatic, fun, and focused on live coding.",
                    "company": "Port",
                    "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-3c5b55cc-0cd4-41c4-aae0-90f4294cd04b.jpg",
                    "twitterHandle": "sebi2706",
                    "linkedInUsername": "",
                    "blueskyUsername": "",
                    "mastodonUsername": "",
                    "countryName": "France"
                  }
                ],
                "keywords": [
                  {
                    "name": "mcp"
                  },
                  {
                    "name": "protocol"
                  },
                  {
                    "name": "quarkus"
                  },
                  {
                    "name": "integration"
                  }
                ],
                "timeSlots": []
              },
              {
                "id": 37706,
                "title": "Boost Developer Productivity and Speed Up Your Inner Loop with Quarkus",
                "description": "\\u003Cp\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003EIn today’s fast-paced development world, slow builds and sluggish feedback loops can cripple productivity—but Quarkus flips the script. Designed for Kubernetes-native Java, it turbocharges the inner loop with live coding, near-instant startup times, and memory efficiency, letting developers iterate faster than ever. Imagine tweaking code and seeing changes immediately without manual redeploys, or testing cloud-native apps locally without resource bloat. This talk includes a live demo showcasing Quarkus’ live coding in action: watch as code edits reflect in real time, feedback loops shrink to seconds, and cloud integrations streamline workflows—proving how Quarkus turns waiting time into productive coding time.\\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E",
                "summary": "Quarkus enhances productivity in Java development for Kubernetes by enabling live coding, near-instant startup, and memory efficiency. It allows developers to see code changes immediately without redeploys and test cloud-native apps locally. A live demo will showcase real-time code editing and streamlined cloud integrations, reducing feedback loops to seconds.",
                "afterVideoURL": "https://www.youtube.com/embed/Z6BomLweo6c?si=5kIK4FUSSKpwbr5c",
                "podcastURL": null,
                "audienceLevel": "BEGINNER",
                "language": null,
                "totalFavourites": null,
                "track": {
                  "id": 39651,
                  "name": "DevNation Day",
                  "description": "Specialized content from Red Hat Developer and friends, exploring the cutting edges of Java, AI, Quarkus, Kafka, and more.",
                  "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2025/02/devnation-day-icon-only.png"
                },
                "sessionType": {
                  "id": 1451,
                  "slug": "byte",
                  "name": "Byte Size Session",
                  "duration": 15,
                  "pause": false,
                  "invitationOnly": null,
                  "freeTickets": null,
                  "minSpeakers": 1,
                  "description": "15 minute sessions designed for speakers to give a quickfire view of a subject (scheduled right before or right after the lunch break)",
                  "cssColor": null,
                  "scheduleInSmallRoom": false
                },
                "speakers": [
                  {
                    "id": 2632,
                    "firstName": "Daniel",
                    "lastName": "Oh",
                    "fullName": "Daniel Oh",
                    "bio": "\\u003Cp\\u003E\\u003Cspan style=\\"background-color: rgb(255, 255, 255); color: rgba(0, 0, 0, 0.9);\\"\\u003EJava Champion, CNCF Ambassador, Developer Advocate, Technical Marketing, International Speaker, Published Author\\u003C/span\\u003E\\u003C/p\\u003E",
                    "anonymizedBio": "Experienced Java expert, cloud-native technology advocate, seasoned developer evangelist, technical marketing specialist, global presenter, and published author.",
                    "company": "Red Hat",
                    "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-c547f612-71b9-4387-a478-9684944dafde.jpeg",
                    "twitterHandle": "@danieloh30",
                    "linkedInUsername": "daniel-oh-083818112",
                    "blueskyUsername": "danieloh30.bsky.social",
                    "mastodonUsername": "",
                    "countryName": "United States of America"
                  }
                ],
                "keywords": [
                  {
                    "name": "efficiency"
                  },
                  {
                    "name": "kubernetes"
                  },
                  {
                    "name": "quarkus"
                  },
                  {
                    "name": "live coding"
                  }
                ],
                "timeSlots": []
              },
              {
                 "id": 2960,
                 "title": "Bootiful Spring Boot: A DOGumentary",
                 "description": "\\u003Cp\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003ESpring Boot 3.x and Java 21 have arrived, making it an exciting time to be a Java developer! Join me, Josh Long (@starbuxman), as we dive into the future of Spring Boot with Java 21. Discover how to scale your applications and codebases effortlessly. We&#39;ll explore the robust Spring Boot ecosystem, featuring AI, modularity, seamless data access, and cutting-edge production optimizations like Project Loom&#39;s virtual threads, GraalVM, AppCDS, and more. Let&#39;s explore the latest-and-greatest in Spring Boot to build faster, more scalable, more efficient, more modular, more secure, and more intelligent systems and services.\\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E",
                 "summary": "Spring  \\nBoot  \\nJava  \\nModularity",
                 "afterVideoURL": "https://www.youtube.com/embed/5Z8FUurjxs4?si=-XZFcezd64IvqkS1",
                 "podcastURL": null,
                 "audienceLevel": "BEGINNER",
                 "language": null,
                 "totalFavourites": null,
                 "track": {
                   "id": 2754,
                   "name": "Java",
                   "description": "Java as a platform, Java language, JVM, JDK, frameworks, app servers, performance, tools, standards…",
                   "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_Java-Colour-1.png"
                 },
                 "sessionType": {
                   "id": 951,
                   "slug": "conf50",
                   "name": "Conference",
                   "duration": 50,
                   "pause": false,
                   "invitationOnly": null,
                   "freeTickets": null,
                   "minSpeakers": 1,
                   "description": "50 minute sessions on a range of different technologies, practices and methodologies",
                   "cssColor": null,
                   "scheduleInSmallRoom": false
                 },
                 "speakers": [
                   {
                     "id": 2615,
                     "firstName": "Josh",
                     "lastName": "Long",
                     "fullName": "Josh Long",
                     "bio": "\\u003Cp\\u003E\\u003Ca href=\\"http://twitter.com/starbuxman\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003EJosh (@starbuxman)\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E has been the first Spring Developer Advocate since 2010. Josh is a Java Champion, author of 7 books (including \\u003C/span\\u003E\\u003Ca href=\\"http://reactivespring.io/\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003E&quot;Reactive Spring&quot;\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E) and numerous best-selling video training (including \\u003C/span\\u003E\\u003Ca href=\\"https://www.safaribooksonline.com/library/view/building-microservices-with/9780134192468/\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003E&quot;Building Microservices with Spring Boot Livelessons&quot;\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E with Spring Boot co-founder Phil Webb), and an open-source contributor (Spring Boot, Spring Integration, Axon, Spring Cloud, Activiti, Vaadin, etc), a Youtuber (\\u003C/span\\u003E\\u003Ca href=\\"https://youtube.com/@coffeesoftware\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003ECoffee + Software with Josh Long\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E as well as \\u003C/span\\u003E\\u003Ca href=\\"http://bit.ly/spring-tips-playlist\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003Emy Spring Tips series \\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E), and a podcaster (\\u003C/span\\u003E\\u003Ca href=\\"http://bootifulpodcast.fm/\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003E&quot;A Bootiful Podcast&quot;\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E).\\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E",
                     "anonymizedBio": "This individual has been the first Spring Developer Advocate since 2010. They are a recognized Java Champion, author of seven books, including one on Reactive Spring, and have created numerous best-selling video training courses, such as one in collaboration with a co-founder of Spring Boot. As an open-source contributor, they have worked on projects like Spring Boot, Spring Integration, Axon, Spring Cloud, Activiti, and Vaadin. Additionally, they are active in the online community as a YouTuber with series like \\"Coffee + Software\\" and \\"Spring Tips,\\" and as a podcaster with \\"A Bootiful Podcast.\\"",
                     "company": "Broadcom",
                     "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-cb234ac6-0b89-48af-baa5-069ff22a27ec.jpg",
                     "twitterHandle": "@starbuxman",
                     "linkedInUsername": "joshlong",
                     "blueskyUsername": null,
                     "mastodonUsername": null,
                     "countryName": "United States of America"
                   }
                 ],
                 "keywords": [
                   {
                     "name": "modularity"
                   },
                   {
                     "name": "java"
                   },
                   {
                     "name": "spring"
                   },
                   {
                     "name": "graalvm"
                   }
                 ],
                 "timeSlots": []
               },
               {
                 "id": 3000,
                 "title": "Passkeys in practice: implementing passwordless apps",
                 "description": "\\u003Cp\\u003EPasswords. They&#39;re everywhere, they get leaked... A security nightmare! A work-around is to a delegate authentication to a third party, for example using OpenID Connect. But sometimes you can&#39;t or don&#39;t want to do that - can you still go password-less, with user-friendly flows?\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EPasskeys, and more specifically the WebAuthN spec, is a browser-based technology that allows you to log in using physical devices, such as a Yubikey, or MacOS&#39;s TouchID or iOS&#39; FaceID. It has been well-supported by browsers for multiple years now. With this technology, we can make our apps authenticate users without a password.\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EIn this presentation, we will discuss the basics of WebAuthN, and use the brand new support for passkeys in Spring Boot 3.4 to integrate it in an existing application.\\u003C/p\\u003E",
                 "summary": "This presentation explores password-less authentication using the WebAuthN specification and passkeys, enabling secure login via physical devices like Yubikey, TouchID, or FaceID. It highlights integrating this technology into applications using new support in Spring Boot 3.4, providing user-friendly, password-free authentication solutions.",
                 "afterVideoURL": "https://www.youtube.com/embed/z-Fwi-Zf0Dk?si=EwhKluN7s-zMB2fw",
                 "podcastURL": null,
                 "audienceLevel": "INTERMEDIATE",
                 "language": null,
                 "totalFavourites": null,
                 "track": {
                   "id": 1255,
                   "name": "Security",
                   "description": "Defensive practices, tools and technologies to be secure                                                                                ",
                   "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_Security-Colour-1.png"
                 },
                 "sessionType": {
                   "id": 951,
                   "slug": "conf50",
                   "name": "Conference",
                   "duration": 50,
                   "pause": false,
                   "invitationOnly": null,
                   "freeTickets": null,
                   "minSpeakers": 1,
                   "description": "50 minute sessions on a range of different technologies, practices and methodologies",
                   "cssColor": null,
                   "scheduleInSmallRoom": false
                 },
                 "speakers": [
                   {
                     "id": 2647,
                     "firstName": "Daniel",
                     "lastName": "Garnier-Moiroux",
                     "fullName": "Daniel Garnier-Moiroux",
                     "bio": "\\u003Cp\\u003EDaniel Garnier is a software engineer on the Spring team, working on Spring Security, and more broadly in the identity space and SSO for applications. He is an adjunct professor at Mines Paris, where he teaches CS and software engineering classes.\\u003C/p\\u003E",
                     "anonymizedBio": "This speaker is a software engineer on a team focused on Spring, working specifically on Spring Security and more broadly in the identity space and SSO for applications. They are also an adjunct professor at a university in Paris, where they teach computer science and software engineering courses.",
                     "company": "Spring",
                     "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-ec6ef8f7-e2c4-4d46-b973-a9abaab5c971.jpg",
                     "twitterHandle": "@kehrlann",
                     "linkedInUsername": "garniermoiroux",
                     "blueskyUsername": "",
                     "mastodonUsername": "",
                     "countryName": "France"
                   }
                 ],
                 "keywords": [
                   {
                     "name": "passkeys"
                   },
                   {
                     "name": "spring boot"
                   },
                   {
                     "name": "webauthn"
                   },
                   {
                     "name": "authentication"
                   }
                 ],
                 "timeSlots": []
               }
            ]
            """, 200)));

    var talkSearchCriteria = TalkSearchCriteria.builder()
        .speakerCompanies("Red Hat", "Broadcom")
        .build();

    assertThat(getClient().findTalks(talkSearchCriteria, "portal1"))
        .isNotNull()
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
            new CfpDevTalkDetails(
                37706L,
                "Boost Developer Productivity and Speed Up Your Inner Loop with Quarkus",
                "\u003Cp\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003EIn today’s fast-paced development world, slow builds and sluggish feedback loops can cripple productivity—but Quarkus flips the script. Designed for Kubernetes-native Java, it turbocharges the inner loop with live coding, near-instant startup times, and memory efficiency, letting developers iterate faster than ever. Imagine tweaking code and seeing changes immediately without manual redeploys, or testing cloud-native apps locally without resource bloat. This talk includes a live demo showcasing Quarkus’ live coding in action: watch as code edits reflect in real time, feedback loops shrink to seconds, and cloud integrations streamline workflows—proving how Quarkus turns waiting time into productive coding time.\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                "Quarkus enhances productivity in Java development for Kubernetes by enabling live coding, near-instant startup, and memory efficiency. It allows developers to see code changes immediately without redeploys and test cloud-native apps locally. A live demo will showcase real-time code editing and streamlined cloud integrations, reducing feedback loops to seconds.",
                new URL("https://www.youtube.com/embed/Z6BomLweo6c?si=5kIK4FUSSKpwbr5c"),
                List.of(
                    new Keyword("efficiency"),
                    new Keyword("kubernetes"),
                    new Keyword("quarkus"),
                    new Keyword("live coding")
                ),
                List.of(new CfpDevSpeakerDetails(
                        2632L,
                        "Daniel",
                        "Oh",
                        "\u003Cp\u003E\u003Cspan style=\"background-color: rgb(255, 255, 255); color: rgba(0, 0, 0, 0.9);\"\u003EJava Champion, CNCF Ambassador, Developer Advocate, Technical Marketing, International Speaker, Published Author\u003C/span\u003E\u003C/p\u003E",
                        "Red Hat",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-c547f612-71b9-4387-a478-9684944dafde.jpeg"),
                        "@danieloh30",
                        "daniel-oh-083818112",
                        "danieloh30.bsky.social",
                        "United States of America"
                    )
                )
            ),
            new CfpDevTalkDetails(
                2960L,
                "Bootiful Spring Boot: A DOGumentary",
                "\u003Cp\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003ESpring Boot 3.x and Java 21 have arrived, making it an exciting time to be a Java developer! Join me, Josh Long (@starbuxman), as we dive into the future of Spring Boot with Java 21. Discover how to scale your applications and codebases effortlessly. We&#39;ll explore the robust Spring Boot ecosystem, featuring AI, modularity, seamless data access, and cutting-edge production optimizations like Project Loom&#39;s virtual threads, GraalVM, AppCDS, and more. Let&#39;s explore the latest-and-greatest in Spring Boot to build faster, more scalable, more efficient, more modular, more secure, and more intelligent systems and services.\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                "Spring  \nBoot  \nJava  \nModularity",
                new URL("https://www.youtube.com/embed/5Z8FUurjxs4?si=-XZFcezd64IvqkS1"),
                List.of(
                    new Keyword("modularity"),
                    new Keyword("java"),
                    new Keyword("spring"),
                    new Keyword("graalvm")
                ),
                List.of(new CfpDevSpeakerDetails(
                        2615L,
                        "Josh",
                        "Long",
                        "\u003Cp\u003E\u003Ca href=\"http://twitter.com/starbuxman\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003EJosh (@starbuxman)\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E has been the first Spring Developer Advocate since 2010. Josh is a Java Champion, author of 7 books (including \u003C/span\u003E\u003Ca href=\"http://reactivespring.io/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;Reactive Spring&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E) and numerous best-selling video training (including \u003C/span\u003E\u003Ca href=\"https://www.safaribooksonline.com/library/view/building-microservices-with/9780134192468/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;Building Microservices with Spring Boot Livelessons&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E with Spring Boot co-founder Phil Webb), and an open-source contributor (Spring Boot, Spring Integration, Axon, Spring Cloud, Activiti, Vaadin, etc), a Youtuber (\u003C/span\u003E\u003Ca href=\"https://youtube.com/@coffeesoftware\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003ECoffee + Software with Josh Long\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E as well as \u003C/span\u003E\u003Ca href=\"http://bit.ly/spring-tips-playlist\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003Emy Spring Tips series \u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E), and a podcaster (\u003C/span\u003E\u003Ca href=\"http://bootifulpodcast.fm/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;A Bootiful Podcast&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E).\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                        "Broadcom",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-cb234ac6-0b89-48af-baa5-069ff22a27ec.jpg"),
                        "@starbuxman",
                        "joshlong",
                        null,
                        "United States of America"
                    )
                )
            )
        );

    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathEqualTo("/api/public/talks"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
            .withHeader(ClientProducer.PORTAL_NAME_HEADER, equalTo("portal1"))
    );
  }

  @Test
  void findTalksWithKeywordsAndSpeakers() throws MalformedURLException {
    setupFindTalksWithKeywords();

    var talkSearchCriteria = TalkSearchCriteria.builder()
        .talkKeywords("quarkus", "spring")
        .speakerCompanies("Red Hat", "Broadcom")
        .build();

    assertThat(getClient().findTalks(talkSearchCriteria, "portal1"))
        .isNotNull()
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
            new CfpDevTalkDetails(
                37706L,
                "Boost Developer Productivity and Speed Up Your Inner Loop with Quarkus",
                "\u003Cp\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003EIn today’s fast-paced development world, slow builds and sluggish feedback loops can cripple productivity—but Quarkus flips the script. Designed for Kubernetes-native Java, it turbocharges the inner loop with live coding, near-instant startup times, and memory efficiency, letting developers iterate faster than ever. Imagine tweaking code and seeing changes immediately without manual redeploys, or testing cloud-native apps locally without resource bloat. This talk includes a live demo showcasing Quarkus’ live coding in action: watch as code edits reflect in real time, feedback loops shrink to seconds, and cloud integrations streamline workflows—proving how Quarkus turns waiting time into productive coding time.\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                "Quarkus enhances productivity in Java development for Kubernetes by enabling live coding, near-instant startup, and memory efficiency. It allows developers to see code changes immediately without redeploys and test cloud-native apps locally. A live demo will showcase real-time code editing and streamlined cloud integrations, reducing feedback loops to seconds.",
                new URL("https://www.youtube.com/embed/Z6BomLweo6c?si=5kIK4FUSSKpwbr5c"),
                List.of(
                    new Keyword("efficiency"),
                    new Keyword("kubernetes"),
                    new Keyword("quarkus"),
                    new Keyword("live coding")
                ),
                List.of(new CfpDevSpeakerDetails(
                        2632L,
                        "Daniel",
                        "Oh",
                        "\u003Cp\u003E\u003Cspan style=\"background-color: rgb(255, 255, 255); color: rgba(0, 0, 0, 0.9);\"\u003EJava Champion, CNCF Ambassador, Developer Advocate, Technical Marketing, International Speaker, Published Author\u003C/span\u003E\u003C/p\u003E",
                        "Red Hat",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-c547f612-71b9-4387-a478-9684944dafde.jpeg"),
                        "@danieloh30",
                        "daniel-oh-083818112",
                        "danieloh30.bsky.social",
                        "United States of America"
                    )
                )
            ),
            new CfpDevTalkDetails(
                2960L,
                "Bootiful Spring Boot: A DOGumentary",
                "\u003Cp\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003ESpring Boot 3.x and Java 21 have arrived, making it an exciting time to be a Java developer! Join me, Josh Long (@starbuxman), as we dive into the future of Spring Boot with Java 21. Discover how to scale your applications and codebases effortlessly. We&#39;ll explore the robust Spring Boot ecosystem, featuring AI, modularity, seamless data access, and cutting-edge production optimizations like Project Loom&#39;s virtual threads, GraalVM, AppCDS, and more. Let&#39;s explore the latest-and-greatest in Spring Boot to build faster, more scalable, more efficient, more modular, more secure, and more intelligent systems and services.\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                "Spring  \nBoot  \nJava  \nModularity",
                new URL("https://www.youtube.com/embed/5Z8FUurjxs4?si=-XZFcezd64IvqkS1"),
                List.of(
                    new Keyword("modularity"),
                    new Keyword("java"),
                    new Keyword("spring"),
                    new Keyword("graalvm")
                ),
                List.of(new CfpDevSpeakerDetails(
                        2615L,
                        "Josh",
                        "Long",
                        "\u003Cp\u003E\u003Ca href=\"http://twitter.com/starbuxman\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003EJosh (@starbuxman)\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E has been the first Spring Developer Advocate since 2010. Josh is a Java Champion, author of 7 books (including \u003C/span\u003E\u003Ca href=\"http://reactivespring.io/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;Reactive Spring&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E) and numerous best-selling video training (including \u003C/span\u003E\u003Ca href=\"https://www.safaribooksonline.com/library/view/building-microservices-with/9780134192468/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;Building Microservices with Spring Boot Livelessons&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E with Spring Boot co-founder Phil Webb), and an open-source contributor (Spring Boot, Spring Integration, Axon, Spring Cloud, Activiti, Vaadin, etc), a Youtuber (\u003C/span\u003E\u003Ca href=\"https://youtube.com/@coffeesoftware\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003ECoffee + Software with Josh Long\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E as well as \u003C/span\u003E\u003Ca href=\"http://bit.ly/spring-tips-playlist\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003Emy Spring Tips series \u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E), and a podcaster (\u003C/span\u003E\u003Ca href=\"http://bootifulpodcast.fm/\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: var(--gray-400);\"\u003E&quot;A Bootiful Podcast&quot;\u003C/a\u003E\u003Cspan style=\"color: rgb(0, 0, 0);\"\u003E).\u003C/span\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E\u003Cp\u003E\u003C/p\u003E",
                        "Broadcom",
                        new URL("https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-cb234ac6-0b89-48af-baa5-069ff22a27ec.jpg"),
                        "@starbuxman",
                        "joshlong",
                        null,
                        "United States of America"
                    )
                )
            )
        );
  }

  private void setupFindTalksWithKeywords() {
    this.wireMock.register(get(urlPathTemplate("/api/public/search/{searchQuery}"))
        .withPathParam("searchQuery", equalTo("quarkus"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .willReturn(jsonResponse("""
            {
              "searchQuery": "quarkus",
              "proposals": [
                {
                  "id": 23332,
                  "title": "30 minutes to understand MCP (Model Context Protocol)",
                  "description": "\\u003Cp\\u003E\\u003Cspan style=\\"background-color: transparent; color: rgb(0, 0, 0);\\"\\u003E2023 was the year of LLMs, 2024 was the year of RAG, 2025 will be the year of MCP ! From the official documentation you can read : “The Model Context Protocol (MCP) is an open protocol that enables seamless integration between LLM applications and external data sources and tools.” But does that really help you to understand what this means ? \\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003Cspan style=\\"background-color: transparent; color: rgb(0, 0, 0);\\"\\u003EJoin me in this Tools-in-action to understand concretely what MCP is and you will see that the concepts behind it are not really new things but more a normalization of existing ones. You will learn how to consume from a MCP server but also how to write one, using the Quarkus implementation and its wonderful developer experience. \\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003Cspan style=\\"background-color: transparent; color: rgb(0, 0, 0);\\"\\u003EExpect a few slides and a lot of live coding ! By the end of the session you will have a complete understanding of this new buzzword of 2025 ! \\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E",
                  "summary": "MCP\\nProtocol\\nIntegration\\nQuarkus",
                  "afterVideoURL": "https://www.youtube.com/embed/hICGtUH7K-4?si=1AEKVT7XXbtwp2kE",
                  "podcastURL": null,
                  "audienceLevel": "BEGINNER",
                  "language": null,
                  "totalFavourites": null,
                  "track": {
                    "id": 1252,
                    "name": "Data & AI",
                    "description": "Data at Scale, High Load, Data Science, Generative AI, LLM, Analytics, ML, Deep Learning, Neural Networks, Automation",
                    "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_Data-AI-Colour-1.png"
                  },
                  "sessionType": {
                    "id": 957,
                    "slug": "tia30",
                    "name": "Tools-in-Action",
                    "duration": 30,
                    "pause": false,
                    "invitationOnly": null,
                    "freeTickets": null,
                    "minSpeakers": 1,
                    "description": "30 minute sessions focused on demonstrating technical development tools or solutions",
                    "cssColor": null,
                    "scheduleInSmallRoom": false
                  },
                  "speakers": [
                    {
                      "id": 18698,
                      "firstName": "Sébastien",
                      "lastName": "Blanc",
                      "fullName": "Sébastien Blanc",
                      "bio": "\\u003Cp\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0); background-color: rgb(255, 255, 255);\\"\\u003ESébastien Blanc is a Passion-Driven-Developer with one primary goal : Make the Developers Happy. He likes to share his passion by giving talks that are pragmatic, fun and focused on live coding.\\u003C/span\\u003E\\u003C/p\\u003E",
                      "anonymizedBio": "This speaker is a passion-driven developer with one primary goal: to make developers happy. They enjoy sharing their enthusiasm by giving talks that are pragmatic, fun, and focused on live coding.",
                      "company": "Port",
                      "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-3c5b55cc-0cd4-41c4-aae0-90f4294cd04b.jpg",
                      "twitterHandle": "sebi2706",
                      "linkedInUsername": "",
                      "blueskyUsername": "",
                      "mastodonUsername": "",
                      "countryName": "France"
                    }
                  ],
                  "keywords": [
                    {
                      "name": "mcp"
                    },
                    {
                      "name": "protocol"
                    },
                    {
                      "name": "quarkus"
                    },
                    {
                      "name": "integration"
                    }
                  ],
                  "timeSlots": []
                },
                {
                  "id": 37706,
                  "title": "Boost Developer Productivity and Speed Up Your Inner Loop with Quarkus",
                  "description": "\\u003Cp\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003EIn today’s fast-paced development world, slow builds and sluggish feedback loops can cripple productivity—but Quarkus flips the script. Designed for Kubernetes-native Java, it turbocharges the inner loop with live coding, near-instant startup times, and memory efficiency, letting developers iterate faster than ever. Imagine tweaking code and seeing changes immediately without manual redeploys, or testing cloud-native apps locally without resource bloat. This talk includes a live demo showcasing Quarkus’ live coding in action: watch as code edits reflect in real time, feedback loops shrink to seconds, and cloud integrations streamline workflows—proving how Quarkus turns waiting time into productive coding time.\\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E",
                  "summary": "Quarkus enhances productivity in Java development for Kubernetes by enabling live coding, near-instant startup, and memory efficiency. It allows developers to see code changes immediately without redeploys and test cloud-native apps locally. A live demo will showcase real-time code editing and streamlined cloud integrations, reducing feedback loops to seconds.",
                  "afterVideoURL": "https://www.youtube.com/embed/Z6BomLweo6c?si=5kIK4FUSSKpwbr5c",
                  "podcastURL": null,
                  "audienceLevel": "BEGINNER",
                  "language": null,
                  "totalFavourites": null,
                  "track": {
                    "id": 39651,
                    "name": "DevNation Day",
                    "description": "Specialized content from Red Hat Developer and friends, exploring the cutting edges of Java, AI, Quarkus, Kafka, and more.",
                    "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2025/02/devnation-day-icon-only.png"
                  },
                  "sessionType": {
                    "id": 1451,
                    "slug": "byte",
                    "name": "Byte Size Session",
                    "duration": 15,
                    "pause": false,
                    "invitationOnly": null,
                    "freeTickets": null,
                    "minSpeakers": 1,
                    "description": "15 minute sessions designed for speakers to give a quickfire view of a subject (scheduled right before or right after the lunch break)",
                    "cssColor": null,
                    "scheduleInSmallRoom": false
                  },
                  "speakers": [
                    {
                      "id": 2632,
                      "firstName": "Daniel",
                      "lastName": "Oh",
                      "fullName": "Daniel Oh",
                      "bio": "\\u003Cp\\u003E\\u003Cspan style=\\"background-color: rgb(255, 255, 255); color: rgba(0, 0, 0, 0.9);\\"\\u003EJava Champion, CNCF Ambassador, Developer Advocate, Technical Marketing, International Speaker, Published Author\\u003C/span\\u003E\\u003C/p\\u003E",
                      "anonymizedBio": "Experienced Java expert, cloud-native technology advocate, seasoned developer evangelist, technical marketing specialist, global presenter, and published author.",
                      "company": "Red Hat",
                      "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-c547f612-71b9-4387-a478-9684944dafde.jpeg",
                      "twitterHandle": "@danieloh30",
                      "linkedInUsername": "daniel-oh-083818112",
                      "blueskyUsername": "danieloh30.bsky.social",
                      "mastodonUsername": "",
                      "countryName": "United States of America"
                    }
                  ],
                  "keywords": [
                    {
                      "name": "efficiency"
                    },
                    {
                      "name": "kubernetes"
                    },
                    {
                      "name": "quarkus"
                    },
                    {
                      "name": "live coding"
                    }
                  ],
                  "timeSlots": []
                }
              ]            
            }
            """, 200)));

    this.wireMock.register(get(urlPathTemplate("/api/public/search/{searchQuery}"))
        .withPathParam("searchQuery", equalTo("spring"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .willReturn(jsonResponse("""
            {
              "searchQuery": "spring",
              "proposals": [
                {
                   "id": 2960,
                   "title": "Bootiful Spring Boot: A DOGumentary",
                   "description": "\\u003Cp\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003ESpring Boot 3.x and Java 21 have arrived, making it an exciting time to be a Java developer! Join me, Josh Long (@starbuxman), as we dive into the future of Spring Boot with Java 21. Discover how to scale your applications and codebases effortlessly. We&#39;ll explore the robust Spring Boot ecosystem, featuring AI, modularity, seamless data access, and cutting-edge production optimizations like Project Loom&#39;s virtual threads, GraalVM, AppCDS, and more. Let&#39;s explore the latest-and-greatest in Spring Boot to build faster, more scalable, more efficient, more modular, more secure, and more intelligent systems and services.\\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E",
                   "summary": "Spring  \\nBoot  \\nJava  \\nModularity",
                   "afterVideoURL": "https://www.youtube.com/embed/5Z8FUurjxs4?si=-XZFcezd64IvqkS1",
                   "podcastURL": null,
                   "audienceLevel": "BEGINNER",
                   "language": null,
                   "totalFavourites": null,
                   "track": {
                     "id": 2754,
                     "name": "Java",
                     "description": "Java as a platform, Java language, JVM, JDK, frameworks, app servers, performance, tools, standards…",
                     "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_Java-Colour-1.png"
                   },
                   "sessionType": {
                     "id": 951,
                     "slug": "conf50",
                     "name": "Conference",
                     "duration": 50,
                     "pause": false,
                     "invitationOnly": null,
                     "freeTickets": null,
                     "minSpeakers": 1,
                     "description": "50 minute sessions on a range of different technologies, practices and methodologies",
                     "cssColor": null,
                     "scheduleInSmallRoom": false
                   },
                   "speakers": [
                     {
                       "id": 2615,
                       "firstName": "Josh",
                       "lastName": "Long",
                       "fullName": "Josh Long",
                       "bio": "\\u003Cp\\u003E\\u003Ca href=\\"http://twitter.com/starbuxman\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003EJosh (@starbuxman)\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E has been the first Spring Developer Advocate since 2010. Josh is a Java Champion, author of 7 books (including \\u003C/span\\u003E\\u003Ca href=\\"http://reactivespring.io/\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003E&quot;Reactive Spring&quot;\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E) and numerous best-selling video training (including \\u003C/span\\u003E\\u003Ca href=\\"https://www.safaribooksonline.com/library/view/building-microservices-with/9780134192468/\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003E&quot;Building Microservices with Spring Boot Livelessons&quot;\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E with Spring Boot co-founder Phil Webb), and an open-source contributor (Spring Boot, Spring Integration, Axon, Spring Cloud, Activiti, Vaadin, etc), a Youtuber (\\u003C/span\\u003E\\u003Ca href=\\"https://youtube.com/@coffeesoftware\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003ECoffee + Software with Josh Long\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E as well as \\u003C/span\\u003E\\u003Ca href=\\"http://bit.ly/spring-tips-playlist\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003Emy Spring Tips series \\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E), and a podcaster (\\u003C/span\\u003E\\u003Ca href=\\"http://bootifulpodcast.fm/\\" rel=\\"noopener noreferrer\\" target=\\"_blank\\" style=\\"color: var(--gray-400);\\"\\u003E&quot;A Bootiful Podcast&quot;\\u003C/a\\u003E\\u003Cspan style=\\"color: rgb(0, 0, 0);\\"\\u003E).\\u003C/span\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E",
                       "anonymizedBio": "This individual has been the first Spring Developer Advocate since 2010. They are a recognized Java Champion, author of seven books, including one on Reactive Spring, and have created numerous best-selling video training courses, such as one in collaboration with a co-founder of Spring Boot. As an open-source contributor, they have worked on projects like Spring Boot, Spring Integration, Axon, Spring Cloud, Activiti, and Vaadin. Additionally, they are active in the online community as a YouTuber with series like \\"Coffee + Software\\" and \\"Spring Tips,\\" and as a podcaster with \\"A Bootiful Podcast.\\"",
                       "company": "Broadcom",
                       "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-cb234ac6-0b89-48af-baa5-069ff22a27ec.jpg",
                       "twitterHandle": "@starbuxman",
                       "linkedInUsername": "joshlong",
                       "blueskyUsername": null,
                       "mastodonUsername": null,
                       "countryName": "United States of America"
                     }
                   ],
                   "keywords": [
                     {
                       "name": "modularity"
                     },
                     {
                       "name": "java"
                     },
                     {
                       "name": "spring"
                     },
                     {
                       "name": "graalvm"
                     }
                   ],
                   "timeSlots": []
                 },
                 {
                   "id": 3000,
                   "title": "Passkeys in practice: implementing passwordless apps",
                   "description": "\\u003Cp\\u003EPasswords. They&#39;re everywhere, they get leaked... A security nightmare! A work-around is to a delegate authentication to a third party, for example using OpenID Connect. But sometimes you can&#39;t or don&#39;t want to do that - can you still go password-less, with user-friendly flows?\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EPasskeys, and more specifically the WebAuthN spec, is a browser-based technology that allows you to log in using physical devices, such as a Yubikey, or MacOS&#39;s TouchID or iOS&#39; FaceID. It has been well-supported by browsers for multiple years now. With this technology, we can make our apps authenticate users without a password.\\u003C/p\\u003E\\u003Cp\\u003E\\u003C/p\\u003E\\u003Cp\\u003EIn this presentation, we will discuss the basics of WebAuthN, and use the brand new support for passkeys in Spring Boot 3.4 to integrate it in an existing application.\\u003C/p\\u003E",
                   "summary": "This presentation explores password-less authentication using the WebAuthN specification and passkeys, enabling secure login via physical devices like Yubikey, TouchID, or FaceID. It highlights integrating this technology into applications using new support in Spring Boot 3.4, providing user-friendly, password-free authentication solutions.",
                   "afterVideoURL": "https://www.youtube.com/embed/z-Fwi-Zf0Dk?si=EwhKluN7s-zMB2fw",
                   "podcastURL": null,
                   "audienceLevel": "INTERMEDIATE",
                   "language": null,
                   "totalFavourites": null,
                   "track": {
                     "id": 1255,
                     "name": "Security",
                     "description": "Defensive practices, tools and technologies to be secure                                                                                ",
                     "imageURL": "https://www.devoxx.co.uk/wp-content/uploads/2024/10/TrackIcons_Security-Colour-1.png"
                   },
                   "sessionType": {
                     "id": 951,
                     "slug": "conf50",
                     "name": "Conference",
                     "duration": 50,
                     "pause": false,
                     "invitationOnly": null,
                     "freeTickets": null,
                     "minSpeakers": 1,
                     "description": "50 minute sessions on a range of different technologies, practices and methodologies",
                     "cssColor": null,
                     "scheduleInSmallRoom": false
                   },
                   "speakers": [
                     {
                       "id": 2647,
                       "firstName": "Daniel",
                       "lastName": "Garnier-Moiroux",
                       "fullName": "Daniel Garnier-Moiroux",
                       "bio": "\\u003Cp\\u003EDaniel Garnier is a software engineer on the Spring team, working on Spring Security, and more broadly in the identity space and SSO for applications. He is an adjunct professor at Mines Paris, where he teaches CS and software engineering classes.\\u003C/p\\u003E",
                       "anonymizedBio": "This speaker is a software engineer on a team focused on Spring, working specifically on Spring Security and more broadly in the identity space and SSO for applications. They are also an adjunct professor at a university in Paris, where they teach computer science and software engineering courses.",
                       "company": "Spring",
                       "imageUrl": "https://devoxxian-image-thumbnails.s3-eu-west-1.amazonaws.com/profile-ec6ef8f7-e2c4-4d46-b973-a9abaab5c971.jpg",
                       "twitterHandle": "@kehrlann",
                       "linkedInUsername": "garniermoiroux",
                       "blueskyUsername": "",
                       "mastodonUsername": "",
                       "countryName": "France"
                     }
                   ],
                   "keywords": [
                     {
                       "name": "passkeys"
                     },
                     {
                       "name": "spring boot"
                     },
                     {
                       "name": "webauthn"
                     },
                     {
                       "name": "authentication"
                     }
                   ],
                   "timeSlots": []
                 }
              ]            
            }
            """, 200)));
  }

  private void verifyFindTalksWithKeywords() {
    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathTemplate("/api/public/search/{searchQuery}"))
            .withPathParam("searchQuery", equalTo("quarkus"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
            .withHeader(ClientProducer.PORTAL_NAME_HEADER, equalTo("portal1"))
    );

    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathTemplate("/api/public/search/{searchQuery}"))
            .withPathParam("searchQuery", equalTo("spring"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
            .withHeader(ClientProducer.PORTAL_NAME_HEADER, equalTo("portal1"))
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