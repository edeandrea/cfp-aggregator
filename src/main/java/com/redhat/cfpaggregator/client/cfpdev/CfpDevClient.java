package com.redhat.cfpaggregator.client.cfpdev;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

import io.quarkus.rest.client.reactive.NotBody;

import com.redhat.cfpaggregator.client.CfpClient;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;

/**
 * Represents a REST client interface for accessing the cfp.dev API.
 *
 * @author Eric Deandrea
 */
@Path("/api/public")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CfpDevClient extends CfpClient {
  /**
   * Fetches the details of an event from the cfp.dev API.
   */
  @GET
  @Path("/event")
  @ClientHeaderParam(name = CfpClient.PORTAL_NAME_HEADER, value = "{portalName}")
  CfpDevEventDetails getEventDetails(@NotBody String portalName);

  /**
   * Searches for talks based on the specified search query.
   *
   * @param searchQuery the search query used to find talks; typically a string representing talk titles,
   *                    keywords, or other attributes
   * @return a list of {@code CfpDevTalkDetails} objects that match the search query
   */
  @GET
  @Path("/search/{searchQuery}")
  @ClientHeaderParam(name = CfpClient.PORTAL_NAME_HEADER, value = "{portalName}")
  CfpDevTalkSearchResults findTalks(@PathParam("searchQuery") String searchQuery, @NotBody String portalName);

  /**
   * Retrieves a list of all talk details available from the cfp.dev API.
   *
   * @return a list of {@code CfpDevTalkDetails} objects representing all available talks
   */
  @GET
  @Path("/talks")
  @ClientHeaderParam(name = CfpClient.PORTAL_NAME_HEADER, value = "{portalName}")
  List<CfpDevTalkDetails> getAllTalks(@NotBody String portalName);

  /**
   * Searches for talks based on the specified search criteria, including talk keywords and speaker companies.
   * If no criteria are provided, retrieves all available talks.
   * For each matching talk, only the speakers that satisfy the company criteria will be included if applicable.
   *
   * @param searchCriteria the search criteria containing talk keywords and speaker companies for filtering the results
   * @return a list of {@code CfpDevTalkDetails} objects that match the search criteria
   */
  default List<CfpDevTalkDetails> findTalks(TalkSearchCriteria searchCriteria, String portalName) {
    // 1) Find all the talks for each keyword (if there are any)
    var talksStream = searchCriteria.hasTalkKeywords() ?
        searchCriteria.getTalkKeywords().stream()
            .flatMap(keyword -> findTalks(keyword, portalName).talks().stream()) :
        Optional.ofNullable(getAllTalks(portalName))
            .map(List::stream)
            .orElseGet(Stream::empty);

    // 2) For each talk, only retain the speakers that match the search criteria (if there is any)
    return searchCriteria.hasSpeakerCompanies() ?
        talksStream
            .map(talk -> new CfpDevTalkDetails(talk, searchCriteria.getSpeakerCompanies()))
            .filter(talk -> !talk.speakers().isEmpty())
            .toList() :
        talksStream.toList();
  }

  @Override
  default Event createEvent(Portal portal, TalkSearchCriteria talkSearchCriteria) {
    var portalName = portal.getPortalName();
    var eventDetails = getEventDetails(portal.getPortalName());
    var event = EVENT_MAPPER.fromCfpDev(portalName, eventDetails);

    if (eventDetails != null) {
      var talks = findTalks(talkSearchCriteria, portalName);

      if (talks != null) {
        var uniqueSpeakers = new HashMap<String, Speaker>();

        talks.forEach(talk -> {
          var speakers = talk.speakers();

          if (speakers != null) {
            var mappedTalk = TALK_MAPPER.fromCfpDev(talk);
            speakers.stream()
                .filter(Objects::nonNull)
                .map(speaker -> uniqueSpeakers.computeIfAbsent(String.valueOf(speaker.eventSpeakerId()), id -> SPEAKER_MAPPER.fromCfpDev(speaker)))
                .forEach(speaker -> {
                  event.addSpeakers(speaker);
                  speaker.addTalks(mappedTalk);
                });
          }
        });
      }
    }

    portal.setEvent(event);
    return event;
  }
}