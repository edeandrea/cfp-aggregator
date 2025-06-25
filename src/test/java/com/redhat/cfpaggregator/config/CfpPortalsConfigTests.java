package com.redhat.cfpaggregator.config;

import static com.redhat.cfpaggregator.config.CfpPortalsConfig.CfpPortalConfig;
import static com.redhat.cfpaggregator.config.CfpPortalsConfig.PortalType;
import static com.redhat.cfpaggregator.config.CfpPortalsConfigTests.ConfigTestProfile;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(ConfigTestProfile.class)
class CfpPortalsConfigTests implements QuarkusTestProfile {
  @Inject
  CfpPortalsConfig config;

  @Test
  void configCorrect() {
    assertThat(config).isNotNull();
    assertThat(config.portals())
        .hasSize(3)
        .hasEntrySatisfying("portal1", config ->
            assertThat(config)
                .isNotNull()
                .extracting(
                    CfpPortalConfig::baseUrl,
                    CfpPortalConfig::portalType,
                    CfpPortalConfig::description
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
                    CfpPortalConfig::baseUrl,
                    CfpPortalConfig::portalType,
                    CfpPortalConfig::description
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
                    CfpPortalConfig::baseUrl,
                    CfpPortalConfig::portalType,
                    CfpPortalConfig::description
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
      return Map.of(
          "cfps.portals.portal1.base-url", "https://portal1.example.com",
          "cfps.portals.portal1.portal-type", "CFP_DEV",
          "cfps.portals.portal1.description", "CFP Portal 1",
          "cfps.portals.portal2.base-url", "https://portal2.example.com",
          "cfps.portals.portal2.portal-type", "SESSIONIZE",
          "cfps.portals.portal2.description", "CFP Portal 2",
          "cfps.portals.portal3.base-url", "https://portal3.example.com",
          "cfps.portals.portal3.portal-type", "DEV2NEXT");
    }
  }
}