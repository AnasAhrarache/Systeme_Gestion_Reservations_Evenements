package com.event.views.publics;

import com.event.model.entities.User;
import com.event.model.enums.UserRole;
import com.event.security.NavigationManager;
import com.event.service.UserService;
import com.event.util.PasswordEncoder;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "2rem");

        createRegistrationForm();
    }

    private void createRegistrationForm() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("450px");
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

        H2 subtitle = new H2("Créer un compte");
        subtitle.getStyle()
                .set("margin", "0.5rem 0 2rem 0")
                .set("text-align", "center")
                .set("color", "#4a5568")
                .set("font-weight", "500")
                .set("font-size", "1.25rem");

        // Form fields
        nomField = new TextField("Nom");
        nomField.setWidthFull();
        nomField.setPrefixComponent(VaadinIcon.USER.create());
        nomField.setRequiredIndicatorVisible(true);

        prenomField = new TextField("Prénom");
        prenomField.setWidthFull();
        prenomField.setPrefixComponent(VaadinIcon.USER.create());
        prenomField.setRequiredIndicatorVisible(true);

        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setPlaceholder("votre@email.ma");
        emailField.setClearButtonVisible(true);
        emailField.setRequiredIndicatorVisible(true);

        telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();
        telephoneField.setPrefixComponent(VaadinIcon.PHONE.create());
        telephoneField.setPlaceholder("06XXXXXXXX");
        telephoneField.setHelperText("Optionnel");

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setHelperText("Au moins 8 caractères, une majuscule, une minuscule et un chiffre");

        // Password strength indicator
        passwordStrengthIndicator = new Span();
        passwordStrengthIndicator.getStyle()
                .set("font-size", "0.875rem")
                .set("margin-top", "-0.5rem")
                .set("margin-bottom", "0.5rem");

        passwordField.addValueChangeListener(e -> updatePasswordStrength(e.getValue()));

        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
        confirmPasswordField.setRequiredIndicatorVisible(true);

        // Setup binder
        setupBinder();

        // Register button
        Button registerButton = new Button("S'inscrire", VaadinIcon.USER_CHECK.create());
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        registerButton.setWidthFull();
        registerButton.getStyle()
                .set("margin-top", "1rem")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none");
        registerButton.addClickListener(e -> handleRegistration());

        // Divider
        Span divider = new Span("Vous avez déjà un compte ?");
        divider.getStyle()
                .set("text-align", "center")
                .set("display", "block")
                .set("margin", "1.5rem 0 1rem 0")
                .set("color", "#a0aec0");

        // Login button
        Button loginButton = new Button("Se connecter", VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        loginButton.setWidthFull();
        loginButton.addClickListener(e -> navigationManager.navigateToLogin());

        formLayout.add(
                logoIcon,
                title,
                subtitle,
                nomField,
                prenomField,
                emailField,
                telephoneField,
                passwordField,
                passwordStrengthIndicator,
                confirmPasswordField,
                registerButton,
                divider,
                loginButton
        );

        add(formLayout);
    }

    private void setupBinder() {
        binder = new Binder<>(User.class);

        binder.forField(nomField)
                .asRequired("Le nom est obligatoire")
                .withValidator(nom -> nom.length() >= 2, "Le nom doit contenir au moins 2 caractères")
                .bind(User::getNom, User::setNom);

        binder.forField(prenomField)
                .asRequired("Le prénom est obligatoire")
                .withValidator(prenom -> prenom.length() >= 2, "Le prénom doit contenir au moins 2 caractères")
                .bind(User::getPrenom, User::setPrenom);

        binder.forField(emailField)
                .asRequired("L'email est obligatoire")
                .withValidator(new EmailValidator("Format d'email invalide"))
                .bind(User::getEmail, User::setEmail);

        binder.forField(telephoneField)
                .bind(User::getTelephone, User::setTelephone);

        binder.forField(passwordField)
                .asRequired("Le mot de passe est obligatoire")
                .withValidator(pwd -> pwd.length() >= 8, "Le mot de passe doit contenir au moins 8 caractères")
                .bind(User::getPassword, User::setPassword);
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordStrengthIndicator.setText("");
            return;
        }

        String message = passwordEncoder.getPasswordStrengthMessage(password);
        passwordStrengthIndicator.setText(message);

        if (message.equals("Mot de passe fort")) {
            passwordStrengthIndicator.getStyle().set("color", "#48bb78");
        } else {
            passwordStrengthIndicator.getStyle().set("color", "#f56565");
        }
    }

    private void handleRegistration() {
        User user = new User();

        if (binder.writeBeanIfValid(user)) {
            // Validate password confirmation
            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                showNotification("Les mots de passe ne correspondent pas", NotificationVariant.LUMO_ERROR);
                return;
            }

            // Validate password strength
            if (!passwordEncoder.isPasswordStrong(passwordField.getValue())) {
                showNotification("Le mot de passe n'est pas assez fort", NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                user.setRole(UserRole.CLIENT);
                userService.registerUser(user);

                showNotification("Inscription réussie! Vous pouvez maintenant vous connecter",
                        NotificationVariant.LUMO_SUCCESS);

                // Navigate to login
                navigationManager.navigateToLogin();

            } catch (Exception e) {
                showNotification("Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        } else {
            showNotification("Veuillez corriger les erreurs dans le formulaire",
                    NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}