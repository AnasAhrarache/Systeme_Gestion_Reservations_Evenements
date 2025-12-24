package com.event.views.client;

import com.event.model.entities.User;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.UserService;
import com.event.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Mon Profil | EventPro")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

        private final UserService userService;
        private final NavigationManager navigationManager;
        private final SessionManager sessionManager;

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private User currentUser;
        private Binder<User> binder;
        private boolean initialized = false;

        // Profile form fields
        private TextField nomField;
        private TextField prenomField;
        private EmailField emailField;
        private TextField telephoneField;

        // Password form fields
        private PasswordField currentPasswordField;
        private PasswordField newPasswordField;
        private PasswordField confirmPasswordField;

        public ProfileView(UserService userService,
                        NavigationManager navigationManager,
                        SessionManager sessionManager) {
                this.userService = userService;
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
                        // Not authenticated: redirect to login
                        navigationManager.navigateToLogin();
                        return;
                }

                // Now it's safe to build the UI using currentUser
                createHeader();
                createContent();
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

                H1 title = new H1("👤 Mon Profil");
                title.getStyle()
                                .set("margin", "0")
                                .set("font-weight", "700");

                Paragraph subtitle = new Paragraph("Gérez vos informations personnelles");
                subtitle.getStyle()
                                .set("margin", "0.5rem 0 0 0")
                                .set("opacity", "0.9");

                header.add(title, subtitle);
                add(header);
        }

        private void createContent() {
                HorizontalLayout mainContent = new HorizontalLayout();
                mainContent.setWidthFull();
                mainContent.setSpacing(true);
                mainContent.setPadding(true);
                mainContent.getStyle()
                                .set("max-width", "1200px")
                                .set("margin", "0 auto")
                                .set("padding", "2rem")
                                .set("background", "#f7fafc");

                // Left column - Profile info
                VerticalLayout leftColumn = createLeftColumn();
                leftColumn.setWidth("40%");

                // Right column - Forms
                VerticalLayout rightColumn = createRightColumn();
                rightColumn.setWidth("60%");

                mainContent.add(leftColumn, rightColumn);
                add(mainContent);
        }

        private VerticalLayout createLeftColumn() {
                VerticalLayout leftColumn = new VerticalLayout();
                leftColumn.setPadding(false);
                leftColumn.setSpacing(true);

                // Profile card
                VerticalLayout profileCard = new VerticalLayout();
                profileCard.setPadding(true);
                profileCard.setAlignItems(Alignment.CENTER);
                profileCard.getStyle()
                                .set("background", "white")
                                .set("border-radius", "12px")
                                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                                .set("text-align", "center");

                // Avatar
                Div avatar = new Div();
                avatar.setWidth("120px");
                avatar.setHeight("120px");
                avatar.getStyle()
                                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                                .set("border-radius", "50%")
                                .set("display", "flex")
                                .set("align-items", "center")
                                .set("justify-content", "center")
                                .set("color", "white")
                                .set("font-size", "3rem")
                                .set("font-weight", "700")
                                .set("margin-bottom", "1rem");

                avatar.add(new Span(currentUser.getInitials()));

                H2 userName = new H2(currentUser.getFullName());
                userName.getStyle()
                                .set("margin", "0 0 0.5rem 0")
                                .set("color", "#2d3748");

                Span roleBadge = new Span(currentUser.getRole().getLabel());
                roleBadge.getStyle()
                                .set("background", currentUser.getRole().getColor())
                                .set("color", "white")
                                .set("padding", "0.5rem 1rem")
                                .set("border-radius", "20px")
                                .set("font-size", "0.875rem")
                                .set("font-weight", "600")
                                .set("display", "inline-block")
                                .set("margin-bottom", "1rem");

                Paragraph memberSince = new Paragraph("Membre depuis le " +
                                currentUser.getDateInscription().format(DATE_FORMATTER));
                memberSince.getStyle()
                                .set("margin", "0")
                                .set("color", "#718096")
                                .set("font-size", "0.9rem");

                profileCard.add(avatar, userName, roleBadge, memberSince);

                // Statistics card
                VerticalLayout statsCard = createStatisticsCard();

                leftColumn.add(profileCard, statsCard);
                return leftColumn;
        }

        private VerticalLayout createStatisticsCard() {
                VerticalLayout statsCard = new VerticalLayout();
                statsCard.setPadding(true);
                statsCard.getStyle()
                                .set("background", "white")
                                .set("border-radius", "12px")
                                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");

                H3 statsTitle = new H3("📊 Statistiques");
                statsTitle.getStyle()
                                .set("margin", "0 0 1.5rem 0")
                                .set("color", "#2d3748");

                Map<String, Object> stats = userService.getUserStatistics(currentUser.getId());

                statsCard.add(statsTitle);
                statsCard.add(createStatRow(
                                VaadinIcon.CALENDAR,
                                "Événements créés",
                                stats.get("eventsCreated").toString(),
                                "#667eea"));
                statsCard.add(createStatRow(
                                VaadinIcon.TICKET,
                                "Réservations",
                                stats.get("totalReservations").toString(),
                                "#48bb78"));
                statsCard.add(createStatRow(
                                VaadinIcon.MONEY,
                                "Total dépensé",
                                String.format("%.0f DH", (Double) stats.get("totalSpent")),
                                "#764ba2"));

                return statsCard;
        }

        private HorizontalLayout createStatRow(VaadinIcon iconType, String label, String value, String color) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                row.setAlignItems(FlexComponent.Alignment.CENTER);
                row.getStyle()
                                .set("padding", "1rem 0")
                                .set("border-bottom", "1px solid #e2e8f0");

                HorizontalLayout leftSide = new HorizontalLayout();
                leftSide.setSpacing(true);
                leftSide.setAlignItems(FlexComponent.Alignment.CENTER);

                Div iconContainer = new Div();
                iconContainer.setWidth("40px");
                iconContainer.setHeight("40px");
                iconContainer.getStyle()
                                .set("background", color)
                                .set("border-radius", "8px")
                                .set("display", "flex")
                                .set("align-items", "center")
                                .set("justify-content", "center");

                var icon = iconType.create();
                icon.setSize("20px");
                icon.getStyle().set("color", "white");
                iconContainer.add(icon);

                Span labelSpan = new Span(label);
                labelSpan.getStyle()
                                .set("color", "#4a5568")
                                .set("font-weight", "500");

                leftSide.add(iconContainer, labelSpan);

                Span valueSpan = new Span(value);
                valueSpan.getStyle()
                                .set("color", "#2d3748")
                                .set("font-weight", "700")
                                .set("font-size", "1.125rem");

                row.add(leftSide, valueSpan);
                return row;
        }

        private VerticalLayout createRightColumn() {
                VerticalLayout rightColumn = new VerticalLayout();
                rightColumn.setPadding(false);
                rightColumn.setSpacing(true);

                // Profile form
                VerticalLayout profileForm = createProfileForm();

                // Password form
                VerticalLayout passwordForm = createPasswordForm();

                // Danger zone
                VerticalLayout dangerZone = createDangerZone();

                rightColumn.add(profileForm, passwordForm, dangerZone);
                return rightColumn;
        }

        private VerticalLayout createProfileForm() {
                VerticalLayout formCard = new VerticalLayout();
                formCard.setPadding(true);
                formCard.getStyle()
                                .set("background", "white")
                                .set("border-radius", "12px")
                                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");

                H3 formTitle = new H3("✏️ Informations personnelles");
                formTitle.getStyle()
                                .set("margin", "0 0 1.5rem 0")
                                .set("color", "#2d3748");

                FormLayout formLayout = new FormLayout();
                formLayout.setResponsiveSteps(
                                new FormLayout.ResponsiveStep("0", 1),
                                new FormLayout.ResponsiveStep("500px", 2));

                nomField = new TextField("Nom");
                nomField.setPrefixComponent(VaadinIcon.USER.create());
                nomField.setRequiredIndicatorVisible(true);

                prenomField = new TextField("Prénom");
                prenomField.setPrefixComponent(VaadinIcon.USER.create());
                prenomField.setRequiredIndicatorVisible(true);

                emailField = new EmailField("Email");
                emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
                emailField.setRequiredIndicatorVisible(true);
                emailField.setClearButtonVisible(true);

                telephoneField = new TextField("Téléphone");
                telephoneField.setPrefixComponent(VaadinIcon.PHONE.create());
                telephoneField.setPlaceholder("06XXXXXXXX");

                // Setup binder
                setupBinder();

                // Load current user data
                binder.readBean(currentUser);

                formLayout.add(nomField, prenomField, emailField, telephoneField);
                formLayout.setColspan(emailField, 2);
                formLayout.setColspan(telephoneField, 2);

                Button saveButton = new Button("Enregistrer les modifications", VaadinIcon.CHECK.create());
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                saveButton.addClickListener(e -> saveProfile());

                formCard.add(formTitle, formLayout, saveButton);
                return formCard;
        }

        private void setupBinder() {
                binder = new Binder<>(User.class);

                binder.forField(nomField)
                                .asRequired("Le nom est obligatoire")
                                .withValidator(nom -> nom.length() >= 2, "Le nom doit contenir au moins 2 caractères")
                                .bind(User::getNom, User::setNom);

                binder.forField(prenomField)
                                .asRequired("Le prénom est obligatoire")
                                .withValidator(prenom -> prenom.length() >= 2,
                                                "Le prénom doit contenir au moins 2 caractères")
                                .bind(User::getPrenom, User::setPrenom);

                binder.forField(emailField)
                                .asRequired("L'email est obligatoire")
                                .withValidator(new EmailValidator("Format d'email invalide"))
                                .bind(User::getEmail, User::setEmail);

                binder.forField(telephoneField)
                                .bind(User::getTelephone, User::setTelephone);
        }

        private void saveProfile() {
                try {
                        binder.writeBean(currentUser);
                        User updated = userService.updateProfile(currentUser.getId(), currentUser);

                        // Update session
                        sessionManager.setCurrentUser(updated);
                        currentUser = updated;

                        showNotification("✓ Profil mis à jour avec succès", NotificationVariant.LUMO_SUCCESS);
                } catch (ValidationException e) {
                        showNotification("Veuillez corriger les erreurs dans le formulaire",
                                        NotificationVariant.LUMO_ERROR);
                } catch (Exception e) {
                        showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
                }
        }

        private VerticalLayout createPasswordForm() {
                VerticalLayout formCard = new VerticalLayout();
                formCard.setPadding(true);
                formCard.getStyle()
                                .set("background", "white")
                                .set("border-radius", "12px")
                                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");

                H3 formTitle = new H3("🔒 Changer le mot de passe");
                formTitle.getStyle()
                                .set("margin", "0 0 1.5rem 0")
                                .set("color", "#2d3748");

                FormLayout formLayout = new FormLayout();
                formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

                currentPasswordField = new PasswordField("Mot de passe actuel");
                currentPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
                currentPasswordField.setRequiredIndicatorVisible(true);

                newPasswordField = new PasswordField("Nouveau mot de passe");
                newPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
                newPasswordField.setRequiredIndicatorVisible(true);
                newPasswordField.setHelperText("Au moins 8 caractères, une majuscule, une minuscule et un chiffre");

                confirmPasswordField = new PasswordField("Confirmer le nouveau mot de passe");
                confirmPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
                confirmPasswordField.setRequiredIndicatorVisible(true);

                formLayout.add(currentPasswordField, newPasswordField, confirmPasswordField);

                Button changePasswordButton = new Button("Changer le mot de passe", VaadinIcon.KEY.create());
                changePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                changePasswordButton.addClickListener(e -> changePassword());

                formCard.add(formTitle, formLayout, changePasswordButton);
                return formCard;
        }

        private void changePassword() {
                String currentPassword = currentPasswordField.getValue();
                String newPassword = newPasswordField.getValue();
                String confirmPassword = confirmPasswordField.getValue();

                // Validation
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        showNotification("Veuillez remplir tous les champs", NotificationVariant.LUMO_ERROR);
                        return;
                }

                if (!newPassword.equals(confirmPassword)) {
                        showNotification("Les mots de passe ne correspondent pas", NotificationVariant.LUMO_ERROR);
                        return;
                }

                if (newPassword.length() < 8) {
                        showNotification("Le mot de passe doit contenir au moins 8 caractères",
                                        NotificationVariant.LUMO_ERROR);
                        return;
                }

                try {
                        userService.changePassword(currentUser.getId(), currentPassword, newPassword);

                        // Clear fields
                        currentPasswordField.clear();
                        newPasswordField.clear();
                        confirmPasswordField.clear();

                        showNotification("✓ Mot de passe changé avec succès", NotificationVariant.LUMO_SUCCESS);
                } catch (Exception e) {
                        showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
                }
        }

        private VerticalLayout createDangerZone() {
                VerticalLayout dangerCard = new VerticalLayout();
                dangerCard.setPadding(true);
                dangerCard.getStyle()
                                .set("background", "white")
                                .set("border-radius", "12px")
                                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                                .set("border", "2px solid #f56565");

                H3 dangerTitle = new H3("⚠️ Zone dangereuse");
                dangerTitle.getStyle()
                                .set("margin", "0 0 1rem 0")
                                .set("color", "#c53030");

                Paragraph dangerDescription = new Paragraph(
                                "La désactivation de votre compte est permanente. Vous ne pourrez plus vous connecter et toutes vos réservations seront annulées.");
                dangerDescription.getStyle()
                                .set("margin", "0 0 1.5rem 0")
                                .set("color", "#4a5568");

                Button deactivateButton = new Button("Désactiver mon compte", VaadinIcon.BAN.create());
                deactivateButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                deactivateButton.addClickListener(e -> confirmDeactivateAccount());

                dangerCard.add(dangerTitle, dangerDescription, deactivateButton);
                return dangerCard;
        }

        private void confirmDeactivateAccount() {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("⚠️ Désactiver le compte");
                dialog.setWidth("500px");

                VerticalLayout content = new VerticalLayout();
                content.setPadding(false);
                content.setSpacing(true);

                Paragraph message = new Paragraph(
                                "Êtes-vous absolument sûr de vouloir désactiver votre compte ?");
                message.getStyle()
                                .set("margin", "0 0 1rem 0")
                                .set("color", "#4a5568");

                VerticalLayout warningBox = new VerticalLayout();
                warningBox.setPadding(true);
                warningBox.getStyle()
                                .set("background", "#fef2f2")
                                .set("border-left", "4px solid #ef4444")
                                .set("border-radius", "4px");

                H4 warningTitle = new H4("Cette action est irréversible !");
                warningTitle.getStyle()
                                .set("margin", "0 0 0.5rem 0")
                                .set("color", "#991b1b");

                UnorderedList consequences = new UnorderedList();
                consequences.getStyle()
                                .set("margin", "0")
                                .set("padding-left", "1.5rem")
                                .set("color", "#7f1d1d");

                consequences.add(new ListItem("Vous ne pourrez plus vous connecter"));
                consequences.add(new ListItem("Toutes vos réservations seront annulées"));
                consequences.add(new ListItem("Vos données seront conservées mais inaccessibles"));

                warningBox.add(warningTitle, consequences);
                content.add(message, warningBox);

                dialog.add(content);
                dialog.setCancelable(true);
                dialog.setCancelText("Non, garder mon compte");
                dialog.setConfirmText("Oui, désactiver");
                dialog.setConfirmButtonTheme("error primary");

                dialog.addConfirmListener(e -> deactivateAccount());
                dialog.open();
        }

        private void deactivateAccount() {
                try {
                        userService.deactivateAccount(currentUser.getId());

                        showNotification("Compte désactivé. Redirection...", NotificationVariant.LUMO_SUCCESS);

                        // Logout and redirect
                        sessionManager.logout();
                        navigationManager.navigateToLogin();
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