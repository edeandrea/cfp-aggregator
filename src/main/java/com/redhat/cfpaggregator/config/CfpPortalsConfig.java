package com.redhat.cfpaggregator.config;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "cfps")
public interface CfpPortalsConfig {
  /**
   * Represents the types of portals supported in the CFP configuration.
   *
   * This enum is part of the configuration mapping for defining portal-specific details and behaviors.
   * It identifies the three available portal types:
   * - CFP_DEV: Represents the cfp.dev portal(s) (Devoxx, VoxxedDays).
   * - SESSIONIZE: Represents the Sessionize platform.
   * - DEV2NEXT: Represents the Dev2Next platform.
   *
   * These portal types are used to configure portal-specific settings through the {@link CfpPortalsConfig}.
   */
  enum PortalType {
    /**
     * Represents the cfp.dev portal(s), which includes platforms such as Devoxx and VoxxedDays.
     */
    CFP_DEV,

    /**
     * Represents the Sessionize platform as a type of portal in the CFP configuration.
     */
    SESSIONIZE,

    /**
     * Represents the Dev2Next platform as a type of portal in the CFP configuration.
     */
    DEV2NEXT
  }

  @ConfigDocSection
  @ConfigDocMapKey("portal-name")
  Map<String, CfpPortalConfig> portals();

  interface CfpPortalConfig {
    /**
     * The base URL configured for the portal.
     */
    String baseUrl();

    /**
     * The type of portal configured for this instance.
     */
    PortalType portalType();

    /**
     * An optional description for the portal configuration. This description may include additional context or details about the portal setup.
     */
    Optional<String> description();
  }
}
