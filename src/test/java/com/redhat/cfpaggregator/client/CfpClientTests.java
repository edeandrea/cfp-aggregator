package com.redhat.cfpaggregator.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkiverse.wiremock.devservice.ConnectWireMock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.mapping.PortalMapper;
import com.redhat.cfpaggregator.repository.PortalRepository;

@ConnectWireMock
public abstract class CfpClientTests<T extends CfpClient> {
  private final Class<T> clientClass;

  @Inject
  protected ClientManager clientManager;

  @Inject
  protected PortalRepository portalRepository;

  @Inject
  protected PortalMapper portalMapper;

  @Inject
  protected CfpPortalsConfig cfpPortalsConfig;

  protected WireMock wireMock;

  protected CfpClientTests(Class<T> clientClass) {
    this.clientClass = clientClass;
  }

  public abstract void createEventNoKeywordsNoSpeakers() throws MalformedURLException;

  protected T getClient() {
    return getClient("portal1");
  }

  protected T getClient(String portalName) {
    return getClient(this.portalRepository.findById(portalName));
  }

  protected T getClient(Portal portal) {
    return (T) assertThat(this.clientManager.getCfpClient(portal))
        .isNotNull()
        .isInstanceOf(this.clientClass)
        .actual();
  }

  @BeforeEach
  public void beforeEach() {
    this.wireMock.resetToDefaultMappings();
    this.portalRepository.deleteAllWithCascade();

    this.cfpPortalsConfig.portals()
        .entrySet()
        .stream()
        .map(entry -> this.portalMapper.fromConfig(entry.getKey(), entry.getValue()))
        .forEach(this.portalRepository::persistAndFlush);

    assertThat(this.portalRepository.count()).isGreaterThanOrEqualTo(1);
  }

  @Test
  void hasCorrectClients() {
    assertThat(this.portalRepository.listAll())
        .isNotNull()
        .filteredOn(Portal::getPortalName, "portal1")
        .map(this::getClient)
        .singleElement()
        .isInstanceOf(this.clientClass);
  }
}
