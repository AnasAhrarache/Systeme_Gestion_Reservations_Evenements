package com.event.views.organizer;

import com.event.model.entities.Event;
import com.event.model.entities.User;
import com.event.model.enums.EventCategory;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.EventService;
import com.event.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;

import java.time.LocalDateTime;
import java.util.Arrays;

@Route(value = "organizer/event/:action/:eventId?", layout = MainLayout.class)
@PageTitle("Créer/Modifier Événement | EventPro")
public class EventFormView extends VerticalLayout implements HasUrlParameter<String>, BeforeEnterObserver {

    private final EventService eventService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private User currentUser;
    private Event event;
    private Binder<Event> binder;
    private boolean isEditMode = false;
    private boolean initialized = false;

    // Form fields
    private TextField titreField;
    private TextArea descriptionField;
    private ComboBox<EventCategory> categorieField;
    private DateTimePicker dateDebutField;
    private DateTimePicker dateFinField;
    private TextField lieuField;
    private ComboBox<String> villeField;
    private IntegerField capaciteMaxField;
    private NumberField prixUnitaireField;
    private TextField imageUrlField;

    public EventFormView(EventService eventService,
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

        initialized = true;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        if (parameter == null) {
            navigationManager.navigateToMyEvents();
            return;
        }

        String[] parts = parameter.split("/");
        String action = parts[0];
        String eventIdStr = parts.length > 1 ? parts[1] : null;

        if ("new".equals(action)) {
            isEditMode = false;
            event = new Event();
            createForm();
        } else if ("edit".equals(action) && eventIdStr != null) {
            isEditMode = true;
            try {
                Long eventId = Long.parseLong(eventIdStr);
                event = eventService.findById(eventId);
                createForm();
                binder.readBean(event);
            } catch (Exception e) {
                showError("Événement introuvable");
            }
        } else {
            navigationManager.navigateToMyEvents();
        }
    }

