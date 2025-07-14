package com.redhat.cfpaggregator.ui.components;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.qute.Variant;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;

import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.ui.templates.ExportConfigTemplates;

public final class ExportPortalConfigDialog extends Dialog {
  private final Select<ExportFormat> outputSelector = new Select<>();
  private final TextArea textArea = new TextArea("Portals config");
  private final Collection<Portal> portals;

  public ExportPortalConfigDialog(Collection<Portal> portals) {
    super("Exported Portals config");

    this.portals = portals;

    setModal(true);
    setResizable(true);
    setCloseOnEsc(true);
    setDraggable(true);
    setCloseOnOutsideClick(true);

    var closeButton = new Button("Close", event -> close());
    closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    this.textArea.setReadOnly(true);
    this.textArea.setWidthFull();
    this.textArea.setMinRows(5);

    this.outputSelector.setItems(ExportFormat.allSorted());
    this.outputSelector.setLabel("Output format");
    this.outputSelector.setTooltipText("Select the format to export the portals config in");
    this.outputSelector.setWidth("min-content");
    this.outputSelector.addValueChangeListener(event -> handleOutputFormatChange(event.getValue()));
    this.outputSelector.setValue(ExportFormat.PROPERTIES);

    var mainLayout = new VerticalLayout(this.outputSelector, this.textArea);
    mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);

    add(mainLayout);
    getFooter().add(closeButton);
    setWidth("40%");
    setHeight("min-content");
  }

  private void handleOutputFormatChange(ExportFormat exportFormat) {
    var value = ExportConfigTemplates.portalsConfig(this.portals)
        .setVariant(Variant.forContentType(exportFormat.getContentType()))
        .render();

    this.textArea.setValue(value);
  }

  private enum ExportFormat {
    PROPERTIES("Properties", "text/properties"),
    YAML("Yaml", "application/yaml"),
    ENV_VAR("Environment Variables", Variant.TEXT_PLAIN),
    SQL("SQL", "application/sql");

    private final String stringValue;
    private final String contentType;

    ExportFormat(String stringValue, String contentType) {
      this.stringValue = stringValue;
      this.contentType = contentType;
    }

    @Override
    public String toString() {
      return this.stringValue;
    }

    public String getContentType() {
      return this.contentType;
    }

    public static Set<ExportFormat> allSorted() {
      return EnumSet.allOf(ExportFormat.class)
          .stream()
          .sorted(Comparator.comparing(ExportFormat::toString))
          .collect(Collectors.toCollection(LinkedHashSet::new));
    }
  }
}
