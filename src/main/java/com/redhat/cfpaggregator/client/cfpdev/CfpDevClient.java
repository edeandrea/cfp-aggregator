package com.redhat.cfpaggregator.client.cfpdev;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Represents a REST client interface for accessing the cfp.dev API.
 *
 * @author Eric Deandrea
 */
@Path("/api/public")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CfpDevClient {
  enum Company {
    RED_HAT("Red Hat"),
    IBM("IBM");

    private final String strVersion;

    Company(String strVersion) {
      this.strVersion = URLEncoder.encode(strVersion.toLowerCase(), StandardCharsets.UTF_8);
    }

    public String getStrVersion() {
      return this.strVersion;
    }
  }

  /**
   * Fetches the details of an event from the cfp.dev API.
   */
  @GET
  @Path("/event")
  CfpDevEventDetails getEventDetails();

  /**
   * Searches for speakers based on the provided search query.
   *
   * @param searchQuery the search query used to find speakers; typically a string representing speaker names, interests, or other attributes
   * @return a list of {@code CfpDevSpeakerDetails} objects that match the search query
   */
  @GET
  @Path("/speakers/search/{searchQuery}")
  List<CfpDevSpeakerDetails> findSpeakers(@PathParam("searchQuery") String searchQuery);

  /**
   * Retrieves a list of all speakers available from the cfp.dev API.
   *
   * @return a list of {@code CfpDevSpeakerDetails} objects representing all available speakers
   */
  @GET
  @Path("/speakers")
  List<CfpDevSpeakerDetails> getAllSpeakers();

  /**
   * Retrieves a list of speaker details associated with the specified company.
   *
   * @param companies Companies to search for, or leave empty to search for all companies
   * @return a list of {@code CfpDevSpeakerDetails} representing the speakers from the specified company
   */
  default List<CfpDevSpeakerDetails> findSpeakersByCompany(Company... companies) {
    var companyNames = Optional.ofNullable(companies)
        .map(Arrays::stream)
        .orElseGet(Stream::of)
        .map(Company::getStrVersion)
        .map(String::strip)
        .filter(companyName -> !companyName.isBlank())
        .toList();

    return companyNames.isEmpty() ?
        getAllSpeakers() :
        companyNames.stream()
            .flatMap(companyName -> findSpeakers(companyName).stream())
            .toList();
  }
}