package com.redhat.cfpaggregator.ui.components;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

public final class SearchProgressBar extends VerticalLayout {
  private final BoldSpan label = new BoldSpan("Searching...");
  private final ProgressBar progressBar = new ProgressBar();

  public SearchProgressBar(FlexComponent parent) {
    super();

    setSpacing(false);
    setJustifyContentMode(JustifyContentMode.CENTER);

    getStyle()
        .set("position", "absolute")
        .set("z-index", "1000");
    setHeightFull();
    setWidth("min-content");
    setVisible(false);
    this.progressBar.setIndeterminate(true);

    add(this.label, this.progressBar);
    parent.getStyle().set("position", "relative");
    parent.add(this);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    this.label.setVisible(visible);
    this.progressBar.setVisible(visible);
  }
}
