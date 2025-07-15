package com.redhat.cfpaggregator.ui.components;

import java.util.Collection;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;

import com.redhat.cfpaggregator.domain.Portal;
import com.redhat.cfpaggregator.service.ExportException;
import com.redhat.cfpaggregator.service.ExportFormat;

public final class ExportPortalConfigDialog extends Dialog {
  private static final String DOWNLOAD_URI_TEMPLATE = "/export/config/portalsConfig%s";
  private final Select<ExportFormat> outputSelector = new Select<>();
  private final TextArea textArea = new TextArea("Portals config");
  private final Collection<Portal> portals;
  private final Anchor downloadConfigAnchor;

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

    this.downloadConfigAnchor = new Anchor("/export/config", "Download config");
    this.downloadConfigAnchor.setDownload(true);

    this.outputSelector.setItems(ExportFormat.allSorted());
    this.outputSelector.setLabel("Output format");
    this.outputSelector.setTooltipText("Select the format to export the portals config in");
    this.outputSelector.setWidth("min-content");
    this.outputSelector.addValueChangeListener(event -> handleOutputFormatChange(event.getValue()));
    this.outputSelector.setValue(ExportFormat.PROPERTIES);

    var header = new VerticalLayout(this.outputSelector, this.downloadConfigAnchor);
    header.setPadding(false);
    var mainLayout = new VerticalLayout(header, this.textArea);
    mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
    mainLayout.setPadding(false);

    add(mainLayout);
    getFooter().add(closeButton);
    setWidth("40%");
    setHeight("min-content");
  }

  private void handleOutputFormatChange(ExportFormat exportFormat) {
    try {
      this.textArea.setValue(exportFormat.render(this.portals));
      this.downloadConfigAnchor.setHref(DOWNLOAD_URI_TEMPLATE.formatted(exportFormat.getFileExtension()));
    }
    catch (ExportException e) {
      this.textArea.setValue(e.getMessage());
    }
  }
}
