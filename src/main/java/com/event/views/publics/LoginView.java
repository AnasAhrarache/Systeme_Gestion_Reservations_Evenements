package com.event.views.publics;

import com.event.model.entities.User;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.UserService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Optional;

@Route("login")
@PageTitle("Connexion | EventPro")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final UserService userService;
    private final SessionManager sessionManager;
    private final NavigationManager navigationManager;

    private EmailField emailField;
    private PasswordField passwordField;

    public LoginView(UserService userService,
                     SessionManager sessionManager,
                     NavigationManager navigationManager) {

        this.userService = userService;
        this.sessionManager = sessionManager;
        this.navigationManager = navigationManager;

        if (sessionManager.isUserLoggedIn()) {
            navigationManager.navigateToHome();
            return;
        }

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Gradient + glow background
        getStyle()
                .set("background",
                        "radial-gradient(circle at top left, #7f9cf5, transparent 40%)," +
                        "radial-gradient(circle at bottom right, #9f7aea, transparent 40%)," +
                        "linear-gradient(135deg, #1a202c, #2d3748)")
                .set("padding", "2rem");

        createLoginCard();
    }

    private void createLoginCard() {

        VerticalLayout card = new VerticalLayout();
        card.setWidth("420px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.STRETCH);

        // Glassmorphism card
        card.getStyle()
                .set("background", "rgba(255,255,255,0.92)")
                .set("backdrop-filter", "blur(18px)")
                .set("border-radius", "20px")
                .set("box-shadow", "0 25px 60px rgba(0,0,0,0.35)")
                .set("animation", "fadeIn 0.6s ease-out");

        // Logo badge
        Span badge = new Span(VaadinIcon.TICKET.create());
        badge.getStyle()
                .set("margin", "0 auto")
                .set("background", "linear-gradient(135deg,#667eea,#764ba2)")
                .set("color", "white")
                .set("border-radius", "50%")
                .set("width", "64px")
                .set("height", "64px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "1.75rem")
                .set("box-shadow", "0 10px 30px rgba(102,126,234,.5)");

        H1 title = new H1("EventPro");
        title.getStyle()
                .set("text-align", "center")
                .set("margin", "0.75rem 0 0")
                .set("font-weight", "800")
                .set("color", "#2d3748");

        Paragraph subtitle = new Paragraph("Connectez-vous à votre espace");
        subtitle.getStyle()
                .set("text-align", "center")
                .set("color", "#718096")
                .set("margin", "0 0 1.5rem");

        emailField = new EmailField("Email");
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setPlaceholder("you@eventpro.com");
        emailField.setWidthFull();
        emailField.setClearButtonVisible(true);

        passwordField = new PasswordField("Mot de passe");
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        passwordField.setWidthFull();

        Button loginButton = new Button("Se connecter", VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.setWidthFull();
        loginButton.getStyle()
                .set("margin-top", "1rem")
                .set("background", "linear-gradient(135deg,#667eea,#764ba2)")
                .set("box-shadow", "0 12px 30px rgba(102,126,234,.45)")
                .set("transition", "transform .2s ease, box-shadow .2s ease");

        loginButton.addClickListener(e -> handleLogin());

        loginButton.getElement().addEventListener("mouseover",
                e -> loginButton.getStyle().set("transform", "translateY(-2px)"));
        loginButton.getElement().addEventListener("mouseout",
                e -> loginButton.getStyle().set("transform", "translateY(0)"));

        passwordField.addKeyPressListener(Key.ENTER, e -> handleLogin());

        // Divider
        HorizontalLayout divider = new HorizontalLayout();
        divider.setAlignItems(Alignment.CENTER);
        divider.setWidthFull();

        Span line1 = new Span();
        Span line2 = new Span();
        Span text = new Span("ou");

        line1.getStyle().set("height", "1px").set("background", "#e2e8f0").set("flex", "1");
        line2.getStyle().set("height", "1px").set("background", "#e2e8f0").set("flex", "1");
        text.getStyle().set("margin", "0 .75rem").set("color", "#a0aec0");

        divider.add(line1, text, line2);

        Button registerButton = new Button("Créer un compte", VaadinIcon.USER.create());
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> navigationManager.navigateToRegister());

        card.add(
                badge,
                title,
                subtitle,
                emailField,
                passwordField,
                loginButton,
                divider,
                registerButton
        );

        add(card);
    }

    private void handleLogin() {
        String email = emailField.getValue();
        String password = passwordField.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            showNotification("Veuillez remplir tous les champs", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Optional<User> userOpt = userService.authenticate(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                sessionManager.setCurrentUser(user);

                showNotification(
                        "Bienvenue " + user.getPrenom(),
                        NotificationVariant.LUMO_SUCCESS
                );

                switch (user.getRole()) {
                    case ADMIN -> navigationManager.navigateTo("admin/dashboard");
                    case ORGANIZER -> navigationManager.navigateTo("organizer/dashboard");
                    case CLIENT -> navigationManager.navigateTo("dashboard");
                    default -> navigationManager.navigateToHome();
                }

            } else {
                showNotification("Email ou mot de passe incorrect", NotificationVariant.LUMO_ERROR);
                passwordField.clear();
                passwordField.focus();
            }

        } catch (Exception e) {
            showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification =
                new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}
