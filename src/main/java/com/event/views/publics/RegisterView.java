package com.event.views.publics;

import com.event.model.entities.User;
import com.event.model.enums.UserRole;
import com.event.security.NavigationManager;
import com.event.service.UserService;
import com.event.util.PasswordEncoder;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Inscription | EventPro")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private final NavigationManager navigationManager;
    private final PasswordEncoder passwordEncoder;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Span passwordStrengthIndicator;

    private Binder<User> binder;

    public RegisterView(UserService userService,
                        NavigationManager navigationManager,
                        PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.navigationManager = navigationManager;
        this.passwordEncoder = passwordEncoder;

        // 🔒 LOCK PAGE SIZE — NO SCROLL
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle()
                .set("overflow", "hidden")
                .set("height", "100vh")
                .set("width", "100vw")
                .set("background",
                        "radial-gradient(circle at top left, #7f9cf5, transparent 40%)," +
                        "radial-gradient(circle at bottom right, #9f7aea, transparent 40%)," +
                        "linear-gradient(135deg, #1a202c, #2d3748)");

        add(createMainContainer());
    }

    private HorizontalLayout createMainContainer() {

        HorizontalLayout container = new HorizontalLayout();
        container.setWidth("900px");
        container.setHeight("520px");
        container.setPadding(false);
        container.setSpacing(false);

        container.getStyle()
                .set("border-radius", "24px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 30px 80px rgba(0,0,0,0.45)");

        container.add(createLeftPanel(), createRightPanel());
        return container;
    }

    // 🔵 LEFT PANEL
    private VerticalLayout createLeftPanel() {

        VerticalLayout left = new VerticalLayout();
        left.setWidth("40%");
        left.setPadding(true);
        left.setJustifyContentMode(JustifyContentMode.CENTER);
        left.setAlignItems(Alignment.START);

        left.getStyle()
                .set("background", "linear-gradient(135deg,#667eea,#764ba2)")
                .set("color", "white");

        Span icon = new Span(VaadinIcon.TICKET.create());
        icon.getStyle().set("font-size", "3rem");

        H1 title = new H1("EventPro");
        title.getStyle().set("margin", "0").set("font-weight", "800");

        Paragraph desc = new Paragraph(
                "La plateforme moderne pour créer, gérer et participer à des événements."
        );
        desc.getStyle().set("opacity", "0.9");

        left.add(icon, title, desc);
        return left;
    }

    // ⚪ RIGHT PANEL
    private VerticalLayout createRightPanel() {

        VerticalLayout right = new VerticalLayout();
        right.setWidth("60%");
        right.setPadding(true);
        right.setSpacing(false);
        right.setAlignItems(Alignment.STRETCH);

        right.getStyle()
                .set("background", "rgba(255,255,255,0.95)")
                .set("backdrop-filter", "blur(16px)");

        H2 formTitle = new H2("Créer un compte");
        formTitle.getStyle().set("margin-bottom", "1rem");

        nomField = new TextField("Nom");
        prenomField = new TextField("Prénom");
        nomField.setPrefixComponent(VaadinIcon.USER.create());
        prenomField.setPrefixComponent(VaadinIcon.USER.create());

        HorizontalLayout names = new HorizontalLayout(nomField, prenomField);
        names.setWidthFull();
        nomField.setWidthFull();
        prenomField.setWidthFull();

        emailField = new EmailField("Email");
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setWidthFull();

        telephoneField = new TextField("Téléphone");
        telephoneField.setPrefixComponent(VaadinIcon.PHONE.create());
        telephoneField.setWidthFull();

        passwordField = new PasswordField("Mot de passe");
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        passwordField.setWidthFull();

        passwordStrengthIndicator = new Span();
        passwordStrengthIndicator.getStyle().set("font-size", "0.8rem");
        passwordField.addValueChangeListener(e -> updatePasswordStrength(e.getValue()));

        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
        confirmPasswordField.setWidthFull();

        setupBinder();

        // ✅ PRIMARY BUTTON — CRÉER UN COMPTE
        Button registerButton = new Button("Créer un compte", VaadinIcon.USER_CHECK.create());
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        registerButton.setWidthFull();
        registerButton.getStyle()
                .set("margin-top", "1rem")
                .set("background", "linear-gradient(135deg,#667eea,#764ba2)");

        registerButton.addClickListener(e -> handleRegistration());
        confirmPasswordField.addKeyPressListener(Key.ENTER, e -> handleRegistration());

        // 🔁 SECONDARY BUTTON — CONNEXION
        Button loginButton = new Button("Déjà un compte ? Se connecter", VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginButton.setWidthFull();
        loginButton.addClickListener(e -> navigationManager.navigateToLogin());

        right.add(
                formTitle,
                names,
                emailField,
                telephoneField,
                passwordField,
                passwordStrengthIndicator,
                confirmPasswordField,
                registerButton,
                loginButton
        );

        return right;
    }

    private void setupBinder() {
        binder = new Binder<>(User.class);

        binder.forField(nomField).asRequired().bind(User::getNom, User::setNom);
        binder.forField(prenomField).asRequired().bind(User::getPrenom, User::setPrenom);
        binder.forField(emailField)
                .asRequired()
                .withValidator(new EmailValidator("Email invalide"))
                .bind(User::getEmail, User::setEmail);

        binder.forField(telephoneField).bind(User::getTelephone, User::setTelephone);
        binder.forField(passwordField).asRequired().bind(User::getPassword, User::setPassword);
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthIndicator.setText("");
            return;
        }

        String msg = passwordEncoder.getPasswordStrengthMessage(password);
        passwordStrengthIndicator.setText(msg);
        passwordStrengthIndicator.getStyle()
                .set("color", passwordEncoder.isPasswordStrong(password) ? "#48bb78" : "#f56565");
    }

    private void handleRegistration() {
        User user = new User();

        if (!binder.writeBeanIfValid(user)) {
            showNotification("Veuillez corriger les erreurs", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            showNotification("Les mots de passe ne correspondent pas", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!passwordEncoder.isPasswordStrong(passwordField.getValue())) {
            showNotification("Mot de passe trop faible", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            user.setRole(UserRole.CLIENT);
            userService.registerUser(user);
            showNotification("Compte créé avec succès 🎉", NotificationVariant.LUMO_SUCCESS);
            navigationManager.navigateToLogin();
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
