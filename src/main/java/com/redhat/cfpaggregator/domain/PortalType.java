package com.redhat.cfpaggregator.domain;

import com.redhat.cfpaggregator.client.CfpClient;
import com.redhat.cfpaggregator.client.cfpdev.CfpDevClient;
import com.redhat.cfpaggregator.client.sessionize.SessionizeClient;
import com.redhat.cfpaggregator.config.CfpPortalsConfig;

/**
 * Represents the types of portals supported in the CFP configuration.
 * <p>
 * This enum is part of the configuration mapping for defining portal-specific details and behaviors.
 * It identifies the three available portal types:
 * - CFP_DEV: Represents the cfp.dev portal(s) (Devoxx, VoxxedDays).
 * - SESSIONIZE: Represents the Sessionize platform.
 * - DEV2NEXT: Represents the Dev2Next platform.
 * <p>
 * These portal types are used to configure portal-specific settings through the {@link CfpPortalsConfig}.
 */
public enum PortalType {
  /**
   * The cfp.dev portal(s), which includes platforms such as Devoxx and VoxxedDays.
   */
  CFP_DEV("cfp.dev", CfpDevClient.class),

  /**
   * The Sessionize platform as a type of portal in the CFP configuration.
   */
  SESSIONIZE("sessionize.com", "https://sessionize.com", SessionizeClient.class),

  /**
   * The Dev2Next platform as a type of portal in the CFP configuration.
   */
  DEV2NEXT("dev2next", null);

  private final String description;
  private final String defaultUrl;
  private final Class<? extends CfpClient> clientClass;

  PortalType(String description, Class<? extends CfpClient> clientClass) {
    this(description, null, clientClass);
  }

  PortalType(String description, String defaultUrl, Class<? extends CfpClient> clientClass) {
    this.description = description;
    this.defaultUrl = defaultUrl;
    this.clientClass = clientClass;
  }

  public String getDescription() {
    return description;
  }

  public boolean hasDefaultUrl() {
    return this.defaultUrl != null;
  }

  public String getDefaultUrl() {
    return this.defaultUrl;
  }

  public <T extends CfpClient> Class<T> getClientClass() {
    return (Class<T>) this.clientClass;
  }
}
