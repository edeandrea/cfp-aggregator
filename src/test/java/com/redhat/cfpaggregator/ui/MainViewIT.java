package com.redhat.cfpaggregator.ui;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;

import com.microsoft.playwright.assertions.PlaywrightAssertions;

@Disabled
@QuarkusIntegrationTest
public class MainViewIT extends PlaywrightTests {
  @Test
  void pageLoads() {
    var page = loadPage("%s_pageLoads".formatted(getClass().getSimpleName()));

    PlaywrightAssertions.assertThat(page).hasTitle("CFP Aggregator");
  }
}
