package com.redhat.cfpaggregator.client.sessionize;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

import com.redhat.cfpaggregator.client.CfpClient;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;

@Path("/api/v2/{portalName}/view")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SessionizeClient extends CfpClient {
  @GET
  @Path("/All")
  @ClientHeaderParam(name = CfpClient.PORTAL_NAME_HEADER, value = "{portalName}")
  SessionizeEventDetails getAll(@PathParam("portalName") String portalName);

  @Override
  default Event createEvent(Portal portal, TalkSearchCriteria searchCriteria) {
    var portalName = portal.getPortalName();
    var event = Event.builder()
        .portalName(portalName)
        .name(portalName)
        .build();

    var data = getAll(portalName);

    if (data != null) {
      var uniqueSpeakers = getUniqueSpeakers(data, searchCriteria);

      if (!uniqueSpeakers.isEmpty()) {
        var sessions = data.sessions();

        if (sessions!=null) {
          sessions.stream()
              .filter(Objects::nonNull)
              .filter(session -> !Collections.disjoint(session.speakers(), uniqueSpeakers.keySet()))
              .filter(session -> searchCriteria.hasTalkKeywords() ?
                  searchCriteria.getTalkKeywords()
                      .stream()
                      .anyMatch(talkKeyword ->
                          Optional.ofNullable(session.description())
                              .orElse("")
                              .contains(talkKeyword)
                      ) :
                  true
              )
              .forEach(talk -> {
                var mappedTalk = TALK_MAPPER.fromSessionize(talk);

                talk.speakers().stream()
                    .filter(Objects::nonNull)
                    .map(uniqueSpeakers::get)
                    .forEach(speaker -> {
                      event.addSpeakers(speaker);
                      speaker.addTalks(mappedTalk);
                    });
              });
        }
      }
    }

    portal.setEvent(event);
    return event;
  }

  private static Map<String, Speaker> getUniqueSpeakers(SessionizeEventDetails session, TalkSearchCriteria searchCriteria) {
    return Optional.ofNullable(session)
        .map(SessionizeEventDetails::speakers)
        .orElseGet(List::of)
        .stream()
        .filter(speaker ->
            searchCriteria.hasSpeakerCompanies() ?
                searchCriteria.getSpeakerCompanies()
                    .stream()
                    .anyMatch(speakerCompany ->
                        Optional.ofNullable(speaker.bio())
                            .orElse("")
                            .contains(speakerCompany)
                    ) :
                true
        )
        .distinct()
        .map(SPEAKER_MAPPER::fromSessionize)
        .collect(Collectors.toMap(Speaker::getEventSpeakerId, Function.identity()));
  }
}
