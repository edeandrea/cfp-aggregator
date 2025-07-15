package com.redhat.cfpaggregator.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.redhat.cfpaggregator.service.CfpService;
import com.redhat.cfpaggregator.service.ExportFormat;

@Path("/export/config")
public class ExportConfigResource {
  private final CfpService cfpService;

  public ExportConfigResource(CfpService cfpService) {
    this.cfpService = cfpService;
  }

  @GET
  @Path("/portalsConfig.{extension: (cfpimport|properties|yml|env|sql)}")
  public Response exportConfig(@PathParam("extension") String extension) {
    return ExportFormat.fromFileExtension(extension)
        .map(exportFormat ->
            Response.ok(
                    exportFormat.render(this.cfpService.getPortals()),
                    exportFormat.getContentType()
                )
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"portalsConfig.%s\"".formatted(extension))
        )
        .orElseGet(() -> Response.status(Status.UNSUPPORTED_MEDIA_TYPE))
        .build();
  }
}
