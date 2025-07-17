package com.redhat.cfpaggregator.client.sessionize;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

import com.redhat.cfpaggregator.client.CfpClient;
import com.redhat.cfpaggregator.client.CfpClientTests;
import com.redhat.cfpaggregator.client.sessionize.SessionizeClientTests.ConfigTestProfile;
import com.redhat.cfpaggregator.client.sessionize.SessionizeSpeakerDetails.Link;

@QuarkusTest
@TestProfile(ConfigTestProfile.class)
public class SessionizeClientTests extends CfpClientTests<SessionizeClient> {
  SessionizeClientTests() {
    super(SessionizeClient.class);
  }

  @Test
  void getAll() throws MalformedURLException {
    this.wireMock.register(get(urlPathEqualTo("/api/v2/portal1/view/All"))
        .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
        .withHeader(CfpClient.PORTAL_NAME_HEADER, equalTo("portal1"))
        .willReturn(jsonResponse("""
            {
              "sessions": [
                {
                  "id": "14022",
                  "title": "Aiden's Keynote",
                  "description": "Usually, you would find a session description here. But, this is not a real session submission, so description is missing. Ha!",
                  "startsAt": "2023-09-16T09:00:00Z",
                  "endsAt": "2023-09-16T10:00:00Z",
                  "isServiceSession": false,
                  "isPlenumSession": true,
                  "speakers": [
                    "00000000-0000-0000-0000-000000000004"
                  ],
                  "categoryItems": [
                    4373,
                    10663,
                    10666,
                    10672
                  ],
                  "questionAnswers": [
                    {
                      "questionId": 148,
                      "answerValue": "67"
                    }
                  ],
                  "roomId": 215,
                  "liveUrl": null,
                  "recordingUrl": null,
                  "status": "Accepted",
                  "isInformed": false,
                  "isConfirmed": false
                },
                {
                  "id": "14020",
                  "title": "Emma's Session",
                  "description": "Usually, you would find a session description here. But, this is not a real session submission, so description is missing. Ha!",
                  "startsAt": "2023-09-16T10:15:00Z",
                  "endsAt": "2023-09-16T11:15:00Z",
                  "isServiceSession": false,
                  "isPlenumSession": false,
                  "speakers": [
                    "00000000-0000-0000-0000-000000000002"
                  ],
                  "categoryItems": [
                    4373,
                    10664,
                    10668,
                    17076
                  ],
                  "questionAnswers": [
                    {
                      "questionId": 148,
                      "answerValue": "45"
                    }
                  ],
                  "roomId": 215,
                  "liveUrl": null,
                  "recordingUrl": null,
                  "status": "Accepted",
                  "isInformed": false,
                  "isConfirmed": false
                }
              ],
              "speakers": [
                {
                  "id": "00000000-0000-0000-0000-000000000004",
                  "firstName": "Aiden",
                  "lastName": "Test",
                  "bio": "Pop culture fanatic. Friend of animals everywhere. Student. Thinker. Bacon practitioner.",
                  "tagLine": "Professional public speaker",
                  "profilePicture": "https://sessionize.com/image/8db9-400o400o1-test4.jpg",
                  "isTopSpeaker": true,
                  "links": [
                    {
                      "title": "Twitter",
                      "url": "https://twitter.com/sessionizecom",
                      "linkType": "Twitter"
                    },
                    {
                      "title": "LinkedIn",
                      "url": "http://linkedin.com/in/domagojpa",
                      "linkType": "LinkedIn"
                    }
                  ],
                  "sessions": [
                    14022
                  ],
                  "fullName": "Aiden Test",
                  "categoryItems": [],
                  "questionAnswers": []
                },
                {
                  "id": "00000000-0000-0000-0000-000000000008",
                  "firstName": "Ava",
                  "lastName": "Test",
                  "bio": "Student. Wannabe creator. Incurable music advocate.",
                  "tagLine": "PR specialist",
                  "profilePicture": "https://sessionize.com/image/e987-400o400o1-test8.jpg",
                  "isTopSpeaker": false,
                  "links": [],
                  "sessions": [
                    14026
                  ],
                  "fullName": "Ava Test",
                  "categoryItems": [],
                  "questionAnswers": []
                }
              ],
              "questions": [
                {
                  "id": 148,
                  "question": "Demo",
                  "questionType": "Percentage",
                  "sort": 5
                }
              ],
              "categories": [
                {
                  "id": 2124,
                  "title": "Session format",
                  "items": [
                    {
                      "id": 4372,
                      "name": "Lightning talk",
                      "sort": 1
                    },
                    {
                      "id": 4373,
                      "name": "Session",
                      "sort": 2
                    },
                    {
                      "id": 4374,
                      "name": "Workshop",
                      "sort": 3
                    }
                  ],
                  "sort": 0,
                  "type": "session"
                },
                {
                  "id": 2125,
                  "title": "Track",
                  "items": [
                    {
                      "id": 10663,
                      "name": "Technical",
                      "sort": 1
                    },
                    {
                      "id": 10664,
                      "name": "Scientific",
                      "sort": 2
                    },
                    {
                      "id": 4375,
                      "name": "Business",
                      "sort": 3
                    }
                  ],
                  "sort": 1,
                  "type": "session"
                },
                {
                  "id": 2126,
                  "title": "Level",
                  "items": [
                    {
                      "id": 10665,
                      "name": "Introductory and overview",
                      "sort": 1
                    },
                    {
                      "id": 10666,
                      "name": "Intermediate",
                      "sort": 2
                    },
                    {
                      "id": 10667,
                      "name": "Advanced",
                      "sort": 3
                    },
                    {
                      "id": 10668,
                      "name": "Expert",
                      "sort": 4
                    }
                  ],
                  "sort": 2,
                  "type": "session"
                },
                {
                  "id": 2127,
                  "title": "Language",
                  "items": [
                    {
                      "id": 10669,
                      "name": "English",
                      "sort": 1
                    },
                    {
                      "id": 10670,
                      "name": "Spanish",
                      "sort": 2
                    },
                    {
                      "id": 10671,
                      "name": "German",
                      "sort": 3
                    },
                    {
                      "id": 10672,
                      "name": "Italian",
                      "sort": 4
                    },
                    {
                      "id": 10673,
                      "name": "French",
                      "sort": 5
                    },
                    {
                      "id": 10674,
                      "name": "Chinese",
                      "sort": 6
                    },
                    {
                      "id": 17076,
                      "name": "Arabic",
                      "sort": 7
                    },
                    {
                      "id": 10675,
                      "name": "Portuguese",
                      "sort": 8
                    }
                  ],
                  "sort": 3,
                  "type": "session"
                }
              ],
              "rooms": [
                {
                  "id": 215,
                  "name": "Green Room",
                  "sort": 0
                },
                {
                  "id": 216,
                  "name": "Yellow Room",
                  "sort": 1
                },
                {
                  "id": 217,
                  "name": "Restaurant",
                  "sort": 2
                }
              ]
            }
            """, Status.OK.getStatusCode())));

    assertThat(getClient().getAll("portal1"))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(
            new SessionizeEventDetails(
                List.of(
                    new SessionizeSessionDetails(
                        "14022",
                        "Aiden's Keynote",
                        "Usually, you would find a session description here. But, this is not a real session submission, so description is missing. Ha!",
                        OffsetDateTime.parse("2023-09-16T09:00:00Z"),
                        OffsetDateTime.parse("2023-09-16T10:00:00Z"),
                        List.of("00000000-0000-0000-0000-000000000004"),
                        null,
                        null,
                        "Accepted"
                    ),
                    new SessionizeSessionDetails(
                        "14020",
                        "Emma's Session",
                        "Usually, you would find a session description here. But, this is not a real session submission, so description is missing. Ha!",
                        OffsetDateTime.parse("2023-09-16T10:15:00Z"),
                        OffsetDateTime.parse("2023-09-16T11:15:00Z"),
                        List.of("00000000-0000-0000-0000-000000000002"),
                        null,
                        null,
                        "Accepted"
                    )
                ),
                List.of(
                    new SessionizeSpeakerDetails(
                        "00000000-0000-0000-0000-000000000004",
                        "Aiden",
                        "Test",
                        "Pop culture fanatic. Friend of animals everywhere. Student. Thinker. Bacon practitioner.",
                        "Professional public speaker",
                        new URL("https://sessionize.com/image/8db9-400o400o1-test4.jpg"),
                        List.of(
                            new Link("Twitter", "Twitter", new URL("https://twitter.com/sessionizecom")),
                            new Link("LinkedIn", "LinkedIn", new URL("http://linkedin.com/in/domagojpa"))
                        )
                    ),
                    new SessionizeSpeakerDetails(
                        "00000000-0000-0000-0000-000000000008",
                        "Ava",
                        "Test",
                        "Student. Wannabe creator. Incurable music advocate.",
                        "PR specialist",
                        new URL("https://sessionize.com/image/e987-400o400o1-test8.jpg"),
                        List.of()
                    )
                )
            )
        );

    this.wireMock.verifyThat(
        1,
        getRequestedFor(urlPathEqualTo("/api/v2/portal1/view/All"))
            .withHeader(HttpHeaders.ACCEPT, equalToIgnoreCase(MediaType.APPLICATION_JSON))
            .withHeader(CfpClient.PORTAL_NAME_HEADER, equalTo("portal1"))
    );
  }

  @Override
  @Test
  public void createEventNoKeywordsNoSpeakers() throws MalformedURLException {

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
          "cfps.portals.portal1.portal-type", "SESSIONIZE",
          "cfps.portals.portal1.description", "CFP Portal 1"
      ));

      return params;
    }
  }
}
