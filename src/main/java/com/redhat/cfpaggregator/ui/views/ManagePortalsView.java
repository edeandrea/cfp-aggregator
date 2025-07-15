package com.redhat.cfpaggregator.ui.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.router.Route;

import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.domain.PortalType;
import com.redhat.cfpaggregator.service.CfpService;
import com.redhat.cfpaggregator.ui.MainLayout;
import com.redhat.cfpaggregator.ui.components.ExportPortalConfigDialog;

@Route(value = "portals", layout = MainLayout.class)
public class ManagePortalsView extends VerticalLayout {
  private final CfpService cfpService;
  private final Grid<PortalWrapper> grid;
  private final List<PortalWrapper> portals;

  public ManagePortalsView(CfpService cfpService) {
    this.cfpService = cfpService;

    setPadding(true);

    var addNewPortalButton = new Button("Add New Portal", e -> handleAddNewPortal());
    addNewPortalButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var exportConfigButton = new Button("Export Config", e -> handleExportConfig());
    exportConfigButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var topBar = new HorizontalLayout(addNewPortalButton, exportConfigButton);
    topBar.setWidthFull();
    add(topBar);

    this.portals = getPortals();
    this.grid = createGrid();
    this.grid.setItems(this.portals);

    add(this.grid);
    setSizeFull();
  }

  private List<PortalWrapper> getPortals() {
    return this.cfpService.getPortals()
        .stream()
        .map(PortalWrapper::of)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private Grid<PortalWrapper> createGrid() {
    var grid = new Grid<>(PortalWrapper.class, false);
    grid.setSelectionMode(SelectionMode.SINGLE);
    grid.setColumnReorderingAllowed(true);
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    grid.setEmptyStateText("No portals found. Please configure some.");

    var editor = grid.getEditor();

    var editColumn = grid.addComponentColumn(portal -> {
              var editButton = new Button(VaadinIcon.EDIT.create(), e -> {
                if (editor.isOpen()) {
                  editor.cancel();
                }

                editor.editItem(portal);
              });

              var deleteButton = new Button(VaadinIcon.TRASH.create(), e -> {
                if (editor.isOpen()) {
                  editor.cancel();
                }

                handleDeletePortal(portal);
              });

              editButton.setTooltipText("Edit portal %s".formatted(portal.getPortalName()));
              deleteButton.setTooltipText("Delete portal %s".formatted(portal.getPortalName()));

              var layout = new HorizontalLayout(editButton, deleteButton);
              layout.setPadding(false);
              return layout;
            }
        )
        .setWidth("140px")
        .setFlexGrow(0);


    var portalNameColumn = grid.addColumn(PortalWrapper::getPortalName)
        .setHeader("Portal Name")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0);

    var baseUrlColumn = grid.addColumn(PortalWrapper::getBaseUrl)
        .setHeader("Base URL")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0);

    var portalTypeColumn = grid.addColumn(portal -> Optional.ofNullable(portal.getPortalType()).map(PortalType::getDescription).orElse(""))
        .setHeader("Portal Type")
        .setResizable(true)
        .setSortable(true)
        .setAutoWidth(true)
        .setFlexGrow(0);

    var descriptionColumn = grid.addColumn(PortalWrapper::getDescription)
        .setHeader("Description")
        .setResizable(true)
        .setAutoWidth(true)
        .setFlexGrow(1);

    var binder = new BeanValidationBinder<>(PortalWrapper.class);
    editor.setBinder(binder);
    editor.setBuffered(true);

    var portalNameField = new TextField();
    portalNameField.setWidthFull();
    portalNameField.setRequiredIndicatorVisible(true);
    portalNameField.setRequired(true);
    portalNameField.setReadOnly(true);
    binder.bindReadOnly(portalNameField, PortalWrapper::getPortalName);
    portalNameColumn.setEditorComponent(portalNameField);

    var baseUrlField = new TextField();
    baseUrlField.setWidthFull();
    baseUrlField.setRequiredIndicatorVisible(true);
    baseUrlField.setRequired(true);
    binder.forField(baseUrlField)
        .asRequired("Base URL is required")
        .withValidator(new RegexpValidator("Base URL MUST be a valid URL", "^https?://(?:localhost|(?:[a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+|(?:\\d{1,3}\\.){3}\\d{1,3})(?::\\d{1,5})?(?:/.*)?$"))
        .bind(PortalWrapper::getBaseUrl, PortalWrapper::setBaseUrl);
    baseUrlColumn.setEditorComponent(baseUrlField);

    var portalTypeListDataProvider = DataProvider.ofCollection(EnumSet.allOf(PortalType.class));
    portalTypeListDataProvider.setSortComparator(Comparator.comparing(PortalType::getDescription)::compare);

