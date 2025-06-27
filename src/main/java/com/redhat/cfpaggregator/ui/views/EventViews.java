package com.redhat.cfpaggregator.ui.views;

import java.time.Instant;

public final class EventViews {
  public record EventName(String portalName, String name, String timeZone, Instant fromDate) {}
}
