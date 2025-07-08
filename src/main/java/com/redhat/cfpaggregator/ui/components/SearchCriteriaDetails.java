package com.redhat.cfpaggregator.ui.components;

import java.util.Optional;

import com.redhat.cfpaggregator.ui.views.SearchCriteria;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;

public final class SearchCriteriaDetails extends Details {
  public SearchCriteriaDetails(String title, ListDataProvider<SearchCriteria> dataProvider) {
    super(title);

    var grid = createGrid(dataProvider);
    add(new Button("Add", event -> new AddKeywordDialog(title, grid.getListDataView()).open()));
    addThemeVariants(DetailsVariant.FILLED);
    setWidthFull();
    setHeight("min-content");
    setOpened(true);
  }

  private Grid<SearchCriteria> createGrid(ListDataProvider<SearchCriteria> dataProvider) {
    var grid = new Grid<>(SearchCriteria.class, false);
    grid.setAllRowsVisible(false);
    grid.setHeight("200px");
    grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);

    var editor = grid.getEditor();
    var keywordColumn = grid
        .addColumn(SearchCriteria::getKeyword)
        .setAutoWidth(true);

    var editColumn = grid.addComponentColumn(criteria ->
            new Button("Edit", e -> {
              if (editor.isOpen()) {
                editor.cancel();
              }

              editor.editItem(criteria);
            })
        )
        .setWidth("150px")
        .setFlexGrow(0);

    grid.addComponentColumn(criteria ->
            new Button("Delete", e -> {
              if (editor.isOpen()) {
                editor.cancel();
              }

              grid.getListDataView().removeItem(criteria);
            })
        )
        .setWidth("150px")
        .setFlexGrow(0);

    var binder = new Binder<>(SearchCriteria.class);
    editor.setBinder(binder);
    editor.setBuffered(true);

    var validationMessage = new ValidationMessage();
    var keywordField = new TextField();
    keywordField.setWidthFull();
    binder.forField(keywordField)
        .asRequired("%s can't be empty".formatted(getSummaryText()))
        .withStatusLabel(validationMessage)
        .bind(SearchCriteria::getKeyword, SearchCriteria::setKeyword);
    keywordColumn.setEditorComponent(keywordField);

    var saveButton = new Button("Save", e -> editor.save());
    var cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
    cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
    var actions = new HorizontalLayout(saveButton, cancelButton);
    actions.setPadding(false);
    editColumn.setEditorComponent(actions);

    editor.addCancelListener(e -> validationMessage.setText(""));
    grid.setItems(dataProvider);

    add(grid, validationMessage);

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

  private static class ValidationMessage extends HorizontalLayout implements HasText {
    private final Span span = new Span();

    public ValidationMessage() {
      setVisible(false);
      setAlignItems(Alignment.CENTER);
      getStyle().set("color", "var(--lumo-error-text-color)");
      getThemeList().clear();
      getThemeList().add("spacing-s");

      var icon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
      icon.setSize("16px");
      add(icon, span);
    }

    @Override
    public String getText() {
      return span.getText();
    }

    @Override
    public void setText(String text) {
      span.setText(text);
      setVisible(text != null && !text.isEmpty());
    }
  }
}
