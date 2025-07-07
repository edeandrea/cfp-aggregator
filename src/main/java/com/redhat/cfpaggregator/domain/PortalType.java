package com.redhat.cfpaggregator.domain;

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
