package com.redhat.cfpaggregator.ui.views;

import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

import com.redhat.cfpaggregator.service.CfpService;
import com.redhat.cfpaggregator.ui.MainLayout;

@Route(value = "events", layout = MainLayout.class)
public class SearchEventsView extends SplitLayout {
  private final CfpService cfpService;

  public SearchEventsView(CfpService cfpService) {
    this.cfpService = cfpService;
  }
}
