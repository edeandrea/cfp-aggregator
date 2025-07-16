package com.redhat.cfpaggregator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@Disabled
@QuarkusTest
@WireMockRecorder(baseUrl = "https://sessionize.com", portalName = "jl4ktls0")
public class CfpRecorder {
  @Test
  void record() {

  }
}
