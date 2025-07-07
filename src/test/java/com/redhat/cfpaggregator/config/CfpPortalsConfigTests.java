package com.redhat.cfpaggregator.config;

import static com.redhat.cfpaggregator.config.CfpPortalsConfig.CfpPortalConfig;
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

import com.redhat.cfpaggregator.domain.PortalType;

@QuarkusTest
@TestProfile(ConfigTestProfile.class)
class CfpPortalsConfigTests {
  @Inject
  CfpPortalsConfig config;

  @Test
  void configCorrect() {
    assertThat(config)
        .isNotNull()
        .extracting(
            CfpPortalsConfig::reloadOnStartup,
            CfpPortalsConfig::logRequests,
            CfpPortalsConfig::logResponses,
            CfpPortalsConfig::timeout
        )
        .containsExactly(
            false,
            true,
            false,
            Duration.ofSeconds(10)
        );

    assertThat(config.portalNames()).contains("portal1", "portal2", "portal3");

    assertThat(config.portals())
        .hasEntrySatisfying("portal1", config ->
            assertThat(config)
                .isNotNull()
                .extracting(
                    CfpPortalConfig::getBaseUrl,
                    CfpPortalConfig::getPortalType,
                    CfpPortalConfig::getDescription
                )
                .containsExactly(
                    "https://portal1.example.com",
                    PortalType.CFP_DEV,
                    Optional.of("CFP Portal 1")
                )
        )
        .hasEntrySatisfying("portal2", config ->
            assertThat(config)
                .isNotNull()
                .extracting(
                    CfpPortalConfig::getBaseUrl,
                    CfpPortalConfig::getPortalType,
                    CfpPortalConfig::getDescription
                )
                .containsExactly(
                    "https://portal2.example.com",
                    PortalType.SESSIONIZE,
                    Optional.of("CFP Portal 2")
                )
        )
        .hasEntrySatisfying("portal3", config ->
            assertThat(config)
                .isNotNull()
                .extracting(
                    CfpPortalConfig::getBaseUrl,
                    CfpPortalConfig::getPortalType,
                    CfpPortalConfig::getDescription
                )
                .containsExactly(
                    "https://portal3.example.com",
                    PortalType.DEV2NEXT,
                    Optional.empty()
                )
        );
  }

  public static class ConfigTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var params = new HashMap<>(Map.of(
          "cfps.timeout", "10s",
          "cfps.log-requests", "true",
          "cfps.log-responses", "false"
      ));

      params.putAll(Map.of(
          "cfps.portals.portal1.base-url", "https://portal1.example.com",
          "cfps.portals.portal1.portal-type", "CFP_DEV",
          "cfps.portals.portal1.description", "CFP Portal 1"
      ));

      params.putAll(Map.of(
          "cfps.portals.portal2.base-url", "https://portal2.example.com",
          "cfps.portals.portal2.portal-type", "SESSIONIZE",
          "cfps.portals.portal2.description", "CFP Portal 2"
      ));

      params.putAll(Map.of(
          "cfps.portals.portal3.base-url", "https://portal3.example.com",
          "cfps.portals.portal3.portal-type", "DEV2NEXT"
      ));

      return params;
    }
  }
}