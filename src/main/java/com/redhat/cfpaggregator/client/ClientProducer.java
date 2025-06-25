package com.redhat.cfpaggregator.client;

import static com.redhat.cfpaggregator.config.CfpPortalsConfig.CfpPortalConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import io.quarkus.logging.Log;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

import com.redhat.cfpaggregator.client.cfpdev.CfpDevClient;
import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import io.smallrye.common.annotation.Identifier;

/**
 * Produces instances of clients for communicating with the portals defined in {@link CfpPortalsConfig#portals()}.
 *
 * @author Eric Deandrea
 */
@ApplicationScoped
public class ClientProducer {
  public static final String CFP_DEV_CLIENTS = "cfpDevClients";
  private final CfpPortalsConfig cfpPortalsConfig;

  public ClientProducer(CfpPortalsConfig cfpPortalsConfig) {
    this.cfpPortalsConfig = cfpPortalsConfig;
  }

  @Produces
  @Identifier(CFP_DEV_CLIENTS)
  public Map<String, CfpDevClient> cfpDevClients() {
    return this.cfpPortalsConfig.portals()
        .entrySet()
        .stream()
        .map(entry -> Map.entry(entry.getKey(), createCfpDevClient(entry.getKey(), entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private CfpDevClient createCfpDevClient(String portalName, CfpPortalConfig config) {
    Log.debugf("Creating client for portal %s", portalName);

    try {
      var builder = QuarkusRestClientBuilder.newBuilder()
          .baseUrl(new URL(config.baseUrl()))
          .connectTimeout(config.timeout().toSeconds(), TimeUnit.SECONDS)
          .readTimeout(config.timeout().toSeconds(), TimeUnit.SECONDS);

      if (config.logRequests() || config.logResponses()) {
        builder
            .loggingScope(LoggingScope.REQUEST_RESPONSE)
            .clientLogger(new RestClientLogger(portalName, config.logRequests(), config.logResponses()));
      }

      return builder.build(CfpDevClient.class);
    }
    catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
