package com.event.views.publics;

import com.event.model.entities.Reservation;
import com.event.model.entities.User;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.EventService;
import com.event.service.ReservationService;
import com.event.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "event/:eventId/reserve", layout = MainLayout.class)
@PageTitle("Réserver | EventPro")
public class EventReservationView extends VerticalLayout implements BeforeEnterObserver {

    private final ReservationService reservationService;
    private final EventService eventService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private Long eventId;
    private User currentUser;

    private IntegerField placesField;
    private TextArea commentField;
    private Span totalSpan;
    private Button confirmButton;

    private static final Logger log = LoggerFactory.getLogger(EventReservationView.class);

    public EventReservationView(ReservationService reservationService,
                                EventService eventService,
                                NavigationManager navigationManager,
                                SessionManager sessionManager) {
        this.reservationService = reservationService;
        this.eventService = eventService;
        this.navigationManager = navigationManager;
        this.sessionManager = sessionManager;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        String idParam = beforeEnterEvent.getRouteParameters().get("eventId").orElse(null);
        if (idParam == null) {
            navigationManager.navigateToEvents();
            return;
        }

        try {
            this.eventId = Long.parseLong(idParam);
            this.currentUser = sessionManager.requireAuthentication();
            createView();
        } catch (Exception e) {
            add(createErrorView("Événement introuvable"));
        }
    }

    private void createView() {
        add(createHeader());

        HorizontalLayout content = new HorizontalLayout();
        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(true);

        // Left: brief event summary
        VerticalLayout left = new VerticalLayout();
        left.setWidth("60%");
        left.setPadding(true);
        left.getStyle().set("background", "white").set("border-radius", "12px");

        left.add(new H2("Confirmez votre réservation"));
        left.add(new Paragraph("Événement: " + eventService.getEventDTO(eventId).getTitre()));
        left.add(new Paragraph("Date: " + eventService.getEventDTO(eventId).getDateDebut()));
        left.add(new Paragraph("Lieu: " + eventService.getEventDTO(eventId).getLieu()));

        // Right: booking form
        VerticalLayout right = new VerticalLayout();
        right.setWidth("40%");
        right.setPadding(true);
        right.getStyle().set("background", "white").set("border-radius", "12px");

        placesField = new IntegerField("Nombre de places");
        placesField.setMin(1);
        placesField.setValue(1);
        placesField.setWidthFull();
        placesField.addValueChangeListener(e -> updateTotal());

        commentField = new TextArea("Commentaire (optionnel)");
        commentField.setWidthFull();
        commentField.setMaxLength(500);

        totalSpan = new Span("Total: 0 DH");
        totalSpan.getStyle().set("font-weight", "700").set("font-size", "1.25rem");

        confirmButton = new Button("Confirmer la réservation", VaadinIcon.TICKET.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.setWidthFull();
        confirmButton.addClickListener(e -> handleConfirm());

        Button cancelBtn = new Button("Annuler", VaadinIcon.ARROW_LEFT.create());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.setWidthFull();
        cancelBtn.addClickListener(e -> navigationManager.navigateBack());

        right.add(placesField, commentField, totalSpan, confirmButton, cancelBtn);

        content.add(left, right);
        add(content);

        updateTotal();
    }

    private void updateTotal() {
        try {
            int places = placesField.getValue() != null ? placesField.getValue() : 0;
            double price = eventService.getEventDTO(eventId).getPrixUnitaire();
            totalSpan.setText("Total: " + String.format("%.0f DH", price * places));
        } catch (Exception ignored) {
            totalSpan.setText("Total: -");
        }
    }

    private void handleConfirm() {
        try {
            int places = placesField.getValue() != null ? placesField.getValue() : 0;
            if (places <= 0) {
                showNotification("Le nombre de places doit être au moins 1", NotificationVariant.LUMO_ERROR);
                return;
            }

            Reservation reservation = new Reservation();
            reservation.setNombrePlaces(places);
            reservation.setCommentaire(commentField.getValue());

            reservationService.createReservation(reservation, currentUser, eventId);

            showNotification("✓ Réservation créée avec succès", NotificationVariant.LUMO_SUCCESS);
            navigationManager.navigateToMyReservations();
        } catch (Throwable t) {
            // Log full stacktrace so server logs contain details for debugging
            log.error("Error creating reservation for event {} by user {}", eventId,
                    currentUser != null ? currentUser.getEmail() : "anonymous", t);

            // Show concise message to user
            String msg = t.getMessage() != null ? t.getMessage() : "Erreur interne";
            showNotification("Erreur lors de la création de la réservation: " + msg + " (voir logs)",
                    NotificationVariant.LUMO_ERROR);
        }
    }

    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)").set("color", "white");
        H1 title = new H1("📝 Finaliser la réservation");
        header.add(title);
        return header;
    }

    private VerticalLayout createErrorView(String message) {
        VerticalLayout errorView = new VerticalLayout();
        errorView.setSizeFull();
        errorView.setAlignItems(Alignment.CENTER);
        errorView.setJustifyContentMode(JustifyContentMode.CENTER);

        Span icon = new Span("❌");
        icon.getStyle().set("font-size", "4rem");
        H2 title = new H2(message);
        Button back = new Button("Retour aux événements", VaadinIcon.ARROW_LEFT.create());
        back.addClickListener(e -> navigationManager.navigateToEvents());

        errorView.add(icon, title, back);
        return errorView;
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification n = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(variant);
        n.open();
    }
}
