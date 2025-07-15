package com.redhat.cfpaggregator.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import com.redhat.cfpaggregator.service.CfpService;
import com.redhat.cfpaggregator.ui.views.ManagePortalsView;
import com.redhat.cfpaggregator.ui.views.SearchEventsView;

@PageTitle("CFP Aggregator")
@Route("")
public class MainLayout extends AppLayout {
  private final CfpService cfpService;

  public MainLayout(CfpService cfpService) {
    this.cfpService = cfpService;
    addToNavbar(new DrawerToggle(), new H1("CFP Aggregator"));

    var scroller = new Scroller(createNav());
    scroller.setClassName(Padding.SMALL);
    addToDrawer(scroller);
    setPrimarySection(Section.DRAWER);
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
