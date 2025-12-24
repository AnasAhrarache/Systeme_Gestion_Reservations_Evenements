package com.event.views.admin;

import com.event.model.entities.User;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.EventService;
import com.event.service.ReservationService;
import com.event.service.UserService;
import com.event.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import java.util.Map;

@Route(value = "admin/dashboard", layout = MainLayout.class)
@PageTitle("Tableau de bord Admin | EventPro")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final EventService eventService;
    private final ReservationService reservationService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private User currentUser;
    private boolean initialized = false;

    public DashboardView(UserService userService,
                         EventService eventService,
                         ReservationService reservationService,
                         NavigationManager navigationManager,
                         SessionManager sessionManager) {
        this.userService = userService;
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.navigationManager = navigationManager;
        this.sessionManager = sessionManager;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (initialized) return;

        try {
            this.currentUser = sessionManager.requireAuthentication();
        } catch (RuntimeException ex) {
            navigationManager.navigateToLogin();
            return;
        }

        createHeader();
        createStatisticsSection();
        createQuickActionsSection();
        initialized = true;
    }

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #FF6B6B 0%, #C92A2A 100%)")
                .set("color", "white")
                .set("padding", "2rem");

        H1 welcome = new H1("Bienvenue, " + currentUser.getPrenom() + " ! 👋");
        welcome.getStyle()
                .set("margin", "0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Panneau d'administration - Gérez l'ensemble de la plateforme");
        subtitle.getStyle()
                .set("margin", "0.5rem 0 0 0")
                .set("opacity", "0.9")
                .set("font-size", "1.125rem");

        header.add(welcome, subtitle);
        add(header);
    }

    private void createStatisticsSection() {
        VerticalLayout statsSection = new VerticalLayout();
        statsSection.setWidthFull();
        statsSection.setPadding(true);
        statsSection.getStyle()
                .set("max-width", "1400px")
                .set("margin", "0 auto")
                .set("padding", "2rem");

        H2 sectionTitle = new H2("📊 Statistiques globales");
        sectionTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#2d3748");

        // Get statistics
        Map<String, Long> userStats = userService.getGlobalStatistics();
        Map<String, Object> eventStats = eventService.getGlobalStatistics();
        Map<String, Object> reservationStats = reservationService.getGlobalStatistics();

        // Users statistics
        VerticalLayout usersStatsCard = createStatsCard("👥 Utilisateurs", "#FF6B6B");
        usersStatsCard.add(
                createStatRow("Total", userStats.get("totalUsers").toString()),
                createStatRow("Actifs", userStats.get("activeUsers").toString()),
                createStatRow("Admins", userStats.get("admins").toString()),
                createStatRow("Organisateurs", userStats.get("organizers").toString()),
                createStatRow("Clients", userStats.get("clients").toString())
        );

        // Events statistics
        VerticalLayout eventsStatsCard = createStatsCard("📅 Événements", "#4ECDC4");
        eventsStatsCard.add(
                createStatRow("Total", eventStats.get("totalEvents").toString()),
                createStatRow("Publiés", eventStats.get("publishedEvents").toString()),
                createStatRow("Brouillons", eventStats.get("draftEvents").toString()),
                createStatRow("Annulés", eventStats.get("cancelledEvents").toString()),
                createStatRow("Terminés", eventStats.get("finishedEvents").toString())
        );

        // Reservations statistics
        VerticalLayout reservationsStatsCard = createStatsCard("🎫 Réservations", "#667eea");
        reservationsStatsCard.add(
                createStatRow("Total", reservationStats.get("totalReservations").toString()),
                createStatRow("Actives", reservationStats.get("activeReservations").toString()),
                createStatRow("Confirmées", reservationStats.get("confirmedReservations").toString()),
                createStatRow("En attente", reservationStats.get("pendingReservations").toString()),
                createStatRow("Annulées", reservationStats.get("cancelledReservations").toString()),
                createStatRow("Revenus total", String.format("%.0f DH", (Double) reservationStats.get("totalRevenue")))
        );

        HorizontalLayout statsCards = new HorizontalLayout();
        statsCards.setWidthFull();
        statsCards.setSpacing(true);
        statsCards.add(usersStatsCard, eventsStatsCard, reservationsStatsCard);

        statsSection.add(sectionTitle, statsCards);
        add(statsSection);
    }

    private VerticalLayout createStatsCard(String title, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setPadding(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("border-top", "4px solid " + color);

        H3 cardTitle = new H3(title);
        cardTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#2d3748")
                .set("font-size", "1.25rem");

        card.add(cardTitle);
        return card;
    }

    private HorizontalLayout createStatRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle()
                .set("padding", "0.75rem 0")
                .set("border-bottom", "1px solid #e2e8f0");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("color", "#718096")
                .set("font-weight", "500");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "#2d3748")
                .set("font-weight", "700")
                .set("font-size", "1.125rem");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private void createQuickActionsSection() {
        VerticalLayout actionsSection = new VerticalLayout();
        actionsSection.setWidthFull();
        actionsSection.setPadding(true);
        actionsSection.getStyle()
                .set("max-width", "1400px")
                .set("margin", "0 auto")
                .set("padding", "2rem");

        H2 sectionTitle = new H2("🚀 Actions rapides");
        sectionTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#2d3748");

        HorizontalLayout actionsGrid = new HorizontalLayout();
        actionsGrid.setWidthFull();
        actionsGrid.setSpacing(true);

        actionsGrid.add(
                createActionCard(
                        VaadinIcon.USERS,
                        "Gérer les utilisateurs",
                        "Consultez et gérez tous les utilisateurs",
                        "#FF6B6B",
                        e -> navigationManager.navigateTo("admin/users")
                ),
                createActionCard(
                        VaadinIcon.CALENDAR,
                        "Gérer les événements",
                        "Consultez et gérez tous les événements",
                        "#4ECDC4",
                        e -> navigationManager.navigateTo("admin/events")
                ),
                createActionCard(
                        VaadinIcon.TICKET,
                        "Gérer les réservations",
                        "Consultez et gérez toutes les réservations",
                        "#667eea",
                        e -> navigationManager.navigateTo("admin/reservations")
                )
        );

        actionsSection.add(sectionTitle, actionsGrid);
        add(actionsSection);
    }

    private VerticalLayout createActionCard(VaadinIcon iconType, String title,
                                            String description, String color,
                                            com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> clickListener) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("transition", "all 0.3s ease");

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-4px)")
                    .set("box-shadow", "0 8px 16px rgba(0,0,0,0.15)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");
        });

        Div iconContainer = new Div();
        iconContainer.setWidth("64px");
        iconContainer.setHeight("64px");
        iconContainer.getStyle()
                .set("background", color)
                .set("border-radius", "12px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "1rem");

        Icon icon = iconType.create();
        icon.setSize("32px");
        icon.getStyle().set("color", "white");

        iconContainer.add(icon);

        H3 cardTitle = new H3(title);
        cardTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#2d3748")
                .set("font-size", "1.25rem");

        Paragraph cardDescription = new Paragraph(description);
        cardDescription.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#718096")
                .set("line-height", "1.6");

        Button actionButton = new Button("Accéder", VaadinIcon.ARROW_RIGHT.create());
        actionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionButton.getStyle()
                .set("background", color)
                .set("border", "none");
        actionButton.addClickListener(clickListener);

        card.add(iconContainer, cardTitle, cardDescription, actionButton);
        return card;
    }
}

