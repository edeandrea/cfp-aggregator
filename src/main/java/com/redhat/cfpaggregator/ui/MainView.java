package com.redhat.cfpaggregator.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.redhat.cfpaggregator.config.CfpPortalsConfig;
import com.redhat.cfpaggregator.domain.Event;
import com.redhat.cfpaggregator.domain.Speaker;
import com.redhat.cfpaggregator.domain.Talk;
import com.redhat.cfpaggregator.service.CfpService;
import com.redhat.cfpaggregator.ui.components.BoldSpan;
import com.redhat.cfpaggregator.ui.views.EventViews.EventName;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

@PageTitle("CFP Aggregator")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class MainView extends VerticalLayout {
  private static final DateTimeFormatter M_D_Y_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
  private static final DateTimeFormatter M_Y_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
  private final CfpService cfpService;
  private final CfpPortalsConfig config;
  private final Details eventDetails = new Details();
  private final Accordion speakers = new Accordion();

  public MainView(CfpService cfpService, CfpPortalsConfig config) {
    this.cfpService = cfpService;
    this.config = config;

    setSizeFull();
    getStyle().set("flex-grow", "1");
    add(createTitleRow());
    add(new Hr());
    add(createMainBody());
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

    var eventsSelector = createEventsSelector();
    var talkKeywordsDetails = createDefaultSearchCriteriaDetails("Talk keywords", this.config.defaultSearchCriteria().talkKeywords().stream());
    var companiesDetails = createDefaultSearchCriteriaDetails("Companies", this.config.defaultSearchCriteria().companies().stream());

    titleRow.setAlignSelf(FlexComponent.Alignment.START, companiesDetails);
    titleRow.setAlignSelf(FlexComponent.Alignment.START, eventsSelector);
    titleRow.setAlignSelf(FlexComponent.Alignment.START, talkKeywordsDetails);
    titleRow.add(eventsSelector);
    titleRow.add(companiesDetails);
    titleRow.add(talkKeywordsDetails);

    return titleRow;
  }

  private Details createDefaultSearchCriteriaDetails(String title, Stream<String> values) {
    var content = new VerticalLayout();
    content.setSpacing(false);
    content.setPadding(false);

    var defaultSearchCriteriaDetails = new Details();
    defaultSearchCriteriaDetails.setWidth("min-content");
    defaultSearchCriteriaDetails.setHeightFull();
    defaultSearchCriteriaDetails.setSummaryText(title);
    defaultSearchCriteriaDetails.setOpened(true);
    defaultSearchCriteriaDetails.setContent(content);

    values.map(BoldSpan::new)
        .forEach(content::add);

    return defaultSearchCriteriaDetails;
  }

  private Select<EventName> createEventsSelector() {
    var eventsSelector = new Select<EventName>();
    eventsSelector.setLabel("Events");
    eventsSelector.setPlaceholder("Select an event");
    eventsSelector.setTooltipText("Select an event to view speakers and talks for that event");
    eventsSelector.setWidth("min-content");
    eventsSelector.setHeightFull();
    eventsSelector.setItems(DataProvider.ofCollection(this.cfpService.getEventNamesOrderedByMostRecent()));
    eventsSelector.setItemLabelGenerator(event -> "%s (%s)".formatted(event.name(), M_Y_FORMATTER.withZone(ZoneId.of(event.timeZone())).format(event.fromDate())));
    eventsSelector.addValueChangeListener(event -> handleEventChanged(event.getValue()));

    return eventsSelector;
  }

  private void handleEventChanged(EventName newEvent) {
    var event = this.cfpService.getFullyPopulatedEvent(newEvent.portalName());
    this.eventDetails.removeAll();
    this.speakers.getChildren().forEach(this.speakers::remove);

    handleEventDetailsChange(event);
    handleSpeakersChange(event);
  }

  private void handleEventDetailsChange(Event newEvent) {
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
//    talkPanel.setAutoResponsive(true);
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
