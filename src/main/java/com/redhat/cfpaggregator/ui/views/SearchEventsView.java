package com.redhat.cfpaggregator.ui.views;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;

import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.Talk;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;
import com.redhat.cfpaggregator.service.CfpService;
import com.redhat.cfpaggregator.ui.MainLayout;
import com.redhat.cfpaggregator.ui.components.BoldSpan;
import com.redhat.cfpaggregator.ui.components.SearchCriteriaDetails;
import com.redhat.cfpaggregator.ui.components.SearchProgressBar;

@Route(value = "events", layout = MainLayout.class)
public class SearchEventsView extends SplitLayout {
  private static final DateTimeFormatter M_D_Y_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
  private final CfpPortalsConfig config;
  private final CfpService cfpService;
  private final ListDataProvider<SearchCriteria> companiesSearchCriteria;
  private final ListDataProvider<SearchCriteria> talkKeywordsSearchCriteria;
  private final VerticalLayout eventDetails = new VerticalLayout();
  private final Accordion speakers = new Accordion();
  private Component searchProgress;
  private Grid<Event> eventsGrid;

  public SearchEventsView(CfpPortalsConfig config, CfpService cfpService) {
    super(Orientation.HORIZONTAL);

    this.config = config;
    this.cfpService = cfpService;
    this.talkKeywordsSearchCriteria = DataProvider.ofCollection(this.config.defaultSearchCriteria().talkKeywords().stream().map(SearchCriteria::new).collect(Collectors.toCollection(ArrayList::new)));
    this.companiesSearchCriteria = DataProvider.ofCollection(this.config.defaultSearchCriteria().companies().stream().map(SearchCriteria::new).collect(Collectors.toCollection(ArrayList::new)));

    addToPrimary(createSearchCriteriaView());
    addToSecondary(createSearchResultsView());

    setSplitterPosition(15);
  }

  private Component createSearchCriteriaView() {
    var layout = new VerticalLayout();

    var talkKeywordsDetails = new SearchCriteriaDetails("Talk keywords", this.talkKeywordsSearchCriteria);
    var companiesDetails = new SearchCriteriaDetails("Speaker Companies", this.companiesSearchCriteria);

    var searchButton = new Button("Search", e -> handleSearchButtonClicked());
    searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    searchButton.setWidthFull();
    searchButton.setTooltipText("Search for events based on search criteria");

    layout.setWidth("min-content");
    layout.setAlignItems(Alignment.START);
    layout.add(companiesDetails, talkKeywordsDetails, searchButton);

    return layout;
  }

  private Component createSearchResultsView() {
    this.eventsGrid = createEventsGrid();
    this.eventDetails.setSizeFull();
    this.eventDetails.setVisible(false);
    this.speakers.setWidthFull();
    this.speakers.setVisible(false);

    var eventsLayout = new VerticalLayout(new H4("Events"), this.eventsGrid);
    eventsLayout.setWidthFull();

    var layout = new SplitLayout(Orientation.VERTICAL);
    layout.addToPrimary(eventsLayout);
    layout.addToSecondary(this.eventDetails);
    layout.setSplitterPosition(40);

    this.searchProgress = new SearchProgressBar(eventsLayout);

    return layout;
  }

  private Grid<Event> createEventsGrid() {
    var grid = new Grid<>(Event.class, false);
    grid.setSelectionMode(SelectionMode.SINGLE);
    grid.setColumnReorderingAllowed(true);
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    grid.setEmptyStateText("No events found. Please use the search on the left to find events.");

    grid.addColumn(Event::getPortalName)
        .setHeader("Portal Name")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0);

