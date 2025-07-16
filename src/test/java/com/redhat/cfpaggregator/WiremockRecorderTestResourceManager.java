package com.redhat.cfpaggregator;

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;

import jakarta.ws.rs.core.HttpHeaders;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.redhat.cfpaggregator.client.CfpClient;

public class WiremockRecorderTestResourceManager implements QuarkusTestResourceConfigurableLifecycleManager<WireMockRecorder> {
  private static final String MAPPINGS_DIR_NAME = "mappings";
  private static final String FILES_DIR_NAME = "__files";
  private static final Path ROOT_PATH = Path.of("src/test/resources");
  private final WireMockServer wiremockServer = new WireMockServer(wireMockConfig().dynamicPort());
  private WireMockRecorder recorder;

  @Override
  public Map<String, String> start() {
    var rootSource = new SingleRootFileSource(ROOT_PATH.resolve("wiremock").toString());
    this.wiremockServer.enableRecordMappings(rootSource.child(MAPPINGS_DIR_NAME), rootSource.child(FILES_DIR_NAME));
    this.wiremockServer.start();
    this.wiremockServer.startRecording(
        recordSpec()
            .forTarget(this.recorder.baseUrl())
            .captureHeader(CfpClient.PORTAL_NAME_HEADER)
            .captureHeader(HttpHeaders.ACCEPT)
            .captureHeader(HttpHeaders.USER_AGENT)
            .ignoreRepeatRequests()
            .makeStubsPersistent(true)
            .build()
    );

    return Map.of("cfps.portals.%s.base-url".formatted(this.recorder.portalName()), "http://localhost:%d".formatted(this.wiremockServer.port()));
  }

  @Override
  public void stop() {
    this.wiremockServer.stopRecording();
    this.wiremockServer.stop();

    // Now clean up the directories
    deleteDirectory(ROOT_PATH.resolve(MAPPINGS_DIR_NAME));
    deleteDirectory(ROOT_PATH.resolve(FILES_DIR_NAME));
    findAndRenameFile(ROOT_PATH.resolve("wiremock").resolve(MAPPINGS_DIR_NAME), "-search.json");
    findAndRenameFile(ROOT_PATH.resolve("wiremock").resolve(FILES_DIR_NAME), "-search.json");
  }

  private void findAndRenameFile(Path dir, String extension) {
    try {
      Files.list(dir)
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().contains(this.recorder.portalName()))
          .findFirst()
          .ifPresent(file -> {
            try {
              Files.move(file, file.resolveSibling("%s%s".formatted(this.recorder.portalName(), extension)));
            }
            catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void deleteDirectory(Path path) {
    try (var paths = Files.walk(path)) {
      paths.sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
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
