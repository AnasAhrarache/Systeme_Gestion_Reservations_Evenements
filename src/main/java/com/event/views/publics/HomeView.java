package com.event.views.publics;

import com.event.dto.EventDTO;
import com.event.model.enums.EventCategory;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Accueil | EventPro")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public HomeView(EventService eventService,
                    NavigationManager navigationManager,
                    SessionManager sessionManager) {

        this.eventService = eventService;
        this.navigationManager = navigationManager;
        this.sessionManager = sessionManager;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        createHeroSection();
        createCategoriesSection();
        createPopularEventsSection();
        createFeaturesSection();
    }

    /* ==========================================================
                           HERO SECTION
       ========================================================== */

    private void createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setWidthFull();
        hero.setHeight("500px");
        hero.setAlignItems(Alignment.CENTER);
        hero.setJustifyContentMode(JustifyContentMode.CENTER);
        hero.getStyle()
                .set("background", "linear-gradient(135deg, #667eea, #764ba2)")
                .set("color", "white");

        H1 title = new H1("🎫 Découvrez les Meilleurs Événements");
        title.getStyle().set("font-size", "3rem");

        Paragraph subtitle = new Paragraph(
                "Concerts, théâtres, conférences et bien plus encore."
        );

        Button explore = new Button("Explorer les événements", VaadinIcon.SEARCH.create());
        explore.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        explore.addClickListener(e -> navigationManager.navigateToEvents());

        Button account = new Button();
        account.addThemeVariants(ButtonVariant.LUMO_LARGE);

        if (sessionManager.isUserLoggedIn()) {
            account.setText("Mon compte");
            account.setIcon(VaadinIcon.USER.create());
            account.addClickListener(e -> navigationManager.navigateToHome());
        } else {
            account.setText("Se connecter");
            account.setIcon(VaadinIcon.SIGN_IN.create());
            account.addClickListener(e -> navigationManager.navigateToLogin());
        }

        HorizontalLayout buttons = new HorizontalLayout(explore, account);
        hero.add(title, subtitle, buttons);
        add(hero);
    }

    /* ==========================================================
                         CATEGORIES SECTION
       ========================================================== */

    private void createCategoriesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();

        H2 title = new H2("Parcourir par catégorie");

        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.setJustifyContentMode(JustifyContentMode.CENTER);
        grid.getStyle().set("flex-wrap", "wrap");

        for (EventCategory category : EventCategory.values()) {
            grid.add(createCategoryCard(category));
        }

        section.add(title, grid);
        add(section);
    }

    private VerticalLayout createCategoryCard(EventCategory category) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("200px");
        card.setAlignItems(Alignment.CENTER);
        card.getStyle()
                .set("border", "2px solid " + category.getColor())
                .set("border-radius", "12px")
                .set("cursor", "pointer");

        Icon icon = category.getIcon().create();
        icon.setSize("48px");
        icon.getStyle().set("color", category.getColor());

        Span label = new Span(category.getLabel());

        card.add(icon, label);
        card.addClickListener(e -> navigationManager.navigateToEvents());

        return card;
    }

    /* ==========================================================
                       POPULAR EVENTS (DTO)
       ========================================================== */

    private void createPopularEventsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.getStyle().set("background", "#f7fafc");

        H2 title = new H2("Événements populaires");

        List<EventDTO> events = eventService.getMostPopularEventsDTO(6);

        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.getStyle().set("flex-wrap", "wrap");
        grid.setJustifyContentMode(JustifyContentMode.CENTER);

        if (events.isEmpty()) {
            section.add(title, new Paragraph("Aucun événement disponible"));
        } else {
            events.forEach(e -> grid.add(createEventCard(e)));
            section.add(title, grid);
        }

        Button viewAll = new Button("Voir tous les événements", VaadinIcon.ARROW_RIGHT.create());
        viewAll.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewAll.addClickListener(e -> navigationManager.navigateToEvents());

        section.add(viewAll);
        section.setHorizontalComponentAlignment(Alignment.CENTER, viewAll);
        add(section);
    }

    private VerticalLayout createEventCard(EventDTO event) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("350px");
        card.getStyle()
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("cursor", "pointer");

        card.addClickListener(e ->
                navigationManager.navigateToEventDetails(event.getId())
        );

        Div image = new Div();
        image.setHeight("200px");
        image.getStyle()
                .set("background-image", "url('" +
                        (event.getImageUrl() != null
                                ? event.getImageUrl()
                                : "https://images.unsplash.com/photo-1492684223066-81342ee5ff30")
                        + "')")
                .set("background-size", "cover")
                .set("background-position", "center");

        Span category = new Span(event.getCategorie().getLabel());
        category.getStyle()
                .set("background", event.getCategorie().getColor())
                .set("color", "white")
                .set("padding", "0.25rem 0.75rem")
                .set("border-radius", "12px");

        H3 title = new H3(event.getTitre());

        Span date = new Span(event.getDateDebut().format(DATE_FORMATTER));
        Span location = new Span(event.getVille());

        Span price = new Span(String.format("%.0f DH", event.getPrixUnitaire()));
        price.getStyle().set("font-weight", "700");

        Span availability = new Span(event.getPlacesDisponibles() + " places");
        availability.getStyle()
                .set("color", event.getPlacesDisponibles() > 10 ? "#48bb78" : "#f56565");

        VerticalLayout content = new VerticalLayout(
                category,
                title,
                date,
                location,
                price,
                availability
        );

        card.add(image, content);
        return card;
    }

    /* ==========================================================
                         FEATURES SECTION
       ========================================================== */

    private void createFeaturesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();

        H2 title = new H2("Pourquoi choisir EventPro ?");

        HorizontalLayout grid = new HorizontalLayout(
                createFeatureCard(VaadinIcon.SEARCH, "Recherche facile",
                        "Trouvez rapidement l'événement idéal"),
                createFeatureCard(VaadinIcon.TICKET, "Réservation simple",
                        "Réservez en quelques clics"),
                createFeatureCard(VaadinIcon.SHIELD, "Paiement sécurisé",
                        "Transactions protégées")
        );

        grid.setJustifyContentMode(JustifyContentMode.CENTER);
        grid.getStyle().set("flex-wrap", "wrap");

        section.add(title, grid);
        add(section);
    }

    private VerticalLayout createFeatureCard(VaadinIcon icon,
                                             String title,
                                             String description) {

        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        card.setAlignItems(Alignment.CENTER);

        Icon i = icon.create();
        i.setSize("40px");

        card.add(i, new H3(title), new Paragraph(description));
        return card;
    }
}
