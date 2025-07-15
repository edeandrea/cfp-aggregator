package com.redhat.cfpaggregator.ui.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.qute.Variant;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.templates.ExportConfigTemplates;

public enum ExportFormat {
  APP_IMPORT("Application import", "application/cfp-import", ".cfpimport") {
    private final ObjectMapper objectMapper = new ObjectMapper()
        .setSerializationInclusion(Include.NON_EMPTY);

    @Override
    public String render(Collection<Portal> portals) {
      try {
        var barePortals = portals.stream()
            .map(Portal::cloneAsNewWithoutEvent)
            .toList();

        return this.objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(barePortals);
      }
      catch (JsonProcessingException e) {
        throw new ExportException(e.getMessage());
      }
    }
  },
  PROPERTIES("Properties", "text/properties", ".properties"),
  YAML("Yaml", "application/yaml", ".yml"),
  ENV_VAR("Environment Variables", Variant.TEXT_PLAIN, ".env"),
  SQL("SQL", "application/sql", ".sql");

  private final String stringValue;
  private final String contentType;
  private final String fileExtension;

  ExportFormat(String stringValue, String contentType, String fileExtension) {
    this.stringValue = stringValue;
    this.contentType = contentType;
    this.fileExtension = fileExtension;
  }

  public String render(Collection<Portal> portals) {
    return ExportConfigTemplates.portalsConfig(portals)
        .setVariant(Variant.forContentType(getContentType()))
        .render();
  }

  @Override
  public String toString() {
    return this.stringValue;
  }

  public String getFileExtension() {
    return this.fileExtension;
  }

  public String getContentType() {
    return this.contentType;
  }

  public static Optional<ExportFormat> fromFileExtension(String fileExtension) {
    var extension = fileExtension.startsWith(".") ? fileExtension : "." + fileExtension;
    return Arrays.stream(values())
        .filter(exportFormat -> exportFormat.getFileExtension().equals(extension))
        .findFirst();
  }

  public static Set<ExportFormat> allSorted() {
    return EnumSet.allOf(ExportFormat.class)
        .stream()
        .sorted(Comparator.comparing(ExportFormat::toString))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
