package com.redhat.cfpaggregator.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;
import com.redhat.cfpaggregator.repository.PortalRepository;

/**
 * Produces instances of clients for communicating with portals.
 *
 * @author Eric Deandrea
 */
@ApplicationScoped
public class ClientManager {
  private final PortalRepository portalRepository;
  private final CfpPortalsConfig config;
  private Map<String, CfpClient> cfpClients = new ConcurrentHashMap<>();

  public ClientManager(PortalRepository portalRepository, CfpPortalsConfig config) {
    this.portalRepository = portalRepository;
    this.config = config;
  }

  public void clearClient(Portal portal) {
    this.cfpClients.remove(portal.getPortalName());
  }

  public Event createEvent(Portal portal, TalkSearchCriteria searchCriteria) {
    return getCfpClient(portal)
        .createEvent(portal, searchCriteria);
  }

  public CfpClient getCfpClient(Portal portal) {
    return this.cfpClients.computeIfAbsent(portal.getPortalName(), portalName -> createCfpClient(portal));
  }

  private CfpClient createCfpClient(Portal portal) {
    try {
      var builder = QuarkusRestClientBuilder.newBuilder()
          .baseUrl(new URL(portal.getBaseUrl()))
          .connectTimeout(this.config.timeout().toSeconds(), TimeUnit.SECONDS)
          .readTimeout(this.config.timeout().toSeconds(), TimeUnit.SECONDS);

      if (this.config.logRequests() || this.config.logResponses()) {
        builder
            .loggingScope(LoggingScope.REQUEST_RESPONSE)
            .clientLogger(new RestClientLogger(portal.getPortalName(), this.config.logRequests(), this.config.logResponses()));
      }

      return builder.build(portal.getPortalType().getClientClass());
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
