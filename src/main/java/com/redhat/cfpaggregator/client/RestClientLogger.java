package com.redhat.cfpaggregator.client;

import static java.util.stream.Collectors.joining;

import java.util.Optional;
import java.util.stream.StreamSupport;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.client.api.ClientLogger;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

/**
 * Handles logging of HTTP client requests and responses.
 *
 * This class is created specifically for logging HTTP interactions performed
 * through REST clients. Logs are collected conditionally based on configuration
 * for requests and responses, and it supports different levels of verbosity
 * depending on the logger settings.
 *
 * Logging is performed at the INFO level and may include information about
 * HTTP method, URL, headers, and body (if available, and depending on the configurations).
 *
 * @author Eric Deandrea
 */
final class RestClientLogger implements ClientLogger {
  private static final Logger LOG = Logger.getLogger(RestClientLogger.class);

  private final String portalName;
  private final boolean logRequests;
  private final boolean logResponses;

  RestClientLogger(String portalName, boolean logRequests, boolean logResponses) {
    this.portalName = portalName;
    this.logRequests = logRequests;
    this.logResponses = logResponses;
  }

  @Override
  public void setBodySize(int bodySize) {
    // ignore
  }

  @Override
  public void logRequest(HttpClientRequest request, Buffer body, boolean omitBody) {
    if (logRequests && LOG.isInfoEnabled()) {
      try {
        LOG.infof(
            "Request to %s:\n- method: %s\n- url: %s\n- headers: %s\n- body: %s",
            this.portalName,
            request.getMethod(),
            request.absoluteURI(),
            inOneLine(request.headers()),
            bodyToString(body));
      } catch (Exception e) {
        LOG.warn("Failed to log request", e);
      }
    }
  }

  @Override
  public void logResponse(HttpClientResponse response, boolean redirect) {
    if (logResponses && LOG.isInfoEnabled()) {
      response.bodyHandler(body -> {
        try {
          LOG.infof(
              "Response from %s:\n- status code: %s\n- headers: %s\n- body: %s",
              this.portalName,
              response.statusCode(),
              inOneLine(response.headers()),
              bodyToString(body));
        } catch (Exception e) {
          LOG.warn("Failed to log response", e);
        }
      });
    }
  }

  private static String bodyToString(Buffer body) {
    return Optional.ofNullable(body)
        .map(Buffer::toString)
        .orElse("");
  }

  private static String inOneLine(MultiMap headers) {
    return StreamSupport.stream(headers.spliterator(), false)
        .map(header -> "[%s: %s]".formatted(header.getKey(), header.getValue()))
        .collect(joining(", "));
  }
}
