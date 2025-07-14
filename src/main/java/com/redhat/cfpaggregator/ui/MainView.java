package com.redhat.cfpaggregator.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.quarkus.logging.Log;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.Talk;
import com.redhat.cfpaggregator.domain.TalkSearchCriteria;
import com.redhat.cfpaggregator.mapping.EventMapper;
import com.redhat.cfpaggregator.service.CfpService;
import com.redhat.cfpaggregator.ui.components.BoldSpan;
import com.redhat.cfpaggregator.ui.components.EventDetailsForm;
import com.redhat.cfpaggregator.ui.components.ExportPortalConfigDialog;
import com.redhat.cfpaggregator.ui.components.SearchCriteriaDetails;
import com.redhat.cfpaggregator.ui.components.SearchProgressBar;
import com.redhat.cfpaggregator.ui.views.EventSortBy;
import com.redhat.cfpaggregator.ui.views.EventViews.EventName;
import com.redhat.cfpaggregator.ui.views.SearchCriteria;

@PageTitle("CFP Aggregator")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class MainView extends VerticalLayout {
  private static final DateTimeFormatter M_D_Y_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
  private static final DateTimeFormatter M_Y_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
  private final CfpService cfpService;
  private final CfpPortalsConfig config;
  private final EventMapper eventMapper;
  private final Details eventDetails = new Details();
  private final Accordion speakers = new Accordion();
  private final Select<EventSortBy> eventSortBy = new Select<>();
  private final Select<EventName> eventsSelector = new Select<>();
  private final ListDataProvider<SearchCriteria> companiesSearchCriteria;
  private final ListDataProvider<SearchCriteria> talkKeywordsSearchCriteria;
  private final Component searchProgress;
  private final Button editEventButton = new Button(VaadinIcon.EDIT.create());
  private final Button deleteEventButton = new Button(VaadinIcon.CLOSE.create());
  private ListDataProvider<EventName> eventDataProvider;

  public MainView(CfpService cfpService, CfpPortalsConfig config, EventMapper eventMapper) {
    this.cfpService = cfpService;
    this.config = config;
    this.eventMapper = eventMapper;
    this.talkKeywordsSearchCriteria = DataProvider.ofCollection(this.config.defaultSearchCriteria().talkKeywords().stream().map(SearchCriteria::new).collect(Collectors.toCollection(ArrayList::new)));
    this.companiesSearchCriteria = DataProvider.ofCollection(this.config.defaultSearchCriteria().companies().stream().map(SearchCriteria::new).collect(Collectors.toCollection(ArrayList::new)));

    var titleRow = createTitleRow();
    this.searchProgress = new SearchProgressBar(titleRow);

    setSizeFull();
    getStyle().set("flex-grow", "1");
    add(titleRow);
    add(new Hr());
    add(createMainBody());
    setupDefaultSelections();
  }

  private void setupDefaultSelections() {
    this.eventSortBy.setValue(EventSortBy.EVENT_DATE);
  }

  private VerticalLayout createMainBody() {
    var mainBody = new VerticalLayout();

    mainBody.getStyle().set("flex-grow", "1");
    mainBody.setSizeFull();
    mainBody.add(eventDetails);

    eventDetails.setWidthFull();
    eventDetails.setVisible(false);
    eventDetails.addThemeVariants(DetailsVariant.FILLED);

    speakers.setWidthFull();
    speakers.setVisible(false);

    var scroller = new Scroller(speakers);
    scroller.addClassNames(LumoUtility.Border.BOTTOM, LumoUtility.Padding.MEDIUM);
    scroller.setScrollDirection(ScrollDirection.VERTICAL);
    scroller.setHeightFull();
    scroller.setWidthFull();
    scroller.scrollToTop();
    mainBody.add(scroller);

    return mainBody;
  }

  private HorizontalLayout createTitleRow() {
    var titleRow = new HorizontalLayout();
    titleRow.addClassName(Gap.MEDIUM);
    titleRow.setWidthFull();
    titleRow.setHeight("min-content");
    titleRow.setAlignItems(Alignment.CENTER);
    titleRow.setJustifyContentMode(JustifyContentMode.CENTER);

    setupEventSortBySelector();
    var eventsSelector = createEventsSelector();
    var talkKeywordsDetails = new SearchCriteriaDetails("Talk keywords", this.talkKeywordsSearchCriteria);
    var companiesDetails = new SearchCriteriaDetails("Companies", this.companiesSearchCriteria);

    var addNewEventButton = new Button(VaadinIcon.FILE_ADD.create(), event -> handleAddNewEvent());
    addNewEventButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
    addNewEventButton.setTooltipText("Add a new event to the list of events");
    addNewEventButton.setWidth("min-content");

    var exportEventConfigButton = new Button(VaadinIcon.EXTERNAL_LINK.create(), event -> handleExportConfig());
    exportEventConfigButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
    exportEventConfigButton.setTooltipText("Export the current event configurations");
    exportEventConfigButton.setWidth("min-content");

    this.editEventButton.addClickListener(event -> handleEditEvent(eventsSelector.getValue()));
    this.editEventButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
    this.editEventButton.setTooltipText("Edit the the selected event");
    this.editEventButton.setEnabled(false);
    this.editEventButton.setWidth("min-content");

    this.deleteEventButton.addClickListener(event -> confirmDeleteEvent(eventsSelector.getValue()));
    this.deleteEventButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
    this.deleteEventButton.setTooltipText("Delete the the selected event");
    this.deleteEventButton.setEnabled(false);
    this.deleteEventButton.setWidth("min-content");

    var addEditDeleteEventLayout = new HorizontalLayout(exportEventConfigButton, addNewEventButton, this.editEventButton, this.deleteEventButton);
    addEditDeleteEventLayout.setWidthFull();
    addEditDeleteEventLayout.setSpacing(true);
    addEditDeleteEventLayout.setAlignItems(Alignment.CENTER);
    addEditDeleteEventLayout.setJustifyContentMode(JustifyContentMode.CENTER);

    var searchButton = new Button("Search", event -> handleSearchButtonClicked());
    searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    searchButton.setWidthFull();
    searchButton.setTooltipText("Search for events based on search criteria");

    var leftSide = new VerticalLayout();
    leftSide.setPadding(false);
    leftSide.setWidth("min-content");
    leftSide.setAlignItems(Alignment.START);
    leftSide.add(this.eventSortBy, eventsSelector, addEditDeleteEventLayout, searchButton);

    titleRow.setAlignSelf(Alignment.START, leftSide, companiesDetails, talkKeywordsDetails);
    titleRow.add(leftSide, companiesDetails, talkKeywordsDetails);

    return titleRow;
  }

  private void handleExportConfig() {
    new ExportPortalConfigDialog(this.cfpService.getPortals()).open();
  }

  private void confirmDeleteEvent(EventName event) {
    new ConfirmDialog(
        "Delete event %s?".formatted(event.name()),
        "Are you sure you want to delete the event %s?".formatted(event.name()),
        "Delete",
        confirmEvent -> handleDeleteEvent(event),
        "Cancel",
        cancelEvent -> {} // Do nothing on cancel
    ).open();
  }

  private void handleDeleteEvent(EventName value) {
    this.cfpService.deletePortal(value.portalName());
    this.eventsSelector.getListDataView().removeItem(value);
  }

  private void handleEditEvent(EventName value) {
    var originalPortal = this.cfpService.getPortal(value.portalName());

    originalPortal.ifPresentOrElse(
        p -> {
          var eventDetailsForm = new EventDetailsForm(false, this.cfpService, p);
          eventDetailsForm.onPortalSaved(this::handleExistingPortalSaved);
          eventDetailsForm.onPortalSaveCancelled(() -> handleExistingPortalSaveCancelled(p));
          eventDetailsForm.open();
        },
        () -> Notification.show("Portal %s not found".formatted(value.portalName()), (int) Duration.ofSeconds(3).toMillis(), Position.TOP_START)
    );
  }

  private void handleExistingPortalSaveCancelled(Portal originalPortal) {
    Log.debugf("Editing of portal %s was cancelled", originalPortal.getPortalName());
  }

  private void handleExistingPortalSaved(Portal updatedPortal) {
    var savedPortal = this.cfpService.savePortal(updatedPortal);
    Log.infof("Portal %s was updated", savedPortal);

    this.eventDataProvider.refreshItem(this.eventMapper.fromEvent(savedPortal.getEvent()));
  }

  private void handleNewPortalSaved(Portal newPortal) {
    var savedPortal = this.cfpService.createPortal(newPortal, createTalkSearchCriteria());
    Log.infof("Portal %s was created", savedPortal);

    var event = this.eventMapper.fromEvent(savedPortal.getEvent());
    this.eventsSelector.getListDataView().addItem(event);
    this.eventsSelector.setValue(event);
  }

  private void handleNewPortalSaveCancelled() {
    Log.debugf("New portal creation was cancelled");
  }

  private void handleAddNewEvent() {
    var eventDetailsForm = new EventDetailsForm(true, this.cfpService);
    eventDetailsForm.onPortalSaveCancelled(this::handleNewPortalSaveCancelled);
    eventDetailsForm.onPortalSaved(this::handleNewPortalSaved);
    eventDetailsForm.open();
  }

  private void performOnUI(Command command) {
    getUI().ifPresentOrElse(ui -> ui.access(command), () -> command.execute());
  }

  private void setupEventSortBySelector() {
    this.eventSortBy.setLabel("Sort events by");
    this.eventSortBy.setTooltipText("How to sort the events drop-down");
    this.eventSortBy.setWidthFull();
    this.eventSortBy.setItems(DataProvider.ofCollection(EventSortBy.valuesOrderedByName()));
    this.eventSortBy.setItemLabelGenerator(EventSortBy::getName);
    this.eventSortBy.addValueChangeListener(event -> handleEventSortByChanged(event.getValue()));
  }

  private Select<EventName> createEventsSelector() {
    this.eventDataProvider = DataProvider.ofCollection(this.cfpService.getEvents());

    this.eventsSelector.setLabel("Events");
    this.eventsSelector.setPlaceholder("Select an event");
    this.eventsSelector.setTooltipText("Select an event to view speakers and talks for that event");
    this.eventsSelector.setWidthFull();
    this.eventsSelector.setDataProvider(this.eventDataProvider);
    this.eventsSelector.setItemLabelGenerator(event -> "%s (%s)".formatted(event.name(), M_Y_FORMATTER.withZone(ZoneId.of(event.timeZone())).format(event.fromDate())));
    this.eventsSelector.addValueChangeListener(event -> handleEventChanged(event.getValue()));

    return eventsSelector;
  }

  private void handleSearchButtonClicked() {
    Consumer<? super TalkSearchCriteria> whenComplete = criteria -> performOnUI(() -> {
      this.eventDataProvider.refreshAll();
      this.eventsSelector.setValue(this.eventsSelector.getEmptyValue());
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

  private TalkSearchCriteria createTalkSearchCriteria() {
    return TalkSearchCriteria.builder()
        .speakerCompanies(this.companiesSearchCriteria.getItems().stream().map(SearchCriteria::getKeyword).toList())
        .talkKeywords(this.talkKeywordsSearchCriteria.getItems().stream().map(SearchCriteria::getKeyword).toList())
        .build();
  }

  private void handleEventSortByChanged(EventSortBy eventSortBy) {
    var currentEvent = this.eventsSelector.getValue();
    this.eventDataProvider.setSortComparator(eventSortBy.getEventNameComparator()::compare);
    this.eventsSelector.setValue(currentEvent);
  }

  private void handleEventChanged(EventName newEvent) {
    this.eventDetails.removeAll();
    this.speakers.getChildren().forEach(this.speakers::remove);

    this.eventDetails.setVisible(false);
    this.speakers.setVisible(false);

    if (newEvent != null) {
      var event = this.cfpService.getFullyPopulatedEvent(newEvent.portalName());
      handleEventDetailsChange(event);
      handleSpeakersChange(event);
    }
    else {
      this.editEventButton.setEnabled(false);
      this.deleteEventButton.setEnabled(false);
    }
  }

  private void handleEventDetailsChange(Event newEvent) {
    this.editEventButton.setEnabled(newEvent != null);
    this.deleteEventButton.setEnabled(newEvent != null);
    this.eventDetails.setSummaryText("%s (%d talks by %d speakers)".formatted(newEvent.getName(), newEvent.getTalkCount(), newEvent.getSpeakerCount()));
    var content = new FormLayout();
    content.setAutoResponsive(true);
    content.setLabelsAside(true);
    content.setLabelSpacing("2em");
    this.eventDetails.setContent(content);
    this.eventDetails.setVisible(true);
    this.eventDetails.setOpened(true);

    optionallyAddToDetails(content, newEvent.getDescription(), "Description: ");
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

  public static boolean containsHtmlTags(String text) {
    return Optional.ofNullable(text)
        .map(t -> t.matches(".*<[^>]+>.*"))
        .orElse(false);
  }
}
