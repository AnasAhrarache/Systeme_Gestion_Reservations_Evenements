package com.event.views.organizer;

import com.event.model.entities.Event;
import com.event.model.entities.Reservation;
import com.event.model.entities.User;
import com.event.model.enums.ReservationStatus;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.EventService;
import com.event.service.ReservationService;
import com.event.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "organizer/reservations", layout = MainLayout.class)
@PageTitle("Réservations | EventPro")
public class EventReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private User currentUser;
    private Grid<Reservation> reservationGrid;
    private TextField searchField;
    private com.vaadin.flow.component.combobox.ComboBox<Event> eventFilter;
    private List<Event> organizerEvents = new ArrayList<>();
    private List<Reservation> allReservations = new ArrayList<>();
    private boolean initialized = false;

    public EventReservationsView(EventService eventService,
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
        createFiltersSection();
        createGridSection();
        loadReservations();
        initialized = true;
    }

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "2rem");

        HorizontalLayout headerContent = new HorizontalLayout();
        headerContent.setWidthFull();
        headerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        H1 title = new H1("📋 Réservations de vos événements");
        title.getStyle()
                .set("margin", "0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Gérez les réservations des clients pour vos événements");
        subtitle.getStyle()
                .set("margin", "0.5rem 0 0 0")
                .set("opacity", "0.9");

        titleSection.add(title, subtitle);

        Button backButton = new Button("← Retour aux événements", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle()
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("border", "1px solid rgba(255,255,255,0.3)");
        backButton.addClickListener(e -> navigationManager.navigateToMyEvents());

        headerContent.add(titleSection, backButton);
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
        searchField.setPlaceholder("Client ou code réservation...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterReservations());
        searchField.setWidth("300px");

        eventFilter = new com.vaadin.flow.component.combobox.ComboBox<>("Événement");
        eventFilter.setItemLabelGenerator(Event::getTitre);
        eventFilter.setPlaceholder("Tous les événements");
        eventFilter.setClearButtonVisible(true);
        eventFilter.addValueChangeListener(e -> filterReservations());
        eventFilter.setWidth("300px");

        Button refreshButton = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> loadReservations());

        filtersRow.add(searchField, eventFilter, refreshButton);
        filtersSection.add(filtersRow);
        add(filtersSection);
    }

    private void createGridSection() {
        VerticalLayout gridSection = new VerticalLayout();
        gridSection.setSizeFull();
        gridSection.setPadding(true);
        gridSection.getStyle()
                .set("background", "#f7fafc");

        reservationGrid = new Grid<>(Reservation.class, false);
        reservationGrid.setSizeFull();
        reservationGrid.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Event Title
        reservationGrid.addColumn(r -> r.getEvenement().getTitre())
                .setHeader("Événement")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Code de réservation
        reservationGrid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setAutoWidth(true)
                .setWidth("100px")
                .setFlexGrow(0);

        // Client name
        reservationGrid.addColumn(r -> r.getUtilisateur().getFullName())
                .setHeader("Client")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Client email
        reservationGrid.addColumn(r -> r.getUtilisateur().getEmail())
                .setHeader("Email")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Number of places
        reservationGrid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setWidth("80px")
                .setFlexGrow(0);

        // Total amount
        reservationGrid.addColumn(r -> String.format("%.0f DH", r.getMontantTotal()))
                .setHeader("Montant")
                .setWidth("100px")
                .setFlexGrow(0);

        // Reservation date
        reservationGrid.addColumn(r -> r.getDateReservation().format(DATE_FORMATTER))
                .setHeader("Date réservation")
                .setWidth("150px")
                .setFlexGrow(0);

        // Status badge
        reservationGrid.addColumn(new ComponentRenderer<>(reservation -> {
            Span badge = new Span(reservation.getStatut().getLabel());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", getStatusColor(reservation.getStatut()))
                    .set("color", "white")
                    .set("padding", "0.5rem 1rem")
                    .set("border-radius", "12px")
                    .set("font-size", "0.875rem")
                    .set("font-weight", "600");
            return badge;
        })).setHeader("Statut").setWidth("120px").setFlexGrow(0);

        // Actions
        reservationGrid.addColumn(new ComponentRenderer<>(reservation -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button viewButton = new Button(VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewButton.getElement().setAttribute("title", "Voir détails");
            viewButton.addClickListener(e -> showReservationDetails(reservation));

            if (reservation.getStatut() == ReservationStatus.EN_ATTENTE) {
                Button confirmButton = new Button(VaadinIcon.CHECK.create());
                confirmButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                confirmButton.getElement().setAttribute("title", "Confirmer");
                confirmButton.addClickListener(e -> confirmReservation(reservation));
                actions.add(confirmButton);

                Button rejectButton = new Button(VaadinIcon.CLOSE.create());
                rejectButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                rejectButton.getElement().setAttribute("title", "Rejeter");
                rejectButton.addClickListener(e -> rejectReservation(reservation));
                actions.add(rejectButton);
            }

            actions.add(viewButton);
            return actions;
        })).setHeader("Actions").setWidth("180px").setFlexGrow(0);

        // Set items AFTER columns are added
        reservationGrid.setItems(new ArrayList<>());
        
        gridSection.add(reservationGrid);
        add(gridSection);
    }

    private void loadReservations() {
        try {
            organizerEvents = eventService.getEventsByOrganizer(currentUser);
            if (organizerEvents == null) {
                organizerEvents = new ArrayList<>();
            }
            eventFilter.setItems(organizerEvents);

            allReservations = new ArrayList<>();
            for (Event event : organizerEvents) {
                try {
                    List<Reservation> eventReservations = reservationService.getReservationsByEvent(event.getId());
                    if (eventReservations != null) {
                        allReservations.addAll(eventReservations);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading reservations for event " + event.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            filterReservations();
        } catch (Exception e) {
            e.printStackTrace();
            allReservations = new ArrayList<>();
            if (reservationGrid != null) {
                reservationGrid.setItems(allReservations);
            }
            showNotification("Erreur lors du chargement des réservations: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterReservations() {
        if (allReservations == null) {
            allReservations = new ArrayList<>();
        }
        
        List<Reservation> filtered = new ArrayList<>(allReservations);

        // Filter by event
        if (eventFilter != null) {
            Event selectedEvent = eventFilter.getValue();
            if (selectedEvent != null) {
                filtered = filtered.stream()
                        .filter(r -> r.getEvenement().getId().equals(selectedEvent.getId()))
                        .collect(Collectors.toList());
            }
        }

        // Filter by search
        if (searchField != null) {
            String searchTerm = searchField.getValue();
            if (searchTerm != null && !searchTerm.isEmpty()) {
                filtered = filtered.stream()
                        .filter(r -> r.getUtilisateur().getFullName().toLowerCase().contains(searchTerm.toLowerCase())
                                || r.getCodeReservation().toLowerCase().contains(searchTerm.toLowerCase())
                                || r.getUtilisateur().getEmail().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (reservationGrid != null) {
            reservationGrid.setItems(filtered);
        }
    }

    private void showReservationDetails(Reservation reservation) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Détails de la réservation");
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);

        // Event details
        VerticalLayout eventSection = new VerticalLayout();
        eventSection.setPadding(true);
        eventSection.getStyle().set("background", "#f0f0f0").set("border-radius", "8px");

        H3 eventTitle = new H3("📅 " + reservation.getEvenement().getTitre());
        eventTitle.getStyle().set("margin", "0 0 1rem 0");

        Div eventDetails = new Div();
        eventDetails.setText("Date: " + reservation.getEvenement().getDateDebut().format(DATE_FORMATTER)
                + "\nLieu: " + reservation.getEvenement().getLieu() + ", " + reservation.getEvenement().getVille()
                + "\nPrix unitaire: " + String.format("%.0f DH", reservation.getEvenement().getPrixUnitaire()));

        eventSection.add(eventTitle, eventDetails);

        // Reservation details
        VerticalLayout reservationSection = new VerticalLayout();
        reservationSection.setPadding(true);
        reservationSection.getStyle().set("background", "#f0f0f0").set("border-radius", "8px");

        H3 reservationTitle = new H3("🎫 Réservation #" + reservation.getCodeReservation());
        reservationTitle.getStyle().set("margin", "0 0 1rem 0");

        Div reservationDetails = new Div();
        reservationDetails.setText("Client: " + reservation.getUtilisateur().getFullName()
                + "\nEmail: " + reservation.getUtilisateur().getEmail()
                + "\nTéléphone: " + (reservation.getUtilisateur().getTelephone() != null ? reservation.getUtilisateur().getTelephone() : "Non spécifié")
                + "\nPlaces: " + reservation.getNombrePlaces()
                + "\nMontant total: " + String.format("%.0f DH", reservation.getMontantTotal())
                + "\nDate réservation: " + reservation.getDateReservation().format(DATE_FORMATTER)
                + "\nStatut: " + reservation.getStatut().getLabel());

        reservationSection.add(reservationTitle, reservationDetails);

        // Comment
        if (reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty()) {
            VerticalLayout commentSection = new VerticalLayout();
            commentSection.setPadding(true);
            commentSection.getStyle().set("background", "#f0f0f0").set("border-radius", "8px");

            H3 commentTitle = new H3("💬 Commentaire");
            commentTitle.getStyle().set("margin", "0 0 1rem 0");

            Div commentDiv = new Div();
            commentDiv.setText(reservation.getCommentaire());

            commentSection.add(commentTitle, commentDiv);
            content.add(commentSection);
        }

        content.add(eventSection, reservationSection);

        dialog.add(content);

        Button closeButton = new Button("Fermer", e -> dialog.close());
        dialog.getFooter().add(closeButton);

        dialog.open();
    }

    private void confirmReservation(Reservation reservation) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmer la réservation");
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);

        Paragraph message = new Paragraph("Êtes-vous sûr de vouloir confirmer la réservation de " +
                reservation.getUtilisateur().getFullName() + " pour " +
                reservation.getNombrePlaces() + " place(s) ?");

        TextArea commentField = new TextArea("Commentaire optionnel");
        commentField.setWidthFull();
        commentField.setHeight("100px");
        commentField.setPlaceholder("Ajouter un commentaire pour le client...");
        commentField.setValue(reservation.getCommentaire() != null ? reservation.getCommentaire() : "");

        content.add(message, commentField);
        dialog.add(content);

        Button cancelButton = new Button("Annuler", e -> dialog.close());
        Button confirmButton = new Button("Confirmer", e -> {
            try {
                reservation.setCommentaire(commentField.getValue());
                reservationService.confirmReservation(reservation.getId(), currentUser);
                showNotification("✓ Réservation confirmée", NotificationVariant.LUMO_SUCCESS);
                loadReservations();
                dialog.close();
            } catch (Exception ex) {
                showNotification("❌ Erreur: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    private void rejectReservation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Rejeter la réservation");
        dialog.setText("Êtes-vous sûr de vouloir rejeter la réservation de " +
                reservation.getUtilisateur().getFullName() + " ?");
        dialog.setCancelable(true);
        dialog.setCancelText("Non");
        dialog.setConfirmText("Oui, rejeter");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                reservationService.cancelReservation(reservation.getId(), currentUser);
                showNotification("✓ Réservation rejetée", NotificationVariant.LUMO_SUCCESS);
                loadReservations();
            } catch (Exception ex) {
                showNotification("❌ Erreur: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private String getStatusColor(ReservationStatus status) {
        return switch (status) {
            case EN_ATTENTE -> "#F39C12";
            case CONFIRMEE -> "#27AE60";
            case ANNULEE -> "#E74C3C";
        };
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}
