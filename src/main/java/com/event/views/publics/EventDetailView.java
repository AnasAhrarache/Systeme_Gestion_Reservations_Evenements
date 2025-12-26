package com.event.views.publics;

import com.event.dto.EventDTO;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.EventService;
import com.event.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;

@Route(value = "event/:eventId", layout = MainLayout.class)
@PageTitle("Détails de l'événement | EventPro")
@AnonymousAllowed
public class EventDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private EventDTO event;
    private Long eventId;

    public EventDetailView(EventService eventService,
                           NavigationManager navigationManager,
                           SessionManager sessionManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;
        this.sessionManager = sessionManager;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters parameters = event.getRouteParameters();
        String eventIdParam = parameters.get("eventId").orElse(null);
        
        if (eventIdParam == null || eventIdParam.isEmpty()) {
            navigationManager.navigateToEvents();
            return;
        }

        try {
            this.eventId = Long.parseLong(eventIdParam);
            loadEventDetails();
        } catch (NumberFormatException e) {
            add(createErrorView());
        }
    }

    private void loadEventDetails() {
        try {
            event = eventService.getEventDTO(eventId);
            createEventDetail();
        } catch (Exception e) {
            add(createErrorView());
        }
    }

    private void createEventDetail() {
        // Header with image
        createHeaderSection();

        // Main content
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setWidthFull();
        mainContent.setSpacing(true);
        mainContent.setPadding(true);
        mainContent.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("padding", "2rem");

        // Left column - Event details
        VerticalLayout leftColumn = createLeftColumn();
        leftColumn.setWidth("70%");

        // Right column - Booking card
        VerticalLayout rightColumn = createRightColumn();
        rightColumn.setWidth("30%");

        mainContent.add(leftColumn, rightColumn);
        add(mainContent);
    }

    private void createHeaderSection() {
        Div headerContainer = new Div();
        headerContainer.setWidthFull();
        headerContainer.setHeight("500px");
        headerContainer.getStyle()
                .set("background-image", "url('" +
                        (event.getImageUrl() != null ? event.getImageUrl() : "https://images.unsplash.com/photo-1492684223066-81342ee5ff30") + "')")
                .set("background-size", "cover")
                .set("background-position", "center")
                .set("position", "relative")
                .set("overflow", "hidden");

        // Animated background pattern
        Div pattern = new Div();
        pattern.setWidthFull();
        pattern.setHeight("100%");
        pattern.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("background", "radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%)")
                .set("pointer-events", "none");

        // Overlay with better gradient
        Div overlay = new Div();
        overlay.setWidthFull();
        overlay.setHeight("100%");
        overlay.getStyle()
                .set("background", "linear-gradient(135deg, rgba(0,0,0,0.2) 0%, rgba(102, 126, 234, 0.3) 50%, rgba(0,0,0,0.7) 100%)")
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "flex-start")
                .set("justify-content", "flex-end")
                .set("padding", "3rem 2rem")
                .set("z-index", "2");

        VerticalLayout headerContent = new VerticalLayout();
        headerContent.setSpacing(false);
        headerContent.getStyle()
                .set("color", "white")
                .set("width", "100%")
                .set("max-width", "1200px")
                .set("margin", "0 auto");

        // Category badge with animation
        Span categoryBadge = new Span(event.getCategorie().getLabel());
        categoryBadge.getStyle()
                .set("background", event.getCategorie().getColor())
                .set("color", "white")
                .set("padding", "0.75rem 1.5rem")
                .set("border-radius", "50px")
                .set("font-size", "0.95rem")
                .set("font-weight", "700")
                .set("display", "inline-block")
                .set("margin-bottom", "1.5rem")
                .set("box-shadow", "0 4px 15px rgba(0, 0, 0, 0.3)")
                .set("animation", "slideInLeft 0.5s ease-out");

        H1 title = new H1(event.getTitre());
        title.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "white")
                .set("font-size", "3.5rem")
                .set("font-weight", "900")
                .set("text-shadow", "0 4px 20px rgba(0, 0, 0, 0.4)")
                .set("line-height", "1.2")
                .set("animation", "slideInLeft 0.6s ease-out 0.1s backwards");

        HorizontalLayout eventMeta = new HorizontalLayout();
        eventMeta.setSpacing(true);
        eventMeta.setAlignItems(FlexComponent.Alignment.CENTER);
        eventMeta.getStyle()
                .set("animation", "slideInLeft 0.6s ease-out 0.2s backwards");

        Span organizer = new Span("👤 Par " + event.getOrganisateurNom());
        organizer.getStyle()
                .set("font-size", "1.1rem")
                .set("opacity", "0.95")
                .set("font-weight", "600");

        eventMeta.add(organizer);

        headerContent.add(categoryBadge, title, eventMeta);
        overlay.add(pattern, headerContent);
        headerContainer.add(overlay);

        add(headerContainer);
    }

    private VerticalLayout createLeftColumn() {
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(true);
        leftColumn.setSpacing(true);

        // Date and time section
        VerticalLayout dateTimeSection = createInfoSection(
                VaadinIcon.CALENDAR_CLOCK,
                "Date et heure",
                event.getDateDebut().format(DATE_FORMATTER) + " à " +
                        event.getDateDebut().format(TIME_FORMATTER)
        );

        // Location section
        VerticalLayout locationSection = createInfoSection(
                VaadinIcon.MAP_MARKER,
                "Lieu",
                event.getLieu() + ", " + event.getVille()
        );

        // Description section
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            VerticalLayout descriptionSection = new VerticalLayout();
            descriptionSection.setPadding(true);
            descriptionSection.getStyle()
                    .set("background", "white")
                    .set("border-radius", "12px")
                    .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

            H3 descTitle = new H3("📝 Description");
            descTitle.getStyle()
                    .set("margin", "0 0 1rem 0")
                    .set("color", "#2d3748");

            Paragraph description = new Paragraph(event.getDescription());
            description.getStyle()
                    .set("color", "#4a5568")
                    .set("line-height", "1.8")
                    .set("margin", "0");

            descriptionSection.add(descTitle, description);
            leftColumn.add(descriptionSection);
        }

        // Event details
        VerticalLayout detailsSection = createDetailsSection();

        leftColumn.add(dateTimeSection, locationSection, detailsSection);
        return leftColumn;
    }

    private VerticalLayout createInfoSection(VaadinIcon iconType, String title, String value) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(false);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);

        Icon icon = iconType.create();
        icon.setSize("24px");
        icon.getStyle().set("color", "#667eea");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#718096")
                .set("text-transform", "uppercase")
                .set("font-weight", "600");

        header.add(icon, titleSpan);

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "1.25rem")
                .set("color", "#2d3748")
                .set("font-weight", "600")
                .set("margin-top", "0.5rem");

        section.add(header, valueSpan);
        return section;
    }

    private VerticalLayout createDetailsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H3 title = new H3("ℹ️ Informations détaillées");
        title.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#2d3748");

        section.add(title);
        section.add(createDetailRow("Catégorie", event.getCategorie().getLabel()));
        section.add(createDetailRow("Capacité totale", event.getCapaciteMax() + " personnes"));
        section.add(createDetailRow("Places réservées", event.getPlacesReservees() + " places"));
        section.add(createDetailRow("Places disponibles", event.getPlacesDisponibles() + " places"));
        section.add(createDetailRow("Taux de remplissage", String.format("%.1f%%", event.getTauxRemplissage())));
        section.add(createDetailRow("Date de fin", event.getDateFin().format(DATE_FORMATTER) + " à " + event.getDateFin().format(TIME_FORMATTER)));

        return section;
    }

    private HorizontalLayout createDetailRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);
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
                .set("font-weight", "600");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private VerticalLayout createRightColumn() {
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(true);
        rightColumn.setSpacing(true);
        rightColumn.getStyle()
                .set("position", "sticky")
                .set("top", "2rem");

        // Booking card
        VerticalLayout bookingCard = new VerticalLayout();
        bookingCard.setPadding(true);
        bookingCard.setSpacing(true);
        bookingCard.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
                .set("border", "2px solid #667eea");

        // Price
        VerticalLayout priceSection = new VerticalLayout();
        priceSection.setPadding(false);
        priceSection.setSpacing(false);
        priceSection.setAlignItems(Alignment.CENTER);

        Span priceLabel = new Span("Prix par place");
        priceLabel.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#718096")
                .set("text-transform", "uppercase");

        Span price = new Span(String.format("%.0f DH", event.getPrixUnitaire()));
        price.getStyle()
                .set("font-size", "3rem")
                .set("font-weight", "800")
                .set("color", "#667eea")
                .set("line-height", "1");

        priceSection.add(priceLabel, price);

        // Availability indicator
        HorizontalLayout availability = new HorizontalLayout();
        availability.setWidthFull();
        availability.setJustifyContentMode(JustifyContentMode.CENTER);
        availability.setAlignItems(FlexComponent.Alignment.CENTER);
        availability.setSpacing(true);
        availability.getStyle()
                .set("background", event.getPlacesDisponibles() > 10 ? "#f0fdf4" : "#fef2f2")
                .set("padding", "1rem")
                .set("border-radius", "8px")
                .set("margin", "1rem 0");

        Icon availIcon = event.getPlacesDisponibles() > 10
                ? VaadinIcon.CHECK_CIRCLE.create()
                : VaadinIcon.WARNING.create();
        availIcon.setSize("20px");
        availIcon.getStyle().set("color", event.getPlacesDisponibles() > 10 ? "#22c55e" : "#ef4444");

        Span availText = new Span(event.getPlacesDisponibles() + " places disponibles");
        availText.getStyle()
                .set("font-weight", "600")
                .set("color", event.getPlacesDisponibles() > 10 ? "#166534" : "#991b1b");

        availability.add(availIcon, availText);

        // Reserve button
        Button reserveButton = new Button("Réserver maintenant", VaadinIcon.TICKET.create());
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveButton.setWidthFull();
        reserveButton.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none")
                .set("font-weight", "600")
                .set("padding", "1rem");

        if (!event.isAvailable()) {
            reserveButton.setEnabled(false);
            reserveButton.setText("Indisponible");
        } else {
            reserveButton.addClickListener(e -> handleReservation());
        }

        // Back button
        Button backButton = new Button("Retour aux événements", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.setWidthFull();
        backButton.addClickListener(e -> navigationManager.navigateToEvents());

        bookingCard.add(priceSection, availability, reserveButton, backButton);
        rightColumn.add(bookingCard);

        return rightColumn;
    }

    private void handleReservation() {
        if (sessionManager.isUserLoggedIn()) {
            navigationManager.navigateToReservation(eventId);
        } else {
            navigationManager.navigateToLogin();
        }
    }

    private VerticalLayout createErrorView() {
        VerticalLayout errorView = new VerticalLayout();
        errorView.setSizeFull();
        errorView.setAlignItems(Alignment.CENTER);
        errorView.setJustifyContentMode(JustifyContentMode.CENTER);

        Span errorIcon = new Span("❌");
        errorIcon.getStyle()
                .set("font-size", "4rem")
                .set("display", "block")
                .set("margin-bottom", "1rem");

        H2 errorTitle = new H2("Événement introuvable");
        errorTitle.getStyle()
                .set("color", "#2d3748")
                .set("margin", "0 0 0.5rem 0");

        Paragraph errorMessage = new Paragraph("L'événement que vous recherchez n'existe pas ou a été supprimé.");
        errorMessage.getStyle()
                .set("color", "#718096")
                .set("margin", "0 0 2rem 0");

        Button backButton = new Button("Retour aux événements", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backButton.addClickListener(e -> navigationManager.navigateToEvents());

        errorView.add(errorIcon, errorTitle, errorMessage, backButton);
        return errorView;
    }
}