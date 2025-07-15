package com.redhat.cfpaggregator.ui.components;

public class ExportException extends RuntimeException {
  public ExportException(String message) {
    super(message, null, true, false);
  }
}
