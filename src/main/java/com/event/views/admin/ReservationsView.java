package com.event.views.admin;

import com.event.model.entities.Reservation;
import com.event.model.entities.User;
import com.event.model.enums.ReservationStatus;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.ReservationService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/reservations", layout = MainLayout.class)
@PageTitle("Gestion des Réservations | EventPro")
public class ReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final ReservationService reservationService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private User currentUser;
    private Grid<Reservation> grid;
    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;
    private List<Reservation> allReservations;
    private boolean initialized = false;

    public ReservationsView(ReservationService reservationService,
                            NavigationManager navigationManager,
                            SessionManager sessionManager) {
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
                .set("background", "linear-gradient(135deg, #FF6B6B 0%, #C92A2A 100%)")
                .set("color", "white")
                .set("padding", "2rem");

        H1 title = new H1("🎫 Gestion des Réservations");
        title.getStyle()
                .set("margin", "0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Consultez et gérez toutes les réservations de la plateforme");
        subtitle.getStyle()
                .set("margin", "0.5rem 0 0 0")
                .set("opacity", "0.9");

        header.add(title, subtitle);
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
        searchField.setPlaceholder("Code de réservation, nom d'utilisateur...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterReservations());
        searchField.setWidth("300px");

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(Arrays.asList(ReservationStatus.values()));
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> filterReservations());
        statusFilter.setWidth("200px");

        Button refreshButton = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> loadReservations());

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

        grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();
        grid.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Code column
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setWidth("150px")
                .setFlexGrow(0);

        // User column
        grid.addColumn(res -> res.getUtilisateur().getFullName())
                .setHeader("Utilisateur")
                .setAutoWidth(true);

        // Event column
        grid.addColumn(res -> res.getEvenement().getTitre())
                .setHeader("Événement")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Event date column
        grid.addColumn(res -> res.getEvenement().getDateDebut().format(DATE_FORMATTER))
                .setHeader("Date événement")
                .setAutoWidth(true);

        // Reservation date column
        grid.addColumn(res -> res.getDateReservation().format(DATE_FORMATTER))
                .setHeader("Date réservation")
                .setAutoWidth(true);

        // Places column
        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setWidth("100px")
                .setFlexGrow(0);

        // Amount column
        grid.addColumn(res -> String.format("%.0f DH", res.getMontantTotal()))
                .setHeader("Montant")
                .setWidth("120px")
                .setFlexGrow(0);

        // Status column with badge
        grid.addColumn(new ComponentRenderer<>(reservation -> {
            Span badge = new Span(reservation.getStatut().getLabel());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", reservation.getStatut().getColor())
                    .set("color", "white")
                    .set("padding", "0.5rem 1rem")
                    .set("border-radius", "12px")
                    .set("font-size", "0.875rem")
                    .set("font-weight", "600");
            return badge;
        })).setHeader("Statut").setWidth("150px").setFlexGrow(0);

        // Actions column
        grid.addColumn(new ComponentRenderer<>(reservation -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button detailsButton = new Button(VaadinIcon.EYE.create());
            detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            detailsButton.getElement().setAttribute("title", "Voir détails");
            detailsButton.addClickListener(e -> showReservationDetails(reservation));

            if (reservation.getStatut() == ReservationStatus.EN_ATTENTE) {
                Button confirmButton = new Button(VaadinIcon.CHECK.create());
                confirmButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                confirmButton.getElement().setAttribute("title", "Confirmer");
                confirmButton.addClickListener(e -> confirmReservation(reservation));
                actions.add(confirmButton);
            }

            if (!reservation.getStatut().isCancelled() && reservation.canBeCancelled()) {
                Button cancelButton = new Button(VaadinIcon.CLOSE.create());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelButton.getElement().setAttribute("title", "Annuler");
                cancelButton.addClickListener(e -> confirmCancelReservation(reservation));
                actions.add(cancelButton);
            }

            actions.add(detailsButton);
            return actions;
        })).setHeader("Actions").setWidth("200px").setFlexGrow(0);

        gridSection.add(grid);
        add(gridSection);
    }

    private void loadReservations() {
        try {
            allReservations = reservationService.getAllReservations();
            filterReservations();
        } catch (Exception e) {
            showNotification("Erreur lors du chargement des réservations", NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterReservations() {
        List<Reservation> filtered = allReservations;

        // Filter by search
        String searchTerm = searchField.getValue();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            filtered = filtered.stream()
                    .filter(r -> r.getCodeReservation().toLowerCase().contains(lowerSearch) ||
                            r.getUtilisateur().getFullName().toLowerCase().contains(lowerSearch) ||
                            r.getEvenement().getTitre().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toList());
        }

        // Filter by status
        ReservationStatus status = statusFilter.getValue();
        if (status != null) {
            filtered = filtered.stream()
                    .filter(r -> r.getStatut() == status)
                    .collect(Collectors.toList());
        }

        grid.setItems(filtered);
    }

    private void showReservationDetails(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Détails de la réservation");
        dialog.setCancelable(true);
        dialog.setConfirmText("Fermer");
        dialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        content.add(createDetailRow("Code", reservation.getCodeReservation()));
        content.add(createDetailRow("Utilisateur", reservation.getUtilisateur().getFullName()));
        content.add(createDetailRow("Email", reservation.getUtilisateur().getEmail()));
        content.add(createDetailRow("Événement", reservation.getEvenement().getTitre()));
        content.add(createDetailRow("Date événement", reservation.getEvenement().getDateDebut().format(DATE_FORMATTER)));
        content.add(createDetailRow("Nombre de places", String.valueOf(reservation.getNombrePlaces())));
        content.add(createDetailRow("Montant total", String.format("%.0f DH", reservation.getMontantTotal())));
        content.add(createDetailRow("Statut", reservation.getStatut().getLabel()));
        content.add(createDetailRow("Date réservation", reservation.getDateReservation().format(DATE_FORMATTER)));

        if (reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty()) {
            content.add(createDetailRow("Commentaire", reservation.getCommentaire()));
        }

        dialog.add(content);
        dialog.open();
    }

    private HorizontalLayout createDetailRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.getStyle()
                .set("padding", "0.75rem 0")
                .set("border-bottom", "1px solid #e2e8f0");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "#4a5568");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "#2d3748")
                .set("text-align", "right");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private void confirmReservation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmer la réservation");
        dialog.setText("Êtes-vous sûr de vouloir confirmer cette réservation ?");
        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");
        dialog.setConfirmText("Confirmer");
        dialog.setConfirmButtonTheme("success primary");

        dialog.addConfirmListener(e -> confirmReservationAction(reservation));
        dialog.open();
    }

    private void confirmReservationAction(Reservation reservation) {
        try {
            reservationService.confirmReservation(reservation.getId(), currentUser);
            showNotification("✓ Réservation confirmée avec succès", NotificationVariant.LUMO_SUCCESS);
            loadReservations();
        } catch (Exception e) {
            showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmCancelReservation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Annuler la réservation");
        dialog.setText("Êtes-vous sûr de vouloir annuler cette réservation ?");
        dialog.setCancelable(true);
        dialog.setCancelText("Non");
        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> cancelReservation(reservation));
        dialog.open();
    }

    private void cancelReservation(Reservation reservation) {
        try {
            reservationService.cancelReservation(reservation.getId(), currentUser);
            showNotification("✓ Réservation annulée", NotificationVariant.LUMO_SUCCESS);
            loadReservations();
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