    var portalTypeField = new Select<PortalType>();
    portalTypeField.setDataProvider(portalTypeListDataProvider);
    portalTypeField.setItemLabelGenerator(PortalType::getDescription);
    portalTypeField.setRequiredIndicatorVisible(true);

    binder.forField(portalTypeField)
        .asRequired("Portal type is required")
        .bind(PortalWrapper::getPortalType, PortalWrapper::setPortalType);
    portalTypeColumn.setEditorComponent(portalTypeField);

    var descriptionField = new TextField();
    descriptionField.setWidthFull();
    binder.bind(descriptionField, PortalWrapper::getDescription, PortalWrapper::setDescription);
    descriptionColumn.setEditorComponent(descriptionField);

    var saveButton = new Button(VaadinIcon.SAFE.create(), e -> handleSaveEdit(editor));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.setTooltipText("Save changes");

    var cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> handleCancelEdit(editor));
    cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
    cancelButton.setTooltipText("Cancel changes");

    var actions = new HorizontalLayout(saveButton, cancelButton);
    actions.setPadding(false);
    editColumn.setEditorComponent(actions);

    editor.addOpenListener(event -> {
      var b = event.getSource().getBinder();
      b.removeBinding(portalNameField);

      if (event.getItem().isNew()) {
        portalNameField.setReadOnly(false);
        b.forField(portalNameField)
            .asRequired("Portal name is required")
            .withValidator(new RegexpValidator("Portal name must be alphanumeric", "^[a-zA-Z0-9]+$"))
            .withValidator(new PortalNameValidator(this.cfpService))
            .bind(PortalWrapper::getPortalName, PortalWrapper::setPortalName);
      } else {
        b.bindReadOnly(portalNameField, PortalWrapper::getPortalName);
        portalNameField.setReadOnly(true);
      }
    });

    return grid;
  }

  private void handleAddNewPortal() {
    var newPortal = PortalWrapper.newPortal();
    this.portals.addFirst(newPortal);
    this.grid.getDataProvider().refreshAll();
    this.grid.getEditor().editItem(newPortal);
  }

  private void handleCancelEdit(Editor<PortalWrapper> editor) {
    var cancelledItem = editor.getItem();
    editor.cancel();
    Optional.ofNullable(cancelledItem)
        .filter(PortalWrapper::isNew)
        .ifPresent(portal -> {
          this.portals.remove(portal);
          this.grid.getDataProvider().refreshAll();
        });
  }

  private void handleSaveEdit(Editor<PortalWrapper> editor) {
    var portal = editor.getItem();
    editor.save();

    if (portal.isNew()) {
      var savedPortal = PortalWrapper.of(this.cfpService.createPortal(portal.getPortal()));
      this.portals.remove(portal);
      this.portals.addFirst(savedPortal);
      this.grid.getDataProvider().refreshAll();
    }
    else {
      this.cfpService.savePortal(portal.getPortal());
      this.grid.getDataProvider().refreshItem(portal);
    }
  }

  private void handleDeletePortal(PortalWrapper portal) {
    this.cfpService.deletePortal(portal.getPortal());
    this.portals.remove(portal);
    this.grid.getDataProvider().refreshAll();
  }

  private void handleExportConfig() {
    new ExportPortalConfigDialog(this.cfpService.getPortals()).open();
  }

  public static class PortalWrapper {
    private final Portal portal;
    private boolean isNew;

    private PortalWrapper() {
      this(Portal.builder().build());
      this.isNew = true;
    }

    private PortalWrapper(Portal portal) {
      this.portal = portal;
      this.isNew = false;
    }

    public Portal getPortal() {
      return this.portal;
    }

    public static PortalWrapper of(Portal portal) {
      return new PortalWrapper(portal);
    }

    public static PortalWrapper newPortal() {
      return new PortalWrapper();
    }

    public String getPortalName() {
      return this.portal.getPortalName();
    }

    public void setPortalName(String portalName) {
      this.portal.setPortalName(portalName);
    }

    public String getBaseUrl() {
      return this.portal.getBaseUrl();
    }

    public void setBaseUrl(String baseUrl) {
      this.portal.setBaseUrl(baseUrl);
    }

    public String getDescription() {
      return this.portal.getDescription();
    }

    public void setDescription(String description) {
      this.portal.setDescription(description);
    }

    public PortalType getPortalType() {
      return this.portal.getPortalType();
    }

    public void setPortalType(PortalType portalType) {
      this.portal.setPortalType(portalType);
    }

    public boolean isNew() {
      return this.isNew;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof PortalWrapper that)) return false;
      return Objects.equals(portal, that.portal);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(portal);
    }

    @Override
    public String toString() {
      return "PortalWrapper{" +
          "portal=" + portal +
          ", isNew=" + isNew +
          '}';
    }
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
          ValidationResult.error(getMessage(value)):
          ValidationResult.ok();
    }
  }
}
