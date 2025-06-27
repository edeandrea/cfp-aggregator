package com.redhat.cfpaggregator.ui;

import static com.redhat.cfpaggregator.ui.PlaywrightTests.RECORD_DIR;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Files;

import jakarta.ws.rs.core.Response.Status;

import io.quarkus.test.common.http.TestHTTPResource;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;

@WithPlaywright(recordVideoDir = RECORD_DIR, slowMo = 500)
abstract class PlaywrightTests {
  protected static final String RECORD_DIR = "target/playwright";

  @InjectPlaywright
	protected BrowserContext context;

	@TestHTTPResource("/")
	URL index;

	protected Page loadPage(String testName) {
		var page = this.context.newPage();
		page.onClose(p -> saveVideoWithReadableName(p, testName));

		var response = page.navigate(this.index.toString());

		assertThat(response)
			.isNotNull()
			.extracting(Response::status)
			.isEqualTo(Status.OK.getStatusCode());

		page.waitForLoadState(LoadState.NETWORKIDLE);

		return page;
	}

	private void saveVideoWithReadableName(Page page, String testName) {
		var video = page.video();
		var path = video.path();

		if (Files.isRegularFile(path)) {
			path = path.getParent();
		}

		var saveAsPath = path.resolve("%s.webm".formatted(testName));

		// Save the video under a different filename
		video.saveAs(saveAsPath);

		// Delete the randomly generated one
		video.delete();
	}
}
