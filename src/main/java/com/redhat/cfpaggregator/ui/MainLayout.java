package com.redhat.cfpaggregator.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import com.redhat.cfpaggregator.ui.views.ManagePortalsView;
import com.redhat.cfpaggregator.ui.views.SearchEventsView;

@PageTitle("CFP Aggregator")
@Route("")
@JsModule("prefers-color-scheme.js")
public class MainLayout extends AppLayout {
  private final Button switchThemeButton = new Button(VaadinIcon.SUN_O.create());
  private boolean isDefaultDarkTheme = false;
  private String currentTheme = Lumo.LIGHT;

  public MainLayout() {
    super();

    // Check the browser preference at set theme accordingly
    UI.getCurrent().getElement().executeJs("return document.documentElement.getAttribute('theme') == 'dark'")
        .then(
            Boolean.class,
            isDark -> {
              this.isDefaultDarkTheme = isDark;
              this.currentTheme = isDark ? Lumo.DARK : Lumo.LIGHT;

              this.switchThemeButton.setIcon(this.isDefaultDarkTheme ? VaadinIcon.SUN_O.create() : VaadinIcon.MOON_O.create());
              this.switchThemeButton.addClickListener(e -> toggleLightDarkTheme());
            }
        );

    var titleLayout = new HorizontalLayout(this.switchThemeButton, new H1("CFP Aggregator"));
    titleLayout.setSpacing(true);

    setDrawerOpened(false);
    addToNavbar(new DrawerToggle(), titleLayout);

    var scroller = new Scroller(createNav());
    scroller.setClassName(Padding.SMALL);
    addToDrawer(scroller);

    UI.getCurrent().access(() -> UI.getCurrent().navigate(SearchEventsView.class));
  }

  private void toggleLightDarkTheme() {
    if (Lumo.DARK.equals(this.currentTheme)) {
      this.currentTheme = Lumo.LIGHT;
      this.switchThemeButton.setIcon(VaadinIcon.MOON_O.create());
    }
    else {
      this.currentTheme = Lumo.DARK;
      this.switchThemeButton.setIcon(VaadinIcon.SUN_O.create());
    }

    UI.getCurrent().getElement().executeJs("document.documentElement.setAttribute('theme', '" + this.currentTheme + "')");
  }

  private SideNav createNav() {
    var nav = new SideNav();

    nav.addItem(
        new SideNavItem("Manage Portals", ManagePortalsView.class, VaadinIcon.COG.create()),
        new SideNavItem("Search Events", SearchEventsView.class, VaadinIcon.SEARCH.create())
    );

    return nav;
  }
}
