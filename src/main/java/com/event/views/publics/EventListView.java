package com.event.views.publics;

import com.event.dto.EventDTO;
import com.event.model.enums.EventCategory;
import com.event.security.NavigationManager;
import com.event.service.EventService;
import com.event.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Route(value = "events", layout = MainLayout.class)
@PageTitle("Événements | EventPro")
@AnonymousAllowed
public class EventListView extends VerticalLayout {

    private final EventService eventService;
    private final NavigationManager navigationManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // Filter components
    private TextField searchField;
    private ComboBox<EventCategory> categoryFilter;
    private ComboBox<String> cityFilter;
    private DatePicker startDateFilter;
    private DatePicker endDateFilter;
    private NumberField minPriceFilter;
    private NumberField maxPriceFilter;

    // Results container
    private VerticalLayout resultsContainer;
    private Span resultsCount;

    private List<EventDTO> currentEvents;

    public EventListView(EventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        createHeader();
        createFiltersSection();
        createResultsSection();

        // Load all available events initially
        loadEvents();
    }

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "3rem 2rem");

        H1 title = new H1("Découvrez nos événements");
        title.getStyle()
                .set("margin", "0")
                .set("text-align", "center")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Trouvez l'événement parfait pour vous");
        subtitle.getStyle()
                .set("margin", "0.5rem 0 0 0")
                .set("text-align", "center")
                .set("font-size", "1.125rem")
                .set("opacity", "0.9");

        header.add(title, subtitle);
        header.setHorizontalComponentAlignment(Alignment.CENTER, title, subtitle);
        add(header);
    }

    private void createFiltersSection() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setWidthFull();
        filtersSection.setPadding(true);
        filtersSection.getStyle()
                .set("background", "white")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("padding", "2rem");

        H3 filtersTitle = new H3("🔍 Filtrer les événements");
        filtersTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#2d3748");

        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par titre...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> loadEvents());

        // Filters row 1
        HorizontalLayout filtersRow1 = new HorizontalLayout();
        filtersRow1.setWidthFull();
        filtersRow1.setSpacing(true);

        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(Arrays.asList(EventCategory.values()));
        categoryFilter.setItemLabelGenerator(EventCategory::getLabel);
        categoryFilter.setPlaceholder("Toutes les catégories");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> loadEvents());

        cityFilter = new ComboBox<>("Ville");
        List<String> cities = Arrays.asList("Casablanca", "Rabat", "Marrakech", "Tanger", "Fès", "Agadir");
        cityFilter.setItems(cities);
        cityFilter.setPlaceholder("Toutes les villes");
        cityFilter.setClearButtonVisible(true);
        cityFilter.addValueChangeListener(e -> loadEvents());

        filtersRow1.add(categoryFilter, cityFilter);
        filtersRow1.setFlexGrow(1, categoryFilter, cityFilter);

        // Filters row 2
        HorizontalLayout filtersRow2 = new HorizontalLayout();
        filtersRow2.setWidthFull();
        filtersRow2.setSpacing(true);

        startDateFilter = new DatePicker("Date de début");
        startDateFilter.setPlaceholder("Sélectionner une date");
        startDateFilter.setClearButtonVisible(true);
        startDateFilter.addValueChangeListener(e -> loadEvents());

        endDateFilter = new DatePicker("Date de fin");
        endDateFilter.setPlaceholder("Sélectionner une date");
        endDateFilter.setClearButtonVisible(true);
        endDateFilter.addValueChangeListener(e -> loadEvents());

        filtersRow2.add(startDateFilter, endDateFilter);
        filtersRow2.setFlexGrow(1, startDateFilter, endDateFilter);

        // Filters row 3 - Price range
        HorizontalLayout filtersRow3 = new HorizontalLayout();
        filtersRow3.setWidthFull();
        filtersRow3.setSpacing(true);

        minPriceFilter = new NumberField("Prix minimum (DH)");
        minPriceFilter.setPlaceholder("0");
        minPriceFilter.setMin(0);
        minPriceFilter.setClearButtonVisible(true);
        minPriceFilter.addValueChangeListener(e -> loadEvents());

        maxPriceFilter = new NumberField("Prix maximum (DH)");
        maxPriceFilter.setPlaceholder("1000");
        maxPriceFilter.setMin(0);
        maxPriceFilter.setClearButtonVisible(true);
        maxPriceFilter.addValueChangeListener(e -> loadEvents());

        filtersRow3.add(minPriceFilter, maxPriceFilter);
        filtersRow3.setFlexGrow(1, minPriceFilter, maxPriceFilter);

        // Clear filters button
        Button clearFiltersButton = new Button("Réinitialiser les filtres", VaadinIcon.REFRESH.create());
        clearFiltersButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearFiltersButton.addClickListener(e -> clearFilters());

        filtersSection.add(
                filtersTitle,
                searchField,
                filtersRow1,
                filtersRow2,
                filtersRow3,
                clearFiltersButton
        );

        add(filtersSection);
    }

    private void createResultsSection() {
        VerticalLayout resultsSection = new VerticalLayout();
        resultsSection.setWidthFull();
        resultsSection.setPadding(true);
        resultsSection.getStyle()
                .set("background", "#f7fafc")
                .set("min-height", "400px");

        HorizontalLayout resultsHeader = new HorizontalLayout();
        resultsHeader.setWidthFull();
        resultsHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        resultsHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        resultsCount = new Span("0 événement(s) trouvé(s)");
        resultsCount.getStyle()
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("color", "#2d3748");

        resultsHeader.add(resultsCount);

        resultsContainer = new VerticalLayout();
        resultsContainer.setWidthFull();
        resultsContainer.setPadding(false);
        resultsContainer.setSpacing(true);

        resultsSection.add(resultsHeader, resultsContainer);
        add(resultsSection);
    }

    private void loadEvents() {
        // Get filter values
        String keyword = searchField.getValue();
        EventCategory category = categoryFilter.getValue();
        String city = cityFilter.getValue();

        LocalDateTime startDate = null;
        if (startDateFilter.getValue() != null) {
            startDate = LocalDateTime.of(startDateFilter.getValue(), LocalTime.MIN);
        }

        LocalDateTime endDate = null;
        if (endDateFilter.getValue() != null) {
            endDate = LocalDateTime.of(endDateFilter.getValue(), LocalTime.MAX);
        }

        Double minPrice = minPriceFilter.getValue();
        Double maxPrice = maxPriceFilter.getValue();

        // Search with filters using DTOs
        currentEvents = eventService.searchEventsDTO(
                category,
                city,
                minPrice,
                maxPrice,
                startDate,
                endDate,
                null,
                keyword != null && !keyword.isEmpty() ? keyword : null
        );

        // Filter only available events
        currentEvents = currentEvents.stream()
                .filter(EventDTO::isAvailable)
                .toList();

        displayEvents();
    }

    private void displayEvents() {
        resultsContainer.removeAll();

        if (currentEvents.isEmpty()) {
            VerticalLayout emptyState = createEmptyState();
            resultsContainer.add(emptyState);
            resultsCount.setText("0 événement trouvé");
            return;
        }

        resultsCount.setText(currentEvents.size() + " événement(s) trouvé(s)");

        // Display events in a grid layout
        HorizontalLayout currentRow = new HorizontalLayout();
        currentRow.setWidthFull();
        currentRow.setSpacing(true);
        currentRow.getStyle().set("flex-wrap", "wrap");

        for (EventDTO event : currentEvents) {
            currentRow.add(createEventCard(event));
        }

        resultsContainer.add(currentRow);
    }

    private VerticalLayout createEventCard(EventDTO event) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("380px");
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("transition", "all 0.3s ease")
                .set("margin", "0.5rem");

        card.addClickListener(e -> navigationManager.navigateToEventDetails(event.getId()));

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

        // Image
        Div imageContainer = new Div();
        imageContainer.setWidthFull();
        imageContainer.setHeight("220px");
        imageContainer.getStyle()
                .set("background-image", "url('" +
                        (event.getImageUrl() != null ? event.getImageUrl() : "https://images.unsplash.com/photo-1492684223066-81342ee5ff30") + "')")
                .set("background-size", "cover")
                .set("background-position", "center")
                .set("position", "relative");

        // Category badge
        Span categoryBadge = new Span(event.getCategorie().getLabel());
        categoryBadge.getStyle()
                .set("position", "absolute")
                .set("top", "1rem")
                .set("right", "1rem")
                .set("background", event.getCategorie().getColor())
                .set("color", "white")
                .set("padding", "0.5rem 1rem")
                .set("border-radius", "20px")
                .set("font-size", "0.875rem")
                .set("font-weight", "600")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.2)");

        imageContainer.add(categoryBadge);

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("color", "#2d3748")
                .set("font-size", "1.25rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(
                    event.getDescription().length() > 100
                            ? event.getDescription().substring(0, 100) + "..."
                            : event.getDescription()
            );
            description.getStyle()
                    .set("margin", "0.5rem 0")
                    .set("color", "#718096")
                    .set("font-size", "0.875rem")
                    .set("line-height", "1.5");
            content.add(description);
        }

        // Date and time
        HorizontalLayout dateTime = new HorizontalLayout();
        dateTime.setSpacing(true);
        dateTime.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon calendarIcon = VaadinIcon.CALENDAR_CLOCK.create();
        calendarIcon.setSize("18px");
        calendarIcon.getStyle().set("color", "#667eea");

        Span dateText = new Span(
                event.getDateDebut().format(DATE_FORMATTER) + " à " +
                        event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        dateText.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#4a5568")
                .set("font-weight", "500");

        dateTime.add(calendarIcon, dateText);

        // Location
        HorizontalLayout location = new HorizontalLayout();
        location.setSpacing(true);
        location.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon locationIcon = VaadinIcon.MAP_MARKER.create();
        locationIcon.setSize("18px");
        locationIcon.getStyle().set("color", "#667eea");

        Span locationText = new Span(event.getLieu() + ", " + event.getVille());
        locationText.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#4a5568");

        location.add(locationIcon, locationText);

        // Footer with price and availability
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.getStyle()
                .set("margin-top", "1rem")
                .set("padding-top", "1rem")
                .set("border-top", "1px solid #e2e8f0");

        VerticalLayout priceLayout = new VerticalLayout();
        priceLayout.setPadding(false);
        priceLayout.setSpacing(false);

        Span priceLabel = new Span("Prix");
        priceLabel.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#a0aec0")
                .set("text-transform", "uppercase");

        Span price = new Span(String.format("%.0f DH", event.getPrixUnitaire()));
        price.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "#667eea")
                .set("line-height", "1");

        priceLayout.add(priceLabel, price);

        VerticalLayout availabilityLayout = new VerticalLayout();
        availabilityLayout.setPadding(false);
        availabilityLayout.setSpacing(false);
        availabilityLayout.setAlignItems(Alignment.END);

        int available = event.getPlacesDisponibles();
        String availabilityText = available + " places";
        String availabilityColor = available > 20 ? "#48bb78" : (available > 5 ? "#ed8936" : "#f56565");

        Span availability = new Span(availabilityText);
        availability.getStyle()
                .set("font-size", "0.875rem")
                .set("color", availabilityColor)
                .set("font-weight", "600");

        Span availabilityLabel = new Span("disponibles");
        availabilityLabel.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#a0aec0");

        availabilityLayout.add(availability, availabilityLabel);

        footer.add(priceLayout, availabilityLayout);

        content.add(title, dateTime, location, footer);
        card.add(imageContainer, content);

        return card;
    }

    private VerticalLayout createEmptyState() {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setWidthFull();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setJustifyContentMode(JustifyContentMode.CENTER);
        emptyState.getStyle()
                .set("padding", "4rem 2rem")
                .set("text-align", "center");

        Span icon = new Span("🔍");
        icon.getStyle()
                .set("font-size", "4rem")
                .set("display", "block")
                .set("margin-bottom", "1rem");

        H3 title = new H3("Aucun événement trouvé");
        title.getStyle()
                .set("color", "#2d3748")
                .set("margin", "0 0 0.5rem 0");

        Paragraph message = new Paragraph("Essayez de modifier vos filtres pour trouver plus d'événements");
        message.getStyle()
                .set("color", "#718096")
                .set("margin", "0");

        Button clearButton = new Button("Réinitialiser les filtres", VaadinIcon.REFRESH.create());
        clearButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clearButton.getStyle().set("margin-top", "1.5rem");
        clearButton.addClickListener(e -> clearFilters());

        emptyState.add(icon, title, message, clearButton);
        return emptyState;
    }

    private void clearFilters() {
        searchField.clear();
        categoryFilter.clear();
        cityFilter.clear();
        startDateFilter.clear();
        endDateFilter.clear();
        minPriceFilter.clear();
        maxPriceFilter.clear();
        loadEvents();
    }
}