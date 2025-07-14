package com.redhat.cfpaggregator.ui.components;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Consumer;

import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.PortalType;
import com.redhat.cfpaggregator.service.CfpService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.RegexpValidator;

public final class EventDetailsForm extends Dialog {
  private final TextField portalNameField = new TextField("Portal Name");
  private final TextField baseUrlField = new TextField("Base URL");
  private final TextArea descriptionField = new TextArea("Description");
  private final Select<PortalType> portalTypeField = new Select<>();
  private final Button saveButton = new Button("OK");
  private final Button cancelButton = new Button("Cancel");
  private final Binder<Portal> binder = new BeanValidationBinder<>(Portal.class);
  private final CfpService cfpService;
  private Portal portal;
  private Consumer<Portal> onPortalSaved;
  private Runnable onPortalSaveCancelled;

  public EventDetailsForm(boolean isNewPortal, CfpService cfpService) {
    this(isNewPortal, cfpService, null);
  }

  public EventDetailsForm(boolean isNewPortal, CfpService cfpService, Portal portal) {
    super(isNewPortal ? "New Portal" : "Portal Details");
    this.cfpService = cfpService;

    this.portal = (portal != null ? portal : new Portal());

    configureFields(isNewPortal);
    configureBinder();
    configureButtons();

    var formLayout = new FormLayout();
    formLayout.add(portalNameField, baseUrlField, descriptionField, portalTypeField);
    formLayout.setResponsiveSteps(
        new ResponsiveStep("0", 1),
        new ResponsiveStep("500px", 2)
    );

    var buttonLayout = new HorizontalLayout(saveButton, cancelButton);
    buttonLayout.getStyle().set("flex-wrap", "wrap");
    buttonLayout.setJustifyContentMode(JustifyContentMode.END);

    setModal(true);
    setCloseOnEsc(true);
    setDraggable(false);
    setResizable(false);
    getFooter().add(buttonLayout);
    add(formLayout);

    setWidth("600px");
    setHeight("auto");
  }

  private void configureFields(boolean isNewPortal) {
    this.portalNameField.setReadOnly(!isNewPortal);
    this.portalNameField.setRequired(true);
    this.baseUrlField.setRequired(true);

    var portalTypeDataProvider = DataProvider.ofCollection(EnumSet.allOf(PortalType.class));
    portalTypeDataProvider.setSortComparator(Comparator.comparing(PortalType::getDescription)::compare);
    this.portalTypeField.setLabel("Portal Type");
    this.portalTypeField.setDataProvider(portalTypeDataProvider);
    this.portalTypeField.setItemLabelGenerator(PortalType::getDescription);
    this.portalTypeField.setRequiredIndicatorVisible(true);

    this.portalNameField.setWidthFull();
    this.baseUrlField.setWidthFull();
    this.descriptionField.setWidthFull();
    this.descriptionField.setHeight("100px");
    this.portalTypeField.setWidthFull();
  }

  private void configureBinder() {
    this.binder.forField(this.portalNameField)
        .asRequired("Portal name is required")
        .withValidator(new PortalNameValidator(this.cfpService))
        .bind(Portal::getPortalName, Portal::setPortalName);

    this.binder.forField(this.baseUrlField)
        .asRequired("Base URL is required")
        .withValidator(new RegexpValidator("Base URL MUST be a valid URL", "^https?://(?:localhost|(?:[a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+|(?:\\d{1,3}\\.){3}\\d{1,3})(?::\\d{1,5})?(?:/.*)?$"))
        .bind(Portal::getBaseUrl, Portal::setBaseUrl);

    this.binder.bind(this.descriptionField, Portal::getDescription, Portal::setDescription);
    this.binder.forField(this.portalTypeField)
        .asRequired("Portal type is required")
        .bind(Portal::getPortalType, Portal::setPortalType);

    this.binder.addStatusChangeListener(event -> this.saveButton.setEnabled(event.getBinder().isValid()));
    this.binder.setBean(this.portal);
  }

  private void configureButtons() {
    this.saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    this.saveButton.addClickShortcut(Key.ENTER);
    this.saveButton.addClickListener(event -> validateAndSave());
    this.cancelButton.addClickListener(event -> {
      this.portal = null;
      close();
      this.onPortalSaveCancelled.run();
    });
  }

  private void validateAndSave() {
    try {
      this.binder.writeBean(this.portal);
      close();
      this.onPortalSaved.accept(this.portal);
    }
    catch (ValidationException e) {
      // Eat it on purpose as the UI will display the errors
    }
  }

  public void onPortalSaved(Consumer<Portal> onPortalSaved) {
    this.onPortalSaved = onPortalSaved;
  }

  public void onPortalSaveCancelled(Runnable onPortalSaveCancelled) {
    this.onPortalSaveCancelled = onPortalSaveCancelled;
  }

  private static class PortalNameValidator extends AbstractValidator<String> {
    private final CfpService cfpService;

    public PortalNameValidator(CfpService cfpService) {
      super("Portal name {0} already exists");
      this.cfpService = cfpService;
    }

    @Override
    public ValidationResult apply(String value, ValueContext context) {
      return this.cfpService.doesPortalNameExist(value) ?
          ValidationResult.error(getMessage(value)) :
          ValidationResult.ok();
    }
  }
}
