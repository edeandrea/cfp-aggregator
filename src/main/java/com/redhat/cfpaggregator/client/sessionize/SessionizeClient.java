package com.redhat.cfpaggregator.client.sessionize;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v2/{portalName}/view")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SessionizeClient {
  @GET
  @Path("/All")
  List<SessionizeInfo> getAll(@PathParam("portalName") String portalName);
}
