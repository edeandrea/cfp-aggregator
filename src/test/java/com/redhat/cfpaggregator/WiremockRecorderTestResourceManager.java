package com.redhat.cfpaggregator;

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.Map;

import jakarta.ws.rs.core.HttpHeaders;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.redhat.cfpaggregator.client.ClientProducer;

public class WiremockRecorderTestResourceManager implements QuarkusTestResourceConfigurableLifecycleManager<WireMockRecorder> {
  private final WireMockServer wiremockServer = new WireMockServer(wireMockConfig().dynamicPort());
  private WireMockRecorder recorder;

  @Override
  public Map<String, String> start() {
    this.wiremockServer.start();
    this.wiremockServer.startRecording(
        recordSpec()
            .forTarget(this.recorder.baseUrl())
            .captureHeader(ClientProducer.PORTAL_NAME_HEADER)
            .captureHeader(HttpHeaders.ACCEPT)
            .captureHeader(HttpHeaders.USER_AGENT)
            .ignoreRepeatRequests()
    );

    return Map.of("cfps.portals.%s.base-url".formatted(this.recorder.portalName()), "http://localhost:%d".formatted(this.wiremockServer.port()));
  }

  @Override
  public void stop() {
    this.wiremockServer.stopRecording();
    this.wiremockServer.stop();
  }

  @Override
  public void inject(TestInjector testInjector) {
    testInjector.injectIntoFields(this.wiremockServer, new TestInjector.MatchesType(WireMockServer.class));
  }

  @Override
  public void init(WireMockRecorder recorder) {
    this.recorder = recorder;
  }
}
