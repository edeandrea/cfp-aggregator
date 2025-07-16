package com.redhat.cfpaggregator.ui.components;

import java.util.Optional;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;

import com.redhat.cfpaggregator.ui.views.SearchCriteria;

public final class SearchCriteriaDetails extends Details {
  public SearchCriteriaDetails(String title, ListDataProvider<SearchCriteria> dataProvider) {
    super(title);

    var grid = createGrid(dataProvider);
    add(grid);
    add(new Button("Add", event -> new AddKeywordDialog(title, grid.getListDataView()).open()));
    setWidthFull();
    setHeight("min-content");
    setOpened(true);
  }

  private Grid<SearchCriteria> createGrid(ListDataProvider<SearchCriteria> dataProvider) {
    var grid = new Grid<>(SearchCriteria.class, false);
    grid.setAllRowsVisible(false);
    grid.setHeight("200px");
    grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_ROW_BORDERS);

    grid
        .addColumn(SearchCriteria::getKeyword)
        .setAutoWidth(true);

    grid.addComponentColumn(criteria ->
            new Button(VaadinIcon.TRASH.create(), e -> {
              grid.getListDataView().removeItem(criteria);
            })
        )
        .setWidth("150px")
        .setFlexGrow(0);

    grid.setItems(dataProvider);

    return grid;
  }

  private static class AddKeywordDialog extends Dialog {
    private final GridListDataView<SearchCriteria> gridListDataView;

    public AddKeywordDialog(String title, GridListDataView<SearchCriteria> gridListDataView) {
      super("Add %s keyword".formatted(title));

      this.gridListDataView = gridListDataView;

      setModal(true);
      setResizable(false);
      setCloseOnEsc(true);
      setCloseOnOutsideClick(false);

      var textField = new TextField("", "", "Search term");
      var cancelButton = new Button("Cancel", event -> handleCancelButtonClick());
      var okButton = new Button("Ok", event -> handleOkButtonClick(textField.getValue()));
      okButton.addClickShortcut(Key.ENTER);
      okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

      var buttonLayout = new HorizontalLayout(okButton, cancelButton);
      buttonLayout.getStyle().set("flex-wrap", "wrap");
      buttonLayout.setJustifyContentMode(JustifyContentMode.END);

      add(textField, buttonLayout);
      textField.focus();
    }

    private void handleOkButtonClick(String keyword) {
      Optional.ofNullable(keyword)
          .map(String::strip)
          .filter(s -> !s.isEmpty())
          .map(SearchCriteria::new)
          .ifPresent(this.gridListDataView::addItem);

      close();
    }

    private void handleCancelButtonClick() {
      close();
    }
  }
}
