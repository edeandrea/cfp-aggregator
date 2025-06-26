package com.redhat.cfpaggregator.config;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration mapping interface for CFP portals.
 *
 * @author Eric Deandrea
 */
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
     * The cfp.dev portal(s), which includes platforms such as Devoxx and VoxxedDays.
     */
    CFP_DEV,

    /**
     * The Sessionize platform as a type of portal in the CFP configuration.
     */
    SESSIONIZE,

    /**
     * The Dev2Next platform as a type of portal in the CFP configuration.
     */
    DEV2NEXT
  }

  /**
   * Whether or not the application should reload all of its data on startup
   */
  @WithDefault("false")
  Boolean reloadOnStartup();

  /**
   * Whether all clients should log requests
   */
  @WithDefault("false")
  Boolean logRequests();

  /**
   * Whether all clients should log responses
   */
  @WithDefault("false")
  Boolean logResponses();

  /**
   * Timeout for all client calls
   */
  @WithDefault("1m")
  Duration timeout();

  /**
   * Retrieves the configuration for all defined portals.
   *
   * Each portal is represented as an entry in the map where the key is the portal's name
   * and the value is its corresponding configuration. This configuration includes details
   * such as the base URL, portal type, logging preferences, and timeout settings.
   */
  @ConfigDocSection
  @ConfigDocMapKey("portal-name")
  Map<String, CfpPortalConfig> portals();

  /**
   * Retrieves the default search criteria configuration.
   */
  DefaultSearchCriteria defaultSearchCriteria();

  /**
   * Retrieves the set of portal names from the configuration.
   *
   * The portal names are derived from the keys of the configured portal mappings.
   * This method provides an immutable set of these names to ensure the portal
   * mappings remain unmodifiable.
   *
   * @return an unmodifiable set containing the names of all configured portals
   */
  default Set<String> portalNames() {
    return Collections.unmodifiableSet(portals().keySet());
  }

  /**
   * Defines the default search criteria for the CFP portals configuration.
   */
  interface DefaultSearchCriteria {
    @WithDefault("Quarkus")
    List<String> talkKeywords();

    @WithDefault("Red Hat")
    List<String> companies();
  }

  /**
   * The configuration for a single portal within the CFP configuration.
   *
   * This interface provides the details required to configure a specific portal instance,
   * including its base URL, portal type, and an optional description. It is typically used
   * as part of a mapping for multiple portals within the {@code CfpPortalsConfig}.
   */
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

    /**
     * Whether an individual client should log requests
     */
    @WithDefault("${cfps.log-requests:false}")
    Boolean logRequests();

    /**
     * Whether an individual client should log responses
     */
    @WithDefault("${cfps.log-responses:false}")
    Boolean logResponses();

    /**
     * Timeout for an individual client call
     */
    @WithDefault("${cfps.timeout:1m}")
    Duration timeout();
  }
}
