package com.redhat.cfpaggregator.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Portal;

@QuarkusTest
class PortalMapperTests {
  @Inject
  PortalMapper mapper;

  @Inject
  CfpPortalsConfig config;

  @Test
  void fromConfig() {
    var portalName = this.config.portalNames().stream().findFirst().orElse(null);
    assertThat(portalName)
        .isNotBlank();

    var portalFromConfig = this.config.portals().get(portalName);

    assertThat(portalFromConfig).isNotNull();

   var expectedPortal = Portal.builder()
       .baseUrl(portalFromConfig.getBaseUrl())
       .description(portalFromConfig.getDescription().orElse(null))
       .portalType(portalFromConfig.getPortalType())
       .portalName(portalName)
       .build();

   assertThat(this.mapper.fromConfig(portalName, portalFromConfig))
       .isNotNull()
       .usingRecursiveComparison()
       .isEqualTo(expectedPortal);
  }
}