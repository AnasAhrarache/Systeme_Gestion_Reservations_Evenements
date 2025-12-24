package com.event.views;

import com.event.model.entities.User;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Optional;

public class MainLayout extends AppLayout {

    private final SessionManager sessionManager;
    private final NavigationManager navigationManager;

    public MainLayout(SessionManager sessionManager, NavigationManager navigationManager) {
        this.sessionManager = sessionManager;
        this.navigationManager = navigationManager;

        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("🎫 EventPro");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );
        logo.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        Optional<User> userOpt = sessionManager.getCurrentUser();

        HorizontalLayout header;

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            Avatar avatar = new Avatar(user.getFullName());
            avatar.setColorIndex(user.getId().intValue());

            Span userName = new Span(user.getFullName());
            userName.getStyle().set("font-weight", "500");

            Span userRole = new Span(user.getRole().getLabel());
            userRole.getStyle()
                    .set("font-size", "var(--lumo-font-size-xs)")
                    .set("color", "var(--lumo-secondary-text-color)");

            VerticalLayout userInfo = new VerticalLayout(userName, userRole);
            userInfo.setSpacing(false);
            userInfo.setPadding(false);

            Button logoutButton = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            logoutButton.addClickListener(e -> {
                sessionManager.logout();
                navigationManager.navigateToLogin();
            });

            HorizontalLayout userLayout = new HorizontalLayout(avatar, userInfo, logoutButton);
            userLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            userLayout.setSpacing(true);

            header = new HorizontalLayout(new DrawerToggle(), logo, userLayout);
        } else {
            Button loginButton = new Button("Connexion", VaadinIcon.SIGN_IN.create());
            loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            loginButton.addClickListener(e -> navigationManager.navigateToLogin());

            Button registerButton = new Button("S'inscrire");
            registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            registerButton.addClickListener(e -> navigationManager.navigateToRegister());

            HorizontalLayout authButtons = new HorizontalLayout(loginButton, registerButton);
            authButtons.setSpacing(true);

            header = new HorizontalLayout(new DrawerToggle(), logo, authButtons);
        }

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM
        );
        header.getStyle()
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("background", "var(--lumo-base-color)");

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        Optional<User> userOpt = sessionManager.getCurrentUser();

        if (userOpt.isEmpty()) {
            // Public navigation
            nav.addItem(new SideNavItem("Accueil", "", VaadinIcon.HOME.create()));
            nav.addItem(new SideNavItem("Événements", "events", VaadinIcon.CALENDAR.create()));
        } else {
            User user = userOpt.get();

            switch (user.getRole()) {
                case CLIENT:
                    createClientNavigation(nav);
                    break;
                case ORGANIZER:
                    createOrganizerNavigation(nav);
                    break;
                case ADMIN:
                    createAdminNavigation(nav);
                    break;
            }
        }

        VerticalLayout drawerLayout = new VerticalLayout(nav);
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.getStyle()
                .set("background", "var(--lumo-contrast-5pct)");

        addToDrawer(drawerLayout);
    }

    private void createClientNavigation(SideNav nav) {
        nav.addItem(new SideNavItem("Tableau de bord", "dashboard", VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Événements", "events", VaadinIcon.CALENDAR.create()));
        nav.addItem(new SideNavItem("Mes réservations", "my-reservations", VaadinIcon.TICKET.create()));
        nav.addItem(new SideNavItem("Mon profil", "profile", VaadinIcon.USER.create()));
    }

    private void createOrganizerNavigation(SideNav nav) {
        nav.addItem(new SideNavItem("Tableau de bord", "organizer/dashboard", VaadinIcon.DASHBOARD.create()));

        SideNavItem eventsItem = new SideNavItem("Mes événements", "organizer/events", VaadinIcon.CALENDAR.create());
        eventsItem.addItem(new SideNavItem("Créer un événement", "organizer/event/new", VaadinIcon.PLUS.create()));
        nav.addItem(eventsItem);

        nav.addItem(new SideNavItem("Tous les événements", "events", VaadinIcon.GLOBE.create()));
        nav.addItem(new SideNavItem("Mes réservations", "my-reservations", VaadinIcon.TICKET.create()));
        nav.addItem(new SideNavItem("Mon profil", "profile", VaadinIcon.USER.create()));
    }

    private void createAdminNavigation(SideNav nav) {
        nav.addItem(new SideNavItem("Tableau de bord Admin", "admin/dashboard", VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Utilisateurs", "admin/users", VaadinIcon.USERS.create()));
        nav.addItem(new SideNavItem("Événements", "admin/events", VaadinIcon.CALENDAR.create()));
        nav.addItem(new SideNavItem("Réservations", "admin/reservations", VaadinIcon.TICKET.create()));
        nav.addItem(new SideNavItem("Mon profil", "profile", VaadinIcon.USER.create()));
    }
}