package com.redhat.cfpaggregator.ui.views;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.redhat.cfpaggregator.ui.views.EventViews.EventName;

public enum EventSortBy {
  EVENT_DATE("Event Date", Comparator.comparing(EventName::fromDate)),
  EVENT_NAME("Event Name", Comparator.comparing(EventName::name)),
  CFP_OPENING("CFP Open Date", Comparator.comparing(EventName::cfpClosing)),
  CFP_CLOSING("CFP Close Date", Comparator.comparing(EventName::cfpClosing));

  private final String name;
  private final Comparator<EventName> eventNameComparator;

  EventSortBy(String name, Comparator<EventName> eventNameComparator) {
    this.name = name;
    this.eventNameComparator = eventNameComparator;
  }

  public String getName() {
    return this.name;
  }

  public static Set<EventSortBy> valuesOrderedByName() {
    return Arrays.stream(values())
        .sorted(Comparator.comparing(EventSortBy::getName))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public Comparator<EventName> getEventNameComparator() {
    return this.eventNameComparator;
  }
}