    private void createForm() {
        createHeader();
        createFormContent();
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

        H1 title = new H1(isEditMode ? "✏️ Modifier l'événement" : "➕ Créer un événement");
        title.getStyle()
                .set("margin", "0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph(isEditMode
                ? "Modifiez les informations de votre événement"
                : "Remplissez le formulaire pour créer un nouvel événement");
        subtitle.getStyle()
                .set("margin", "0.5rem 0 0 0")
                .set("opacity", "0.9");

        titleSection.add(title, subtitle);

        Button cancelButton = new Button("Annuler", VaadinIcon.ARROW_LEFT.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.getStyle()
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("border", "1px solid rgba(255,255,255,0.3)");
        cancelButton.addClickListener(e -> navigationManager.navigateToMyEvents());

        headerContent.add(titleSection, cancelButton);
        header.add(headerContent);
        add(header);
    }

    private void createFormContent() {
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setPadding(true);
        content.getStyle()
                .set("max-width", "1000px")
                .set("margin", "0 auto")
                .set("padding", "2rem")
                .set("background", "#f7fafc");

        VerticalLayout formCard = new VerticalLayout();
        formCard.setPadding(true);
        formCard.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        // Title
        titreField = new TextField("Titre *");
        titreField.setPrefixComponent(VaadinIcon.TEXT_INPUT.create());
        titreField.setRequiredIndicatorVisible(true);
        titreField.setWidthFull();
        titreField.setPlaceholder("Ex: Festival de Musique 2026");

        // Description
        descriptionField = new TextArea("Description");
        descriptionField.setPrefixComponent(VaadinIcon.FILE_TEXT.create());
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(1000);
        descriptionField.setPlaceholder("Décrivez votre événement...");
        formLayout.setColspan(descriptionField, 2);

        // Category
        categorieField = new ComboBox<>("Catégorie *");
        categorieField.setItems(Arrays.asList(EventCategory.values()));
        categorieField.setItemLabelGenerator(EventCategory::getLabel);
        categorieField.setRequiredIndicatorVisible(true);
        categorieField.setWidthFull();

        // Dates
        dateDebutField = new DateTimePicker("Date de début *");
        dateDebutField.setRequiredIndicatorVisible(true);
        dateDebutField.setMin(LocalDateTime.now());
        dateDebutField.setWidthFull();

        dateFinField = new DateTimePicker("Date de fin *");
        dateFinField.setRequiredIndicatorVisible(true);
        dateFinField.setMin(LocalDateTime.now());
        dateFinField.setWidthFull();

        // Location
        lieuField = new TextField("Lieu *");
        lieuField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        lieuField.setRequiredIndicatorVisible(true);
        lieuField.setWidthFull();
        lieuField.setPlaceholder("Ex: Théâtre Mohammed V");

        villeField = new ComboBox<>("Ville *");
        villeField.setItems("Casablanca", "Rabat", "Marrakech", "Tanger", "Fès", "Agadir", "Meknès", "Oujda");
        villeField.setRequiredIndicatorVisible(true);
        villeField.setWidthFull();

        // Capacity and Price
        capaciteMaxField = new IntegerField("Capacité maximale *");
        capaciteMaxField.setPrefixComponent(VaadinIcon.USERS.create());
        capaciteMaxField.setRequiredIndicatorVisible(true);
        capaciteMaxField.setMin(1);
        capaciteMaxField.setWidthFull();

        prixUnitaireField = new NumberField("Prix unitaire (DH) *");
        prixUnitaireField.setPrefixComponent(VaadinIcon.MONEY.create());
        prixUnitaireField.setRequiredIndicatorVisible(true);
        prixUnitaireField.setMin(0);
        prixUnitaireField.setWidthFull();

        // Image URL
        imageUrlField = new TextField("URL de l'image");
        imageUrlField.setPrefixComponent(VaadinIcon.PICTURE.create());
        imageUrlField.setWidthFull();
        imageUrlField.setPlaceholder("https://example.com/image.jpg");
        formLayout.setColspan(imageUrlField, 2);

        formLayout.add(
                titreField, categorieField,
                descriptionField,
                dateDebutField, dateFinField,
                lieuField, villeField,
                capaciteMaxField, prixUnitaireField,
                imageUrlField
        );

        // Setup binder
        setupBinder();

        // Action buttons
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonsLayout.setSpacing(true);
        buttonsLayout.getStyle().set("margin-top", "2rem");

        Button cancelBtn = new Button("Annuler", VaadinIcon.CLOSE.create());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> navigationManager.navigateToMyEvents());

        Button saveBtn = new Button(isEditMode ? "Enregistrer" : "Créer l'événement",
                isEditMode ? VaadinIcon.CHECK.create() : VaadinIcon.PLUS.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        saveBtn.addClickListener(e -> saveEvent());

        buttonsLayout.add(cancelBtn, saveBtn);

        formCard.add(formLayout, buttonsLayout);
        content.add(formCard);
        add(content);
    }

    private void setupBinder() {
        binder = new Binder<>(Event.class);

        binder.forField(titreField)
                .asRequired("Le titre est obligatoire")
                .withValidator(titre -> titre.length() >= 5, "Le titre doit contenir au moins 5 caractères")
                .withValidator(titre -> titre.length() <= 100, "Le titre ne peut pas dépasser 100 caractères")
                .bind(Event::getTitre, Event::setTitre);

        binder.forField(descriptionField)
                .withValidator(desc -> desc == null || desc.length() <= 1000,
                        "La description ne peut pas dépasser 1000 caractères")
                .bind(Event::getDescription, Event::setDescription);

        binder.forField(categorieField)
                .asRequired("La catégorie est obligatoire")
                .bind(Event::getCategorie, Event::setCategorie);

        binder.forField(dateDebutField)
                .asRequired("La date de début est obligatoire")
                .withValidator(date -> date == null || date.isAfter(LocalDateTime.now()),
                        "La date de début doit être dans le futur")
                .bind(Event::getDateDebut, Event::setDateDebut);

        binder.forField(dateFinField)
                .asRequired("La date de fin est obligatoire")
                .withValidator(dateFin -> {
                    LocalDateTime dateDebut = dateDebutField.getValue();
                    return dateDebut == null || dateFin == null || dateFin.isAfter(dateDebut);
                }, "La date de fin doit être après la date de début")
                .bind(Event::getDateFin, Event::setDateFin);

        binder.forField(lieuField)
                .asRequired("Le lieu est obligatoire")
                .bind(Event::getLieu, Event::setLieu);

        binder.forField(villeField)
                .asRequired("La ville est obligatoire")
                .bind(Event::getVille, Event::setVille);

        binder.forField(capaciteMaxField)
                .asRequired("La capacité maximale est obligatoire")
                .withValidator(cap -> cap != null && cap > 0, "La capacité doit être supérieure à 0")
                .bind(Event::getCapaciteMax, Event::setCapaciteMax);

        binder.forField(prixUnitaireField)
                .asRequired("Le prix unitaire est obligatoire")
                .withValidator(prix -> prix != null && prix >= 0, "Le prix doit être positif ou nul")
                .bind(Event::getPrixUnitaire, Event::setPrixUnitaire);

        binder.forField(imageUrlField)
                .bind(Event::getImageUrl, Event::setImageUrl);
    }

    private void saveEvent() {
        try {
            binder.writeBean(event);

            if (isEditMode) {
                eventService.updateEvent(event.getId(), event, currentUser);
                showNotification("✓ Événement modifié avec succès", NotificationVariant.LUMO_SUCCESS);
            } else {
                eventService.createEvent(event, currentUser);
                showNotification("✓ Événement créé avec succès", NotificationVariant.LUMO_SUCCESS);
            }

            navigationManager.navigateToMyEvents();
        } catch (ValidationException e) {
            showNotification("Veuillez corriger les erreurs dans le formulaire", NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showError(String message) {
        VerticalLayout errorView = new VerticalLayout();
        errorView.setSizeFull();
        errorView.setAlignItems(Alignment.CENTER);
        errorView.setJustifyContentMode(JustifyContentMode.CENTER);

        Span errorIcon = new Span("❌");
        errorIcon.getStyle()
                .set("font-size", "4rem")
                .set("display", "block")
                .set("margin-bottom", "1rem");

        H2 errorTitle = new H2(message);
        errorTitle.getStyle()
                .set("color", "#2d3748")
                .set("margin", "0 0 2rem 0");

        Button backButton = new Button("Retour", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backButton.addClickListener(e -> navigationManager.navigateToMyEvents());

        errorView.add(errorIcon, errorTitle, backButton);
        add(errorView);
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}

