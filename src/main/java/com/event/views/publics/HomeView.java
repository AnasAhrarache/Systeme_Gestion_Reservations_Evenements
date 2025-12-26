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
        getStyle().set("background", "#f8fafc");

        createHeroSection();
        createCategoriesSection();
        createPopularEventsSection();
        createFeaturesSection();
        createFooterCTA();
    }

    /* ==========================================================
                           HERO SECTION
       ========================================================== */

    private void createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setWidthFull();
        hero.setMinHeight("600px");
        hero.setAlignItems(Alignment.CENTER);
        hero.setJustifyContentMode(JustifyContentMode.CENTER);
        hero.getStyle()
                .set("background-image", "url('https://images.unsplash.com/photo-1506784983877-45594efa4cbe?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s='), linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("background-size", "cover, 400% 400%")
                .set("background-position", "center center, 0% 50%")
                .set("animation", "gradientShift 15s ease infinite")
                .set("color", "white")
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("padding", "4rem 2rem");

        // Add animated background pattern
        addHeroBackgroundPattern(hero);

        // Content container with glassmorphism
        VerticalLayout contentBox = new VerticalLayout();
        contentBox.setAlignItems(Alignment.CENTER);
        contentBox.setMaxWidth("900px");
        contentBox.getStyle()
                .set("background", "rgba(255, 255, 255, 0.1)")
                .set("backdrop-filter", "blur(10px)")
                .set("border-radius", "24px")
                .set("padding", "3rem 2rem")
                .set("border", "1px solid rgba(255, 255, 255, 0.2)")
                .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)");

        H1 title = new H1("🎫 Découvrez les Meilleurs Événements");
        title.getStyle()
                .set("font-size", "clamp(2rem, 5vw, 3.5rem)")
                .set("font-weight", "800")
                .set("text-align", "center")
                .set("margin", "0")
                .set("text-shadow", "0 4px 20px rgba(0, 0, 0, 0.3)")
                .set("line-height", "1.2");

        Paragraph subtitle = new Paragraph(
                "Concerts, théâtres, conférences et bien plus encore. Réservez vos places en quelques clics."
        );
        subtitle.getStyle()
                .set("font-size", "1.25rem")
                .set("text-align", "center")
                .set("margin", "1rem 0 2rem 0")
                .set("opacity", "0.95")
                .set("max-width", "600px");

        Button explore = new Button("Explorer les événements", VaadinIcon.SEARCH.create());
        explore.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        explore.getStyle()
                .set("background", "white")
                .set("color", "#667eea")
                .set("font-weight", "600")
                .set("padding", "1rem 2.5rem")
                .set("border-radius", "50px")
                .set("box-shadow", "0 10px 30px rgba(0, 0, 0, 0.2)")
                .set("transition", "all 0.3s ease")
                .set("border", "none");
        explore.addClickListener(e -> navigationManager.navigateToEvents());

        Button account = new Button();
        account.addThemeVariants(ButtonVariant.LUMO_LARGE);
        account.getStyle()
                .set("background", "rgba(255, 255, 255, 0.2)")
                .set("color", "white")
                .set("font-weight", "600")
                .set("padding", "1rem 2.5rem")
                .set("border-radius", "50px")
                .set("border", "2px solid white")
                .set("backdrop-filter", "blur(10px)")
                .set("transition", "all 0.3s ease");

        if (sessionManager.isUserLoggedIn()) {
            account.setText("Mon compte");
            account.setIcon(VaadinIcon.USER.create());
            account.addClickListener(e -> navigationManager.navigateToHome());
        } else {
            account.setText("Se connecter");
            account.setIcon(VaadinIcon.SIGN_IN.create());
            account.addClickListener(e -> navigationManager.navigateToLogin());
        }

        // Add hover effects
        addButtonHoverEffect(explore, "transform: translateY(-3px); box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);");
        addButtonHoverEffect(account, "transform: translateY(-3px); background: rgba(255, 255, 255, 0.3);");

        HorizontalLayout buttons = new HorizontalLayout(explore, account);
        buttons.setSpacing(true);
        buttons.getStyle().set("gap", "1rem");

        contentBox.add(title, subtitle, buttons);
        hero.add(contentBox);
        add(hero);

        // Add CSS animation
        addGlobalStyles();
    }

    private void addHeroBackgroundPattern(VerticalLayout hero) {
        Div pattern = new Div();
        pattern.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("left", "0")
                .set("width", "100%")
                .set("height", "100%")
                .set("opacity", "0.1")
                .set("background-image", 
                     "radial-gradient(circle at 20% 50%, white 2px, transparent 2px), " +
                     "radial-gradient(circle at 80% 80%, white 2px, transparent 2px)")
                .set("background-size", "100px 100px")
                .set("pointer-events", "none");
        hero.getElement().insertChild(0, pattern.getElement());
    }

    private void addButtonHoverEffect(Button button, String hoverStyle) {
        button.getElement().addEventListener("mouseenter", e -> {
            button.getElement().getStyle().set("cssText", 
                button.getElement().getStyle().get("cssText") + hoverStyle);
        });
        button.getElement().addEventListener("mouseleave", e -> {
            button.getElement().getStyle().remove("transform");
        });
    }

    /* ==========================================================
                         CATEGORIES SECTION
       ========================================================== */

    private void createCategoriesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.getStyle()
                .set("padding", "4rem 2rem")
                .set("max-width", "1400px")
                .set("margin", "0 auto");

        H2 title = new H2("Parcourir par catégorie");
        title.getStyle()
                .set("text-align", "center")
                .set("font-size", "2.5rem")
                .set("font-weight", "700")
                .set("color", "#1a202c")
                .set("margin-bottom", "3rem");

        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.setJustifyContentMode(JustifyContentMode.CENTER);
        grid.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "1.5rem");

        for (EventCategory category : EventCategory.values()) {
            grid.add(createCategoryCard(category));
        }

        section.add(title, grid);
        add(section);
    }

    private VerticalLayout createCategoryCard(EventCategory category) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("220px");
        card.setHeight("180px");
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.getStyle()
                .set("border", "2px solid " + category.getColor())
                .set("border-radius", "20px")
                .set("cursor", "pointer")
                .set("background", "white")
                .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.07)")
                .set("position", "relative")
                .set("overflow", "hidden");

        // Background circle
        Div bgCircle = new Div();
        bgCircle.getStyle()
                .set("position", "absolute")
                .set("width", "120px")
                .set("height", "120px")
                .set("border-radius", "50%")
                .set("background", category.getColor())
                .set("opacity", "0.1")
                .set("top", "-30px")
                .set("right", "-30px")
                .set("transition", "all 0.3s ease");

        Icon icon = category.getIcon().create();
        icon.setSize("56px");
        icon.getStyle()
                .set("color", category.getColor())
                .set("transition", "transform 0.3s ease");

        Span label = new Span(category.getLabel());
        label.getStyle()
                .set("font-size", "1.1rem")
                .set("font-weight", "600")
                .set("color", "#2d3748")
                .set("margin-top", "1rem");

        card.add(bgCircle, icon, label);

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-8px) scale(1.02)")
                    .set("box-shadow", "0 20px 40px rgba(0, 0, 0, 0.15)")
                    .set("border-color", category.getColor());
            icon.getStyle().set("transform", "scale(1.1) rotate(5deg)");
            bgCircle.getStyle().set("opacity", "0.15").set("transform", "scale(1.2)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0) scale(1)")
                    .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.07)");
            icon.getStyle().set("transform", "scale(1) rotate(0deg)");
            bgCircle.getStyle().set("opacity", "0.1").set("transform", "scale(1)");
        });

        card.addClickListener(e -> navigationManager.navigateToEvents());

        return card;
    }

    /* ==========================================================
                       POPULAR EVENTS (DTO)
       ========================================================== */

    private void createPopularEventsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.getStyle()
                .set("background", "linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%)")
                .set("padding", "4rem 2rem");

        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("1400px");
        container.getStyle().set("margin", "0 auto");

        H2 title = new H2("Événements populaires");
        title.getStyle()
                .set("text-align", "center")
                .set("font-size", "2.5rem")
                .set("font-weight", "700")
                .set("color", "#1a202c")
                .set("margin-bottom", "3rem");

        List<EventDTO> events = eventService.getMostPopularEventsDTO(6);

        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "2rem")
                .set("justify-content", "center");

        if (events.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement disponible");
            noEvents.getStyle()
                    .set("text-align", "center")
                    .set("color", "#718096")
                    .set("font-size", "1.2rem");
            container.add(title, noEvents);
        } else {
            events.forEach(e -> grid.add(createEventCard(e)));
            container.add(title, grid);
        }

        Button viewAll = new Button("Voir tous les événements", VaadinIcon.ARROW_RIGHT.create());
        viewAll.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewAll.getStyle()
                .set("margin-top", "2rem")
                .set("background", "#667eea")
                .set("color", "white")
                .set("font-weight", "600")
                .set("padding", "0.875rem 2rem")
                .set("border-radius", "12px")
                .set("box-shadow", "0 10px 25px rgba(102, 126, 234, 0.3)")
                .set("transition", "all 0.3s ease")
                .set("border", "none");
        viewAll.addClickListener(e -> navigationManager.navigateToEvents());

        addButtonHoverEffect(viewAll, "transform: translateY(-3px); box-shadow: 0 15px 35px rgba(102, 126, 234, 0.4);");

        container.add(viewAll);
        container.setHorizontalComponentAlignment(Alignment.CENTER, viewAll);
        section.add(container);
        add(section);
    }

    private VerticalLayout createEventCard(EventDTO event) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("380px");
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
                .set("border-radius", "20px")
                .set("box-shadow", "0 10px 30px rgba(0, 0, 0, 0.1)")
                .set("cursor", "pointer")
                .set("background", "white")
                .set("overflow", "hidden")
                .set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)");

        card.addClickListener(e ->
                navigationManager.navigateToEventDetails(event.getId())
        );

        // Image container with overlay
        Div imageContainer = new Div();
        imageContainer.setWidthFull();
        imageContainer.setHeight("240px");
        imageContainer.getStyle()
                .set("position", "relative")
                .set("overflow", "hidden");

        Div image = new Div();
        image.setWidthFull();
        image.setHeight("100%");
        image.getStyle()
                .set("background-image", "url('" +
                        (event.getImageUrl() != null
                                ? event.getImageUrl()
                                : "https://images.unsplash.com/photo-1492684223066-81342ee5ff30")
                        + "')")
                .set("background-size", "cover")
                .set("background-position", "center")
                .set("transition", "transform 0.5s ease");

        // Gradient overlay
        Div overlay = new Div();
        overlay.getStyle()
                .set("position", "absolute")
                .set("bottom", "0")
                .set("left", "0")
                .set("right", "0")
                .set("height", "50%")
                .set("background", "linear-gradient(to top, rgba(0,0,0,0.4), transparent)")
                .set("pointer-events", "none");

        // Category badge
        Span category = new Span(event.getCategorie().getLabel());
        category.getStyle()
                .set("position", "absolute")
                .set("top", "1rem")
                .set("left", "1rem")
                .set("background", event.getCategorie().getColor())
                .set("color", "white")
                .set("padding", "0.5rem 1rem")
                .set("border-radius", "20px")
                .set("font-weight", "600")
                .set("font-size", "0.875rem")
                .set("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.15)")
                .set("z-index", "10");

        imageContainer.add(image, overlay, category);

        // Content
        VerticalLayout content = new VerticalLayout();
        content.getStyle()
                .set("padding", "1.5rem")
                .set("gap", "0.75rem");

        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "#1a202c")
                .set("line-height", "1.3");

        // Date and location
        HorizontalLayout dateLocation = new HorizontalLayout();
        dateLocation.setSpacing(true);
        dateLocation.getStyle().set("gap", "1.5rem");

        HorizontalLayout dateInfo = new HorizontalLayout();
        Icon calendarIcon = VaadinIcon.CALENDAR.create();
        calendarIcon.setSize("18px");
        calendarIcon.getStyle().set("color", "#667eea");
        Span date = new Span(event.getDateDebut().format(DATE_FORMATTER));
        date.getStyle().set("color", "#4a5568").set("font-size", "0.95rem");
        dateInfo.add(calendarIcon, date);
        dateInfo.setAlignItems(Alignment.CENTER);

        HorizontalLayout locationInfo = new HorizontalLayout();
        Icon locationIcon = VaadinIcon.MAP_MARKER.create();
        locationIcon.setSize("18px");
        locationIcon.getStyle().set("color", "#667eea");
        Span location = new Span(event.getVille());
        location.getStyle().set("color", "#4a5568").set("font-size", "0.95rem");
        locationInfo.add(locationIcon, location);
        locationInfo.setAlignItems(Alignment.CENTER);

        dateLocation.add(dateInfo, locationInfo);

        // Price and availability
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);
        footer.getStyle()
                .set("margin-top", "0.5rem")
                .set("padding-top", "1rem")
                .set("border-top", "1px solid #e2e8f0");

        Span price = new Span(String.format("%.0f DH", event.getPrixUnitaire()));
        price.getStyle()
                .set("font-weight", "700")
                .set("font-size", "1.5rem")
                .set("color", "#667eea");

        Span availability = new Span(event.getPlacesDisponibles() + " places");
        availability.getStyle()
                .set("color", event.getPlacesDisponibles() > 10 ? "#48bb78" : "#f56565")
                .set("font-weight", "600")
                .set("font-size", "0.95rem")
                .set("padding", "0.375rem 0.75rem")
                .set("background", event.getPlacesDisponibles() > 10 ? "#f0fff4" : "#fff5f5")
                .set("border-radius", "8px");

        footer.add(price, availability);
        content.add(title, dateLocation, footer);
        card.add(imageContainer, content);

        // Hover effects
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-12px)")
                    .set("box-shadow", "0 25px 50px rgba(0, 0, 0, 0.15)");
            image.getStyle().set("transform", "scale(1.1)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 10px 30px rgba(0, 0, 0, 0.1)");
            image.getStyle().set("transform", "scale(1)");
        });

        return card;
    }

    /* ==========================================================
                         FEATURES SECTION
       ========================================================== */

    private void createFeaturesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.getStyle()
                .set("padding", "4rem 2rem")
                .set("background", "white");

        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("1400px");
        container.getStyle().set("margin", "0 auto");

        H2 title = new H2("Pourquoi choisir EventPro ?");
        title.getStyle()
                .set("text-align", "center")
                .set("font-size", "2.5rem")
                .set("font-weight", "700")
                .set("color", "#1a202c")
                .set("margin-bottom", "3rem");

        HorizontalLayout grid = new HorizontalLayout(
                createFeatureCard(VaadinIcon.SEARCH, "Recherche facile",
                        "Trouvez rapidement l'événement idéal grâce à notre système de recherche avancé", "#667eea"),
                createFeatureCard(VaadinIcon.TICKET, "Réservation simple",
                        "Réservez vos places en quelques clics avec notre processus simplifié", "#764ba2"),
                createFeatureCard(VaadinIcon.SHIELD, "Paiement sécurisé",
                        "Transactions 100% protégées avec les meilleurs standards de sécurité", "#48bb78")
        );

        grid.setWidthFull();
        grid.setJustifyContentMode(JustifyContentMode.CENTER);
        grid.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "2rem");

        container.add(title, grid);
        section.add(container);
        add(section);
    }

    private VerticalLayout createFeatureCard(VaadinIcon icon,
                                             String title,
                                             String description,
                                             String color) {

        VerticalLayout card = new VerticalLayout();
        card.setWidth("360px");
        card.setAlignItems(Alignment.CENTER);
        card.getStyle()
                .set("padding", "2.5rem 2rem")
                .set("border-radius", "20px")
                .set("background", "white")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.05)")
                .set("transition", "all 0.3s ease")
                .set("text-align", "center");

        // Icon container
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "80px")
                .set("height", "80px")
                .set("border-radius", "50%")
                .set("background", "linear-gradient(135deg, " + color + " 0%, " + color + "dd 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "1.5rem")
                .set("box-shadow", "0 10px 25px " + color + "40")
                .set("transition", "all 0.3s ease");

        Icon i = icon.create();
        i.setSize("40px");
        i.getStyle().set("color", "white");
        iconContainer.add(i);

        H3 h3 = new H3(title);
        h3.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "#1a202c");

        Paragraph p = new Paragraph(description);
        p.getStyle()
                .set("margin", "0")
                .set("color", "#718096")
                .set("line-height", "1.6")
                .set("font-size", "1rem");

        card.add(iconContainer, h3, p);

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-8px)")
                    .set("box-shadow", "0 20px 40px rgba(0, 0, 0, 0.1)");
            iconContainer.getStyle()
                    .set("transform", "scale(1.1) rotate(5deg)")
                    .set("box-shadow", "0 15px 35px " + color + "60");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.05)");
            iconContainer.getStyle()
                    .set("transform", "scale(1) rotate(0deg)")
                    .set("box-shadow", "0 10px 25px " + color + "40");
        });

        return card;
    }

    /* ==========================================================
                         FOOTER CTA
       ========================================================== */

    private void createFooterCTA() {
        VerticalLayout cta = new VerticalLayout();
        cta.setWidthFull();
        cta.setAlignItems(Alignment.CENTER);
        cta.setJustifyContentMode(JustifyContentMode.CENTER);
        cta.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "4rem 2rem")
                .set("color", "white")
                .set("text-align", "center");

        H2 ctaTitle = new H2("Prêt à vivre une expérience inoubliable ?");
        ctaTitle.getStyle()
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("margin", "0 0 1rem 0");

        Paragraph ctaText = new Paragraph("Rejoignez des milliers d'utilisateurs qui font confiance à EventPro");
        ctaText.getStyle()
                .set("font-size", "1.1rem")
                .set("opacity", "0.9")
                .set("margin-bottom", "2rem");

        Button ctaButton = new Button("Commencer maintenant", VaadinIcon.ARROW_FORWARD.create());
        ctaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ctaButton.getStyle()
                .set("background", "white")
                .set("color", "#667eea")
                .set("font-weight", "600")
                .set("padding", "1rem 2.5rem")
                .set("border-radius", "50px")
                .set("box-shadow", "0 10px 30px rgba(0, 0, 0, 0.2)")
                .set("transition", "all 0.3s ease")
                .set("border", "none");
        ctaButton.addClickListener(e -> navigationManager.navigateToEvents());

        addButtonHoverEffect(ctaButton, "transform: translateY(-3px) scale(1.05); box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);");

        cta.add(ctaTitle, ctaText, ctaButton);
        add(cta);
    }

    /* ==========================================================
                         GLOBAL STYLES
       ========================================================== */

    private void addGlobalStyles() {
        // Add CSS keyframes for animations
        getElement().executeJs(
                "if (!document.getElementById('hero-animations')) {" +
                "  const style = document.createElement('style');" +
                "  style.id = 'hero-animations';" +
                "  style.innerHTML = `" +
                "    @keyframes gradientShift {" +
                "      0% { background-position: 0% 50%; }" +
                "      50% { background-position: 100% 50%; }" +
                "      100% { background-position: 0% 50%; }" +
                "    }" +
                "  `;" +
                "  document.head.appendChild(style);" +
                "}"
        );
    }
}