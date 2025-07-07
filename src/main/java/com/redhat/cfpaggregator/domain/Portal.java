package com.redhat.cfpaggregator.domain;

import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "portals")
public class Portal {
  @Id
  private String portalName;

  @NotEmpty(message = "base_url can not be null or empty")
  private String baseUrl;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @NotNull(message = "portal_type can not be null")
  private PortalType portalType;

  @OneToOne(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      mappedBy = "portal",
      orphanRemoval = true
  )
  @PrimaryKeyJoinColumn
  private Event event;

  // Default constructor for JPA
  public Portal() {
  }

  // Private constructor for builder
  private Portal(Builder builder) {
    this.portalName = builder.portalName;
    this.baseUrl = builder.baseUrl;
    this.description = builder.description;
    this.portalType = builder.portalType;
    this.event = builder.event;

    if (this.event != null) {
      this.event.setPortal(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  // Getters and setters
  public String getPortalName() {
    return portalName;
  }

  public void setPortalName(String portalName) {
    this.portalName = portalName;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PortalType getPortalType() {
    return portalType;
  }

  public void setPortalType(PortalType portalType) {
    this.portalType = portalType;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;

    if (event != null) {
      event.setPortal(this);
    }
  }

  public Portal cloneAsNew() {
    return (this.event != null) ?
        cloneAsNewWithNewEvent(this.event.cloneAsNew()) :
        cloneAsNewWithNewEvent(null);
  }

  public Portal cloneAsNewWithNewEvent(Event event) {
    return toBuilder()
        .event(event)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Portal portal)) return false;
    return Objects.equals(portalName, portal.portalName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(portalName);
  }

  @Override
  public String toString() {
    return "Portal{" +
        "portalName='" + portalName + '\'' +
        ", baseUrl='" + baseUrl + '\'' +
        ", description='" + description + '\'' +
        ", portalType=" + portalType +
        ", event=" + ((event != null) ? event.getName() : "") +
        '}';
  }

  public static final class Builder {
    private String portalName;
    private String baseUrl;
    private String description;
    private PortalType portalType;
    private Event event;

    private Builder() {
    }

    private Builder(Portal portal) {
      this.portalName = portal.portalName;
      this.baseUrl = portal.baseUrl;
      this.description = portal.description;
      this.portalType = portal.portalType;
      this.event = portal.event;
    }

    public Builder portalName(String portalName) {
      this.portalName = portalName;
      return this;
    }

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder portalType(PortalType portalType) {
      this.portalType = portalType;
      return this;
    }

    public Builder event(Event event) {
      this.event = event;
      return this;
    }

    public Portal build() {
      return new Portal(this);
    }
  }
}