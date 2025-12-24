package com.event.views.organizer;

import com.event.model.entities.Event;
import com.event.model.entities.User;
import com.event.model.enums.EventStatus;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.EventService;
import com.event.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "organizer/events", layout = MainLayout.class)
@PageTitle("Mes Événements | EventPro")
public class EventsView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private User currentUser;
    private Grid<Event> grid;
    private TextField searchField;
    private ComboBox<EventStatus> statusFilter;
    private List<Event> allEvents = new java.util.ArrayList<>();
    private boolean initialized = false;

    public EventsView(EventService eventService,
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
        if (initialized) return;

        try {
            this.currentUser = sessionManager.requireAuthentication();
        } catch (RuntimeException ex) {
            navigationManager.navigateToLogin();
            return;
        }

        createHeader();
        createFiltersSection();
        createGridSection();
        loadEvents();
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

        HorizontalLayout headerContent = new HorizontalLayout();
        headerContent.setWidthFull();
        headerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        H1 title = new H1("📅 Mes Événements");
        title.getStyle()
                .set("margin", "0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Gérez tous vos événements");
        subtitle.getStyle()
                .set("margin", "0.5rem 0 0 0")
                .set("opacity", "0.9");

        titleSection.add(title, subtitle);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        Button createButton = new Button("Créer un événement", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        createButton.getStyle()
                .set("background", "white")
                .set("color", "#4ECDC4")
                .set("border", "none");
        createButton.addClickListener(e -> navigationManager.navigateToCreateEvent());

        Button reservationsButton = new Button("Réservations", VaadinIcon.CLIPBOARD_CHECK.create());
        reservationsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        reservationsButton.getStyle()
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("border", "1px solid rgba(255,255,255,0.3)");
        reservationsButton.addClickListener(e -> navigationManager.navigateToOrganizerReservations());

        buttonsLayout.add(createButton, reservationsButton);
        headerContent.add(titleSection, buttonsLayout);
        header.add(headerContent);
        add(header);
    }

    private void createFiltersSection() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setWidthFull();
        filtersSection.setPadding(true);
        filtersSection.getStyle()
                .set("background", "white")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem");

        HorizontalLayout filtersRow = new HorizontalLayout();
        filtersRow.setWidthFull();
        filtersRow.setSpacing(true);
        filtersRow.setAlignItems(FlexComponent.Alignment.END);

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Titre de l'événement...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterEvents());
        searchField.setWidth("300px");

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(Arrays.asList(EventStatus.values()));
        statusFilter.setItemLabelGenerator(EventStatus::getLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> filterEvents());
        statusFilter.setWidth("200px");

        Button refreshButton = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> loadEvents());

        filtersRow.add(searchField, statusFilter, refreshButton);
        filtersSection.add(filtersRow);
        add(filtersSection);
    }

    private void createGridSection() {
        VerticalLayout gridSection = new VerticalLayout();
        gridSection.setSizeFull();
        gridSection.setPadding(true);
        gridSection.getStyle()
                .set("background", "#f7fafc");

        grid = new Grid<>(Event.class, false);
        grid.setSizeFull();
        grid.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Title column
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Category column
        grid.addColumn(e -> e.getCategorie().getLabel())
                .setHeader("Catégorie")
                .setAutoWidth(true);

        // Date column
        grid.addColumn(e -> e.getDateDebut().format(DATE_FORMATTER))
                .setHeader("Date de début")
                .setAutoWidth(true);

        // Location column
        grid.addColumn(e -> e.getVille())
                .setHeader("Ville")
                .setAutoWidth(true);

        // Capacity column
        grid.addColumn(e -> e.getCapaciteMax() + " places")
                .setHeader("Capacité")
                .setWidth("120px")
                .setFlexGrow(0);

        // Price column
        grid.addColumn(e -> String.format("%.0f DH", e.getPrixUnitaire()))
                .setHeader("Prix")
                .setWidth("100px")
                .setFlexGrow(0);

        // Status column with badge
        grid.addColumn(new ComponentRenderer<>(event -> {
            Span badge = new Span(event.getStatut().getLabel());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", event.getStatut().getColor())
                    .set("color", "white")
                    .set("padding", "0.5rem 1rem")
                    .set("border-radius", "12px")
                    .set("font-size", "0.875rem")
                    .set("font-weight", "600");
            return badge;
        })).setHeader("Statut").setWidth("120px").setFlexGrow(0);

        // Actions column
        grid.addColumn(new ComponentRenderer<>(event -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button viewButton = new Button(VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewButton.getElement().setAttribute("title", "Voir détails");
            viewButton.addClickListener(e -> navigationManager.navigateToEventDetails(event.getId()));

            Button reservationsButton = new Button(VaadinIcon.CLIPBOARD_CHECK.create());
            reservationsButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            reservationsButton.getElement().setAttribute("title", "Voir réservations");
            reservationsButton.addClickListener(e -> navigationManager.navigateToOrganizerReservations());

            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Modifier");
            editButton.setEnabled(event.canBeModified());
            editButton.addClickListener(e -> navigationManager.navigateToEditEvent(event.getId()));

            if (event.getStatut() == EventStatus.BROUILLON) {
                Button publishButton = new Button(VaadinIcon.CHECK.create());
                publishButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                publishButton.getElement().setAttribute("title", "Publier");
                publishButton.addClickListener(e -> confirmPublishEvent(event));
                actions.add(publishButton);
            }

            if (event.getStatut() == EventStatus.PUBLIE) {
                Button cancelButton = new Button(VaadinIcon.CLOSE.create());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelButton.getElement().setAttribute("title", "Annuler");
                cancelButton.addClickListener(e -> confirmCancelEvent(event));
                actions.add(cancelButton);
            }

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Supprimer");
            deleteButton.setEnabled(event.canBeDeleted());
            deleteButton.addClickListener(e -> confirmDeleteEvent(event));

            actions.add(viewButton, reservationsButton, editButton, deleteButton);
            return actions;
        })).setHeader("Actions").setWidth("240px").setFlexGrow(0);

        // Set items AFTER columns are added
        grid.setItems(new ArrayList<>());
        
        gridSection.add(grid);
        add(gridSection);
    }

    private void loadEvents() {
        try {
            if (currentUser != null) {
                allEvents = eventService.getEventsByOrganizer(currentUser);
                if (allEvents == null) {
                    allEvents = new java.util.ArrayList<>();
                }
            } else {
                allEvents = new java.util.ArrayList<>();
            }
            filterEvents();
        } catch (Exception e) {
            e.printStackTrace();
            allEvents = new java.util.ArrayList<>();
            grid.setItems(allEvents);
            showNotification("Erreur lors du chargement des événements: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterEvents() {
        if (allEvents == null) {
            allEvents = new java.util.ArrayList<>();
        }
        
        List<Event> filtered = new java.util.ArrayList<>(allEvents);

        // Filter by search
        if (searchField != null) {
            String searchTerm = searchField.getValue();
            if (searchTerm != null && !searchTerm.isEmpty()) {
                filtered = filtered.stream()
                        .filter(e -> e.getTitre().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        // Filter by status
        if (statusFilter != null) {
            EventStatus status = statusFilter.getValue();
            if (status != null) {
                filtered = filtered.stream()
                        .filter(e -> e.getStatut() == status)
                        .collect(Collectors.toList());
            }
        }

        if (grid != null) {
            grid.setItems(filtered);
        }
    }

    private void confirmPublishEvent(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Publier l'événement");
        dialog.setText("Êtes-vous sûr de vouloir publier cet événement ? Il sera visible par tous les utilisateurs.");
        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");
        dialog.setConfirmText("Publier");
        dialog.setConfirmButtonTheme("success primary");

        dialog.addConfirmListener(e -> publishEvent(event));
        dialog.open();
    }

    private void publishEvent(Event event) {
        try {
            eventService.publishEvent(event.getId(), currentUser);
            showNotification("✓ Événement publié avec succès", NotificationVariant.LUMO_SUCCESS);
            loadEvents();
        } catch (Exception e) {
            showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmCancelEvent(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Annuler l'événement");
        dialog.setText("Êtes-vous sûr de vouloir annuler cet événement ? Toutes les réservations seront affectées.");
        dialog.setCancelable(true);
        dialog.setCancelText("Non");
        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> cancelEvent(event));
        dialog.open();
    }

    private void cancelEvent(Event event) {
        try {
            eventService.cancelEvent(event.getId(), currentUser);
            showNotification("✓ Événement annulé", NotificationVariant.LUMO_SUCCESS);
            loadEvents();
        } catch (Exception e) {
            showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteEvent(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Supprimer l'événement");
        dialog.setText("Cette action est irréversible. Êtes-vous sûr de vouloir supprimer cet événement ?");
        dialog.setCancelable(true);
        dialog.setCancelText("Non");
        dialog.setConfirmText("Oui, supprimer");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteEvent(event));
        dialog.open();
    }

    private void deleteEvent(Event event) {
        try {
            eventService.deleteEvent(event.getId(), currentUser);
            showNotification("✓ Événement supprimé avec succès", NotificationVariant.LUMO_SUCCESS);
            loadEvents();
        } catch (Exception e) {
            showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}
