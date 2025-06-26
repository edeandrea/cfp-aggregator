package com.redhat.cfpaggregator.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;

@QuarkusTest
class TalkSearchCriteriaMapperTests {
  @Inject
  TalkSearchCriteriaMapper mapper;

  @Inject
  CfpPortalsConfig config;

  @Test
  void talkSearchCriteriaFromConfig() {
    assertThat(this.mapper.fromConfig(config.defaultSearchCriteria()))
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(
            TalkSearchCriteria.builder()
                .talkKeywords("Quarkus")
                .speakerCompanies("Red Hat")
                .build()
        );
  }
}