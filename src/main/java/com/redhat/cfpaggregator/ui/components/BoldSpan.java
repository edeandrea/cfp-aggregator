package com.redhat.cfpaggregator.ui.components;

import com.vaadin.flow.component.html.Span;

public class BoldSpan extends Span {
  public BoldSpan(String text) {
    super(text);
    getElement().getStyle().set("font-weight", "bold");
  }
}
