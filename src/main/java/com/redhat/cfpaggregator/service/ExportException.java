package com.redhat.cfpaggregator.service;

public class ExportException extends RuntimeException {
  public ExportException(String message) {
    super(message, null, true, false);
  }
}
