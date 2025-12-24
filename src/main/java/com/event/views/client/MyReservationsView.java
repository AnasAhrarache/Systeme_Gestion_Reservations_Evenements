package com.event.views.client;

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

@Route(value = "my-reservations", layout = MainLayout.class)
@PageTitle("Mes Réservations | EventPro")
public class MyReservationsView extends VerticalLayout implements BeforeEnterObserver {

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

    public MyReservationsView(ReservationService reservationService,
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
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "2rem");

        H1 title = new H1("🎫 Mes Réservations");
        title.getStyle()
                .set("margin", "0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Gérez toutes vos réservations d'événements");
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
        searchField.setPlaceholder("Code de réservation...");
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

        // Event column
        grid.addColumn(res -> res.getEvenement().getTitre())
                .setHeader("Événement")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Date column
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

            Button cancelButton = new Button(VaadinIcon.CLOSE.create());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            cancelButton.getElement().setAttribute("title", "Annuler");
            cancelButton.setEnabled(reservation.canBeCancelled() && !reservation.getStatut().isCancelled());
            cancelButton.addClickListener(e -> confirmCancelReservation(reservation));

            actions.add(detailsButton, cancelButton);
            return actions;
        })).setHeader("Actions").setWidth("150px").setFlexGrow(0);

        gridSection.add(grid);
        add(gridSection);
    }

    private void loadReservations() {
        try {
            allReservations = reservationService.getReservationsByUser(currentUser);
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
            filtered = filtered.stream()
                    .filter(r -> r.getCodeReservation().toLowerCase().contains(searchTerm.toLowerCase()))
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
        content.getStyle()
                .set("max-height", "600px")
                .set("overflow-y", "auto");

        // Main info section
        VerticalLayout mainInfo = new VerticalLayout();
        mainInfo.setPadding(true);
        mainInfo.setSpacing(true);
        mainInfo.getStyle()
                .set("background", "#f7fafc")
                .set("border-radius", "8px")
                .set("margin-bottom", "1rem");

        H3 reservationTitle = new H3("Réservation " + reservation.getCodeReservation());
        reservationTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "#2d3748");

        mainInfo.add(reservationTitle);
        mainInfo.add(createDetailRow("Code", reservation.getCodeReservation()));
        mainInfo.add(createDetailRow("Statut", reservation.getStatut().getLabel()));
        mainInfo.add(createDetailRow("Date de réservation", reservation.getDateReservation().format(DATE_FORMATTER)));

        // Event info section
        VerticalLayout eventInfo = new VerticalLayout();
        eventInfo.setPadding(true);
        eventInfo.setSpacing(true);
        eventInfo.getStyle()
                .set("background", "#f7fafc")
                .set("border-radius", "8px")
                .set("margin-bottom", "1rem");

        H3 eventTitle = new H3("Informations de l'événement");
        eventTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "#2d3748");

        eventInfo.add(eventTitle);
        eventInfo.add(createDetailRow("Événement", reservation.getEvenement().getTitre()));
        eventInfo.add(createDetailRow("Catégorie", reservation.getEvenement().getCategorie().getLabel()));
        eventInfo.add(createDetailRow("Date", reservation.getEvenement().getDateDebut().format(DATE_FORMATTER)));
        eventInfo.add(createDetailRow("Lieu", reservation.getEvenement().getLieu()));
        eventInfo.add(createDetailRow("Ville", reservation.getEvenement().getVille()));

        // Payment info section
        VerticalLayout paymentInfo = new VerticalLayout();
        paymentInfo.setPadding(true);
        paymentInfo.setSpacing(true);
        paymentInfo.getStyle()
                .set("background", "#f7fafc")
                .set("border-radius", "8px")
                .set("margin-bottom", "1rem");

        H3 paymentTitle = new H3("Informations de paiement");
        paymentTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "#2d3748");

        paymentInfo.add(paymentTitle);
        paymentInfo.add(createDetailRow("Nombre de places", String.valueOf(reservation.getNombrePlaces())));
        paymentInfo.add(createDetailRow("Prix unitaire", String.format("%.0f DH", reservation.getEvenement().getPrixUnitaire())));

        HorizontalLayout totalRow = new HorizontalLayout();
        totalRow.setWidthFull();
        totalRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        totalRow.getStyle()
                .set("padding", "1rem")
                .set("background", "white")
                .set("border-radius", "8px")
                .set("margin-top", "0.5rem");

        Span totalLabel = new Span("MONTANT TOTAL");
        totalLabel.getStyle()
                .set("font-weight", "700")
                .set("color", "#2d3748")
                .set("font-size", "1.125rem");

        Span totalValue = new Span(String.format("%.0f DH", reservation.getMontantTotal()));
        totalValue.getStyle()
                .set("font-weight", "700")
                .set("color", "#667eea")
                .set("font-size", "1.5rem");

        totalRow.add(totalLabel, totalValue);
        paymentInfo.add(totalRow);

        // Comment section if exists
        if (reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty()) {
            VerticalLayout commentInfo = new VerticalLayout();
            commentInfo.setPadding(true);
            commentInfo.setSpacing(true);
            commentInfo.getStyle()
                    .set("background", "#f7fafc")
                    .set("border-radius", "8px");

            H3 commentTitle = new H3("Commentaire");
            commentTitle.getStyle()
                    .set("margin", "0 0 0.5rem 0")
                    .set("color", "#2d3748");

            Paragraph commentText = new Paragraph(reservation.getCommentaire());
            commentText.getStyle()
                    .set("color", "#4a5568")
                    .set("margin", "0")
                    .set("font-style", "italic");

            commentInfo.add(commentTitle, commentText);
            content.add(commentInfo);
        }

        content.add(mainInfo, eventInfo, paymentInfo);

        // Action buttons
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setWidthFull();
        actionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actionButtons.setSpacing(true);
        actionButtons.getStyle().set("margin-top", "1rem");

        if (reservation.canBeCancelled() && !reservation.getStatut().isCancelled()) {
            Button cancelBtn = new Button("Annuler la réservation", VaadinIcon.CLOSE.create());
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
            cancelBtn.addClickListener(e -> {
                dialog.close();
                confirmCancelReservation(reservation);
            });
            actionButtons.add(cancelBtn);
        }

        Button closeBtn = new Button("Fermer");
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.addClickListener(e -> dialog.close());
        actionButtons.add(closeBtn);

        content.add(actionButtons);

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
                .set("color", "#4a5568")
                .set("font-size", "0.9rem");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "#2d3748")
                .set("font-weight", "500")
                .set("text-align", "right");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private void confirmCancelReservation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Confirmer l'annulation");
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Paragraph message = new Paragraph(
                "Êtes-vous sûr de vouloir annuler cette réservation ?"
        );
        message.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "#4a5568");

        VerticalLayout warningBox = new VerticalLayout();
        warningBox.setPadding(true);
        warningBox.setSpacing(false);
        warningBox.getStyle()
                .set("background", "#fef2f2")
                .set("border-left", "4px solid #ef4444")
                .set("border-radius", "4px")
                .set("padding", "1rem");

        Span warningTitle = new Span("⚠️ Important");
        warningTitle.getStyle()
                .set("font-weight", "700")
                .set("color", "#991b1b")
                .set("display", "block")
                .set("margin-bottom", "0.5rem");

        Paragraph warningText = new Paragraph(
                "Cette action est irréversible. Vous ne pourrez pas récupérer cette réservation."
        );
        warningText.getStyle()
                .set("margin", "0")
                .set("color", "#7f1d1d")
                .set("font-size", "0.9rem");

        warningBox.add(warningTitle, warningText);

        content.add(message, warningBox);

        dialog.add(content);
        dialog.setCancelable(true);
        dialog.setCancelText("Non, garder");
        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> cancelReservation(reservation));
        dialog.open();
    }

    private void cancelReservation(Reservation reservation) {
        try {
            reservationService.cancelReservation(reservation.getId(), currentUser);
            showNotification("✓ Réservation annulée avec succès", NotificationVariant.LUMO_SUCCESS);
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