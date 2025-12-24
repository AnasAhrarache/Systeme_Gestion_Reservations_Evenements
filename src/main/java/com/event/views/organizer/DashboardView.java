package com.event.views.organizer;

import com.event.model.entities.User;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.EventService;
import com.event.service.ReservationService;
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

@Route(value = "organizer/dashboard", layout = MainLayout.class)
@PageTitle("Tableau de bord Organisateur | EventPro")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private User currentUser;
    private boolean initialized = false;

    public DashboardView(EventService eventService,
            ReservationService reservationService,
            NavigationManager navigationManager,
            SessionManager sessionManager) {
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
        createRecentEventsSection();
        initialized = true;
    }

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #4ECDC4 0%, #44A08D 100%)")
                .set("color", "white")
                .set("padding", "2rem");

        H1 welcome = new H1("Bienvenue, " + currentUser.getPrenom() + " ! 👋");
        welcome.getStyle()
                .set("margin", "0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Gérez vos événements et suivez vos statistiques");
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
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("padding", "2rem");

        H2 sectionTitle = new H2("📊 Mes statistiques");
        sectionTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#2d3748");

        // Get statistics
        Map<String, Object> stats = eventService.getOrganizerStatistics(currentUser);

        HorizontalLayout statsCards = new HorizontalLayout();
        statsCards.setWidthFull();
        statsCards.setSpacing(true);

        statsCards.add(
                createStatCard(
                        VaadinIcon.CALENDAR,
                        "Événements total",
                        stats.get("totalEvents").toString(),
                        "#4ECDC4"),
                createStatCard(
                        VaadinIcon.CHECK_CIRCLE,
                        "Publiés",
                        stats.get("publishedEvents").toString(),
                        "#48bb78"),
                createStatCard(
                        VaadinIcon.EDIT,
                        "Brouillons",
                        stats.get("draftEvents").toString(),
                        "#ed8936"),
                createStatCard(
                        VaadinIcon.TICKET,
                        "Réservations",
                        stats.get("totalReservations").toString(),
                        "#667eea"),
                createStatCard(
                        VaadinIcon.MONEY,
                        "Revenus total",
                        String.format("%.0f DH", (Double) stats.get("totalRevenue")),
                        "#764ba2"));

        statsSection.add(sectionTitle, statsCards);
        add(statsSection);
    }

    private VerticalLayout createStatCard(VaadinIcon iconType, String title, String value, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("border-left", "4px solid " + color);

        Icon icon = iconType.create();
        icon.setSize("32px");
        icon.getStyle()
                .set("color", color)
                .set("margin-bottom", "1rem");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#718096")
                .set("text-transform", "uppercase")
                .set("display", "block")
                .set("margin-bottom", "0.5rem");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "2rem")
                .set("font-weight", "800")
                .set("color", "#2d3748")
                .set("line-height", "1");

        card.add(icon, titleSpan, valueSpan);
        return card;
    }

    private void createQuickActionsSection() {
        VerticalLayout actionsSection = new VerticalLayout();
        actionsSection.setWidthFull();
        actionsSection.setPadding(true);
        actionsSection.getStyle()
                .set("max-width", "1200px")
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
                        VaadinIcon.PLUS,
                        "Créer un événement",
                        "Créez un nouvel événement",
                        "#4ECDC4",
                        e -> navigationManager.navigateToCreateEvent()),
                createActionCard(
                        VaadinIcon.CALENDAR,
                        "Mes événements",
                        "Gérez tous vos événements",
                        "#48bb78",
                        e -> navigationManager.navigateToMyEvents()),
                createActionCard(
                        VaadinIcon.TICKET,
                        "Mes réservations",
                        "Consultez les réservations",
                        "#667eea",
                        e -> navigationManager.navigateToMyReservations()));

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

    private void createRecentEventsSection() {
        VerticalLayout recentSection = new VerticalLayout();
        recentSection.setWidthFull();
        recentSection.setPadding(true);
        recentSection.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("padding", "2rem");

        HorizontalLayout sectionHeader = new HorizontalLayout();
        sectionHeader.setWidthFull();
        sectionHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        sectionHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 sectionTitle = new H2("📅 Mes événements récents");
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("color", "#2d3748");

        Button viewAllButton = new Button("Voir tout", VaadinIcon.ARROW_RIGHT.create());
        viewAllButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllButton.addClickListener(e -> navigationManager.navigateToMyEvents());

        sectionHeader.add(sectionTitle, viewAllButton);

        // Get recent events
        var events = eventService.getEventsByOrganizer(currentUser);
        var recentEvents = events.stream()
                .sorted((e1, e2) -> e2.getDateCreation().compareTo(e1.getDateCreation()))
                .limit(3)
                .toList();

        VerticalLayout eventsList = new VerticalLayout();
        eventsList.setWidthFull();
        eventsList.setSpacing(true);

        if (recentEvents.isEmpty()) {
            Paragraph emptyMessage = new Paragraph("Aucun événement créé pour le moment");
            emptyMessage.getStyle()
                    .set("color", "#718096")
                    .set("text-align", "center")
                    .set("padding", "2rem");
            eventsList.add(emptyMessage);
        } else {
            recentEvents.forEach(event -> {
                HorizontalLayout eventRow = new HorizontalLayout();
                eventRow.setWidthFull();
                eventRow.setPadding(true);
                eventRow.setSpacing(true);
                eventRow.getStyle()
                        .set("background", "white")
                        .set("border-radius", "8px")
                        .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

                Span eventTitle = new Span(event.getTitre());
                eventTitle.getStyle()
                        .set("font-weight", "600")
                        .set("color", "#2d3748")
                        .set("flex-grow", "1");

                Span statusBadge = new Span(event.getStatut().getLabel());
                statusBadge.getElement().getThemeList().add("badge");
                statusBadge.getStyle()
                        .set("background", event.getStatut().getColor())
                        .set("color", "white")
                        .set("padding", "0.25rem 0.75rem")
                        .set("border-radius", "12px")
                        .set("font-size", "0.75rem")
                        .set("font-weight", "600");

                Button viewButton = new Button("Voir", VaadinIcon.EYE.create());
                viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                viewButton.addClickListener(e -> navigationManager.navigateToEventDetails(event.getId()));

                eventRow.add(eventTitle, statusBadge, viewButton);
                eventRow.setFlexGrow(1, eventTitle);
                eventsList.add(eventRow);
            });
        }

        recentSection.add(sectionHeader, eventsList);
        add(recentSection);
    }
}
