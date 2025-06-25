package com.redhat.cfpaggregator.config;

import static com.redhat.cfpaggregator.config.CfpPortalsConfig.CfpPortalConfig;
import static com.redhat.cfpaggregator.config.CfpPortalsConfig.PortalType;
import static com.redhat.cfpaggregator.config.CfpPortalsConfigTests.ConfigTestProfile;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(ConfigTestProfile.class)
class CfpPortalsConfigTests {
  @Inject
  CfpPortalsConfig config;

  @Test
  void configCorrect() {
    assertThat(config).isNotNull();
    assertThat(config.portalNames()).contains("portal1", "portal2", "portal3");

    assertThat(config.portals())
        .hasEntrySatisfying("portal1", config ->
            assertThat(config)
                .isNotNull()
                .extracting(
                    CfpPortalConfig::baseUrl,
                    CfpPortalConfig::portalType,
                    CfpPortalConfig::description,
                    CfpPortalConfig::logRequests,
                    CfpPortalConfig::logResponses,
                    CfpPortalConfig::timeout
                )
                .containsExactly(
                    "https://portal1.example.com",
                    PortalType.CFP_DEV,
                    Optional.of("CFP Portal 1"),
                    true,
                    true,
                    Duration.ofSeconds(10)
                )
        )
        .hasEntrySatisfying("portal2", config ->
            assertThat(config)
                .isNotNull()
                .extracting(
                    CfpPortalConfig::baseUrl,
                    CfpPortalConfig::portalType,
                    CfpPortalConfig::description,
                    CfpPortalConfig::logRequests,
                    CfpPortalConfig::logResponses,
                    CfpPortalConfig::timeout
                )
                .containsExactly(
                    "https://portal2.example.com",
                    PortalType.SESSIONIZE,
                    Optional.of("CFP Portal 2"),
                    true,
                    false,
                    Duration.ofMinutes(10)
                )
        )
        .hasEntrySatisfying("portal3", config ->
            assertThat(config)
                .isNotNull()
                .extracting(
                    CfpPortalConfig::baseUrl,
                    CfpPortalConfig::portalType,
                    CfpPortalConfig::description,
                    CfpPortalConfig::logRequests,
                    CfpPortalConfig::logResponses,
                    CfpPortalConfig::timeout
                )
                .containsExactly(
                    "https://portal3.example.com",
                    PortalType.DEV2NEXT,
                    Optional.empty(),
                    false,
                    true,
                    Duration.ofSeconds(10)
                )
        );
  }

  public static class ConfigTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var params = new HashMap<>(Map.of(
          "cfps.timeout", "10s",
          "cfps.log-requests", "true"
      ));

      params.putAll(Map.of(
          "cfps.portals.portal1.base-url", "https://portal1.example.com",
          "cfps.portals.portal1.portal-type", "CFP_DEV",
          "cfps.portals.portal1.description", "CFP Portal 1",
          "cfps.portals.portal1.log-responses", "true"
      ));

      params.putAll(Map.of(
          "cfps.portals.portal2.base-url", "https://portal2.example.com",
          "cfps.portals.portal2.portal-type", "SESSIONIZE",
          "cfps.portals.portal2.description", "CFP Portal 2",
          "cfps.portals.portal2.timeout", "10m"
      ));

      params.putAll(Map.of(
          "cfps.portals.portal3.base-url", "https://portal3.example.com",
          "cfps.portals.portal3.portal-type", "DEV2NEXT",
          "cfps.portals.portal3.log-requests", "false",
          "cfps.portals.portal3.log-responses", "true"
      ));

      return params;
    }
  }
}