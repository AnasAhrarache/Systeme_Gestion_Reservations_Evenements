package com.event.views.admin;

import com.event.model.entities.User;
import com.event.model.enums.UserRole;
import com.event.security.NavigationManager;
import com.event.security.SessionManager;
import com.event.service.UserService;
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

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Gestion des Utilisateurs | EventPro")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final NavigationManager navigationManager;
    private final SessionManager sessionManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private User currentUser;
    private Grid<User> grid;
    private TextField searchField;
    private ComboBox<UserRole> roleFilter;
    private ComboBox<Boolean> activeFilter;
    private List<User> allUsers;
    private boolean initialized = false;

    public UsersView(UserService userService,
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
            navigationManager.navigateToLogin();
            return;
        }

        createHeader();
        createFiltersSection();
        createGridSection();
        loadUsers();
        initialized = true;
    }

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #FF6B6B 0%, #C92A2A 100%)")
                .set("color", "white")
                .set("padding", "2rem");

        H1 title = new H1("👥 Gestion des Utilisateurs");
        title.getStyle()
                .set("margin", "0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Consultez et gérez tous les utilisateurs de la plateforme");
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
        searchField.setPlaceholder("Nom, prénom, email...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> filterUsers());
        searchField.setWidth("300px");

        roleFilter = new ComboBox<>("Rôle");
        roleFilter.setItems(Arrays.asList(UserRole.values()));
        roleFilter.setItemLabelGenerator(UserRole::getLabel);
        roleFilter.setPlaceholder("Tous les rôles");
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(e -> filterUsers());
        roleFilter.setWidth("200px");

        activeFilter = new ComboBox<>("Statut");
        activeFilter.setItems(true, false);
        activeFilter.setItemLabelGenerator(active -> active ? "Actif" : "Inactif");
        activeFilter.setPlaceholder("Tous les statuts");
        activeFilter.setClearButtonVisible(true);
        activeFilter.addValueChangeListener(e -> filterUsers());
        activeFilter.setWidth("150px");

        Button refreshButton = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> loadUsers());

        filtersRow.add(searchField, roleFilter, activeFilter, refreshButton);
        filtersSection.add(filtersRow);
        add(filtersSection);
    }

    private void createGridSection() {
        VerticalLayout gridSection = new VerticalLayout();
        gridSection.setSizeFull();
        gridSection.setPadding(true);
        gridSection.getStyle()
                .set("background", "#f7fafc");

        grid = new Grid<>(User.class, false);
        grid.setSizeFull();
        grid.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Name column
        grid.addColumn(User::getFullName)
                .setHeader("Nom complet")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Email column
        grid.addColumn(User::getEmail)
                .setHeader("Email")
                .setAutoWidth(true);

        // Role column with badge
        grid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span(user.getRole().getLabel());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", user.getRole().getColor())
                    .set("color", "white")
                    .set("padding", "0.5rem 1rem")
                    .set("border-radius", "12px")
                    .set("font-size", "0.875rem")
                    .set("font-weight", "600");
            return badge;
        })).setHeader("Rôle").setWidth("150px").setFlexGrow(0);

        // Phone column
        grid.addColumn(user -> user.getTelephone() != null ? user.getTelephone() : "-")
                .setHeader("Téléphone")
                .setAutoWidth(true);

        // Registration date column
        grid.addColumn(user -> user.getDateInscription().format(DATE_FORMATTER))
                .setHeader("Date d'inscription")
                .setAutoWidth(true);

        // Status column
        grid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span(user.getActif() ? "Actif" : "Inactif");
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", user.getActif() ? "#48bb78" : "#f56565")
                    .set("color", "white")
                    .set("padding", "0.5rem 1rem")
                    .set("border-radius", "12px")
                    .set("font-size", "0.875rem")
                    .set("font-weight", "600");
            return badge;
        })).setHeader("Statut").setWidth("120px").setFlexGrow(0);

        // Actions column
        grid.addColumn(new ComponentRenderer<>(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (!user.getId().equals(currentUser.getId())) {
                // Change role button
                Button roleButton = new Button(VaadinIcon.USER.create());
                roleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                roleButton.getElement().setAttribute("title", "Changer le rôle");
                roleButton.addClickListener(e -> showChangeRoleDialog(user));
                actions.add(roleButton);

                // Activate/Deactivate button
                Button statusButton = new Button(
                        user.getActif() ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create()
                );
                statusButton.addThemeVariants(
                        ButtonVariant.LUMO_SMALL,
                        user.getActif() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS
                );
                statusButton.getElement().setAttribute("title", user.getActif() ? "Désactiver" : "Activer");
                statusButton.addClickListener(e -> toggleUserStatus(user));
                actions.add(statusButton);
            } else {
                Span selfLabel = new Span("Vous");
                selfLabel.getStyle()
                        .set("color", "#718096")
                        .set("font-size", "0.875rem")
                        .set("font-style", "italic");
                actions.add(selfLabel);
            }

            return actions;
        })).setHeader("Actions").setWidth("150px").setFlexGrow(0);

        gridSection.add(grid);
        add(gridSection);
    }

    private void loadUsers() {
        try {
            allUsers = userService.getAllUsers();
            filterUsers();
        } catch (Exception e) {
            showNotification("Erreur lors du chargement des utilisateurs", NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterUsers() {
        List<User> filtered = allUsers;

        // Filter by search
        String searchTerm = searchField.getValue();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            filtered = filtered.stream()
                    .filter(u -> u.getNom().toLowerCase().contains(lowerSearch) ||
                            u.getPrenom().toLowerCase().contains(lowerSearch) ||
                            u.getEmail().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toList());
        }

        // Filter by role
        UserRole role = roleFilter.getValue();
        if (role != null) {
            filtered = filtered.stream()
                    .filter(u -> u.getRole() == role)
                    .collect(Collectors.toList());
        }

        // Filter by active status
        Boolean active = activeFilter.getValue();
        if (active != null) {
            filtered = filtered.stream()
                    .filter(u -> u.getActif().equals(active))
                    .collect(Collectors.toList());
        }

        grid.setItems(filtered);
    }

    private void showChangeRoleDialog(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Changer le rôle de " + user.getFullName());
        dialog.setCancelable(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Paragraph message = new Paragraph("Sélectionnez le nouveau rôle pour cet utilisateur :");
        message.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "#4a5568");

        ComboBox<UserRole> roleCombo = new ComboBox<>("Nouveau rôle");
        roleCombo.setItems(Arrays.asList(UserRole.values()));
        roleCombo.setItemLabelGenerator(UserRole::getLabel);
        roleCombo.setValue(user.getRole());
        roleCombo.setWidthFull();

        content.add(message, roleCombo);

        dialog.add(content);
        dialog.setCancelText("Annuler");
        dialog.setConfirmText("Changer");
        dialog.setConfirmButtonTheme("primary");

        dialog.addConfirmListener(e -> {
            UserRole newRole = roleCombo.getValue();
            if (newRole != null && !newRole.equals(user.getRole())) {
                changeUserRole(user, newRole);
            }
        });

        dialog.open();
    }

    private void changeUserRole(User user, UserRole newRole) {
        try {
            userService.changeRole(user.getId(), newRole);
            showNotification("✓ Rôle modifié avec succès", NotificationVariant.LUMO_SUCCESS);
            loadUsers();
        } catch (Exception e) {
            showNotification("❌ Erreur: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void toggleUserStatus(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(user.getActif() ? "Désactiver l'utilisateur" : "Activer l'utilisateur");
        dialog.setText("Êtes-vous sûr de vouloir " +
                (user.getActif() ? "désactiver" : "activer") + " " + user.getFullName() + " ?");
        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");
        dialog.setConfirmText(user.getActif() ? "Désactiver" : "Activer");
        dialog.setConfirmButtonTheme(user.getActif() ? "error primary" : "success primary");

        dialog.addConfirmListener(e -> {
            try {
                if (user.getActif()) {
                    userService.deactivateAccount(user.getId());
                    showNotification("✓ Utilisateur désactivé", NotificationVariant.LUMO_SUCCESS);
                } else {
                    userService.activateAccount(user.getId());
                    showNotification("✓ Utilisateur activé", NotificationVariant.LUMO_SUCCESS);
                }
                loadUsers();
            } catch (Exception ex) {
                showNotification("❌ Erreur: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}