    grid.addColumn(Event::getName)
        .setHeader("Event Name")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0);

    var fromDateColumn = grid.addColumn(new LocalDateRenderer<>(Event::getLocalFromDate))
        .setHeader("Event Date")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0)
        .setComparator(Event::getLocalFromDate);

    grid.addColumn(new LocalDateRenderer<>(Event::getLocalCfpOpening))
        .setHeader("CFP Opening")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0)
        .setComparator(Event::getLocalCfpOpening);

    grid.addColumn(new LocalDateRenderer<>(Event::getLocalCfpClosing))
        .setHeader("CFP Closing")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0)
        .setComparator(Event::getLocalCfpClosing);

    var numTalksColumn = grid.addColumn(Event::getTalkCount)
        .setHeader("# Talks")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0);
    numTalksColumn.setKey("numTalksColumn");

    var numSpeakersColumn = grid.addColumn(Event::getSpeakerCount)
        .setHeader("# Speakers")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0);
    numSpeakersColumn.setKey("numSpeakersColumn");

    grid.addSelectionListener(this::handleEventSelection);
    grid.sort(GridSortOrder.asc(fromDateColumn).build());

    return grid;
  }

  private void handleEventSelection(SelectionEvent<Grid<Event>, Event> event) {
    event.getFirstSelectedItem().ifPresent(selectedEvent -> {
      this.eventDetails.setVisible(false);
      this.speakers.setVisible(false);
      this.eventDetails.removeAll();
      this.speakers.getChildren().forEach(this.speakers::remove);
      handleEventDetailsChange(selectedEvent);
      handleSpeakersChange(selectedEvent);
    });
  }

  private void handleEventDetailsChange(Event newEvent) {
    this.eventDetails.add(new H4("%s (%d talks by %d speakers)".formatted(newEvent.getName(), newEvent.getTalkCount(), newEvent.getSpeakerCount())));
    var content = new FormLayout();
    content.setAutoResponsive(true);
    content.setLabelsAside(true);
    content.setLabelSpacing("2em");
    this.eventDetails.add(content);
    this.eventDetails.add(this.speakers);
    this.eventDetails.setVisible(true);

    optionallyAddToDetails(content, newEvent.getEventOrPortalDescription(), "Description: ");
    optionallyAddToDetails(content, newEvent.getWebsiteUrl(), "Website URL: ");
    optionallyAddToDetails(content, newEvent.getYouTubeUrl(), "YouTube URL: ");
    optionallyAddToDetails(content, newEvent.getFlickrUrl(), "Flickr URL: ");
    optionallyAddToDetails(content, formatAsHumanReadable(newEvent.getFromDate(), newEvent.getTimeZone()), "Start Date: ");
    optionallyAddToDetails(content, formatAsHumanReadable(newEvent.getToDate(), newEvent.getTimeZone()), "End Date: ");
    optionallyAddToDetails(content, formatAsHumanReadable(newEvent.getCfpOpening(), newEvent.getTimeZone()), "CFP Opening Date: ");
    optionallyAddToDetails(content, formatAsHumanReadable(newEvent.getCfpClosing(), newEvent.getTimeZone()), "CFP Closing Date: ");
  }

  private void handleSpeakersChange(Event newEvent) {
    this.speakers.setVisible(true);

    newEvent.getSpeakers()
        .stream()
        .sorted(Comparator.comparing(Speaker::getLastName))
        .forEach(this::createSpeakerPanel);

    this.speakers.close();
  }

  private TalkSearchCriteria createTalkSearchCriteria() {
    return TalkSearchCriteria.builder()
        .speakerCompanies(this.companiesSearchCriteria.getItems().stream().map(SearchCriteria::getKeyword).toList())
        .talkKeywords(this.talkKeywordsSearchCriteria.getItems().stream().map(SearchCriteria::getKeyword).toList())
        .build();
  }

  private void handleSearchButtonClicked() {
    Consumer<? super TalkSearchCriteria> whenComplete = criteria -> performOnUI(() -> {
      var events = this.cfpService.getFullyPopulatedEvents();
      var totalSpeakerCount = events.stream()
          .mapToInt(Event::getSpeakerCount)
          .sum();
      var totalTalkCount = events.stream()
          .mapToInt(Event::getTalkCount)
          .sum();

      this.eventsGrid.getColumnByKey("numSpeakersColumn")
          .setHeader("# Speakers (" + totalSpeakerCount + ")");

      this.eventsGrid.getColumnByKey("numTalksColumn")
          .setHeader("# Talks (" + totalTalkCount + ")");

      this.eventsGrid.setItems(events);
      this.eventsGrid.recalculateColumnWidths();
      this.searchProgress.setVisible(false);
    });

    Consumer<? super Throwable> whenError = error -> performOnUI(() -> {
      whenComplete.accept(null);
      Notification.show("Error: %s".formatted(error.getMessage()), (int) Duration.ofSeconds(3).toMillis(), Position.TOP_CENTER);
    });

    this.searchProgress.setVisible(true);

    Uni.createFrom().item(createTalkSearchCriteria())
        .invoke(this.cfpService::recreateEvents)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .subscribe().with(whenComplete, whenError);
  }

  private void createSpeakerPanel(Speaker speaker) {
    var speakerPanel = new Accordion();
    speakerPanel.setWidthFull();

    speaker.getTalks()
        .stream()
        .sorted(Comparator.comparing(Talk::getTitle))
        .forEach(talk -> createTalkPanel(talk, speakerPanel));

    speakerPanel.close();
    this.speakers.add("%s (%d)".formatted(speaker.getFullName(), speaker.getTalkCount()), speakerPanel)
        .addThemeVariants(DetailsVariant.FILLED);
  }

  private void createTalkPanel(Talk talk, Accordion speakerPanel) {
    var talkPanel = new FormLayout();
    talkPanel.setLabelsAside(true);
    talkPanel.setLabelSpacing("2em");

    optionallyAddToDetails(talkPanel, talk.getVideoUrl(), "Video URL: ");
    optionallyAddToDetails(talkPanel, talk.getDescription(), "Description: ");
    optionallyAddToDetails(talkPanel, talk.getSummary(), "Summary: ");

    speakerPanel.add(talk.getTitle(), talkPanel)
        .addThemeVariants(DetailsVariant.FILLED);
  }

  private void performOnUI(Command command) {
    getUI().ifPresentOrElse(ui -> ui.access(command), () -> command.execute());
  }

  public boolean containsHtmlTags(String text) {
    return Optional.ofNullable(text)
        .map(t -> t.matches(".*<[^>]+>.*"))
        .orElse(false);
  }

  private String formatAsHumanReadable(Instant instant, String timeZone) {
    if ((instant == null) || (timeZone == null)) {
      return null;
    }

    return M_D_Y_FORMATTER
        .withZone(ZoneId.of(timeZone))
        .format(instant);
  }

  private <T> void optionallyAddToDetails(FormLayout parent, T value, String label) {
    Optional.ofNullable(value)
        .map(Object::toString)
        .ifPresent(val -> {
              var labelSpan = new BoldSpan(label);
              labelSpan.getStyle()
                  .set("white-space", "nowrap")
                  .set("overflow", "visible");

              var isLink = val.startsWith("http");
              var isHtml = !isLink && containsHtmlTags(val);
              var valComponent = isLink ?
                  new Anchor(val, val, AnchorTarget.BLANK) :
                  isHtml ?
                      new Html("<div>%s</div>".formatted(val)) :
                      new Span(val);

              valComponent.getStyle().set("white-space", "normal");
              parent.addFormItem(valComponent, labelSpan);
            }
        );
  }
}
