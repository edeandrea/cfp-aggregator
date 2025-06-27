package com.redhat.cfpaggregator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@Disabled
@QuarkusTest
@WireMockRecorder(baseUrl = "https://dvma25.cfp.dev", portalName = "dvma25")
public class CfpRecorder {
  @Test
  void record() {

  }
}
