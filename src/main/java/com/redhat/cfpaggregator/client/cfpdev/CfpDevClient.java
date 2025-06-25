package com.redhat.cfpaggregator.client.cfpdev;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
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
  /**
   * Fetches the details of an event from the cfp.dev API.
   */
  @GET
  @Path("/event")
  CfpDevEventDetails getEventDetails();
}