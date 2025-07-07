package com.redhat.cfpaggregator.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import io.quarkus.logging.Log;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevClient;
import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.repository.PortalRepository;

/**
 * Produces instances of clients for communicating with portals.
 *
 * @author Eric Deandrea
 */
@ApplicationScoped
public class ClientProducer {
  public static final String CFP_DEV_CLIENTS = "cfpDevClients";
  public static final String PORTAL_NAME_HEADER = "X-Portal-Name";
  private final PortalRepository portalRepository;
  private final CfpPortalsConfig config;
  private Map<String, CfpDevClient> cfpDevClients = new ConcurrentHashMap<>();

  public ClientProducer(PortalRepository portalRepository, CfpPortalsConfig config) {
    this.portalRepository = portalRepository;
    this.config = config;
  }

  public CfpDevClient getCfpDevClient(Portal portal) {
    return this.cfpDevClients.computeIfAbsent(portal.getPortalName(), portalName -> createCfpDevClient(portal));
  }

  private CfpDevClient createCfpDevClient(Portal portal) {
    Log.debugf("Creating client for portal %s", portal.getPortalName());

    try {
      return createCfpDevClient(new URL(portal.getBaseUrl()), portal.getPortalName());
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private CfpDevClient createCfpDevClient(URL baseUrl, String portalName) {
    var builder = QuarkusRestClientBuilder.newBuilder()
        .baseUrl(baseUrl)
        .connectTimeout(this.config.timeout().toSeconds(), TimeUnit.SECONDS)
        .readTimeout(this.config.timeout().toSeconds(), TimeUnit.SECONDS);

    if (this.config.logRequests() || this.config.logResponses()) {
      builder
          .loggingScope(LoggingScope.REQUEST_RESPONSE)
          .clientLogger(new RestClientLogger(portalName, this.config.logRequests(), this.config.logResponses()));
    }

    return builder.build(CfpDevClient.class);
  }
}
