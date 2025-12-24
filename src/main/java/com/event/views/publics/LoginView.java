package com.event.views.publics;

import com.event.model.entities.User;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
    private Button loginButton;
    private Button registerButton;

    public LoginView(UserService userService,
                     SessionManager sessionManager,
                     NavigationManager navigationManager) {
        this.userService = userService;
        this.sessionManager = sessionManager;
        this.navigationManager = navigationManager;

        // Check if already logged in
        if (sessionManager.isUserLoggedIn()) {
            navigationManager.navigateToHome();
            return;
        }

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "2rem");

        createLoginForm();
    }

    private void createLoginForm() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("400px");
        formLayout.setPadding(true);
        formLayout.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)");

        // Logo and title
        Span logoIcon = new Span("🎫");
        logoIcon.getStyle()
                .set("font-size", "3rem")
                .set("display", "block")
                .set("text-align", "center");

        H1 title = new H1("EventPro");
        title.getStyle()
                .set("margin", "0")
                .set("text-align", "center")
                .set("color", "#667eea")
                .set("font-weight", "bold");

        H2 subtitle = new H2("Connexion");
        subtitle.getStyle()
                .set("margin", "0.5rem 0 2rem 0")
                .set("text-align", "center")
                .set("color", "#4a5568")
                .set("font-weight", "500")
                .set("font-size", "1.25rem");

        // Email field
        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setPlaceholder("votre@email.ma");
        emailField.setClearButtonVisible(true);
        emailField.setRequiredIndicatorVisible(true);

        // Password field
        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        passwordField.setPlaceholder("Votre mot de passe");
        passwordField.setRequiredIndicatorVisible(true);

        // Login button
        loginButton = new Button("Se connecter", VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.setWidthFull();
        loginButton.getStyle()
                .set("margin-top", "1rem")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none");
        loginButton.addClickListener(e -> handleLogin());

        // Add enter key listener
        passwordField.addKeyPressListener(e -> {
            if (e.getKey().getKeys().get(0).equals("Enter")) {
                handleLogin();
            }
        });

        // Divider
        Span divider = new Span("ou");
        divider.getStyle()
                .set("text-align", "center")
                .set("display", "block")
                .set("margin", "1.5rem 0")
                .set("color", "#a0aec0")
                .set("position", "relative");

        // Register button
        registerButton = new Button("Créer un compte", VaadinIcon.USER_CARD.create());
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> navigationManager.navigateToRegister());

        // Demo accounts info
        VerticalLayout demoInfo = createDemoAccountsInfo();

        formLayout.add(
                logoIcon,
                title,
                subtitle,
                emailField,
                passwordField,
                loginButton,
                divider,
                registerButton,
                demoInfo
        );

        add(formLayout);
    }

    private VerticalLayout createDemoAccountsInfo() {
        VerticalLayout demoLayout = new VerticalLayout();
        demoLayout.setPadding(true);
        demoLayout.setSpacing(false);
        demoLayout.getStyle()
                .set("background", "#f7fafc")
                .set("border-radius", "8px")
                .set("margin-top", "1.5rem");

        Paragraph demoTitle = new Paragraph("🔑 Comptes de démonstration");
        demoTitle.getStyle()
                .set("font-weight", "600")
                .set("color", "#2d3748")
                .set("margin", "0 0 0.5rem 0");

        Paragraph admin = new Paragraph("Admin: admin@event.ma / admin123");
        admin.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#4a5568")
                .set("margin", "0.25rem 0");

        Paragraph organizer = new Paragraph("Organisateur: organizer1@event.ma / org123");
        organizer.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#4a5568")
                .set("margin", "0.25rem 0");

        Paragraph client = new Paragraph("Client: client1@event.ma / client123");
        client.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#4a5568")
                .set("margin", "0.25rem 0");

        demoLayout.add(demoTitle, admin, organizer, client);
        return demoLayout;
    }

    private void handleLogin() {
        String email = emailField.getValue();
        String password = passwordField.getValue();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showNotification("Veuillez remplir tous les champs", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Optional<User> userOpt = userService.authenticate(email, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                sessionManager.setCurrentUser(user);

                showNotification("Connexion réussie! Bienvenue " + user.getPrenom(),
                        NotificationVariant.LUMO_SUCCESS);

                // Navigate based on role
                switch (user.getRole()) {
                    case ADMIN:
                        navigationManager.navigateTo("admin/dashboard");
                        break;
                    case ORGANIZER:
                        navigationManager.navigateTo("organizer/dashboard");
                        break;
                    case CLIENT:
                        navigationManager.navigateTo("dashboard");
                        break;
                    default:
                        navigationManager.navigateTo("");
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
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}