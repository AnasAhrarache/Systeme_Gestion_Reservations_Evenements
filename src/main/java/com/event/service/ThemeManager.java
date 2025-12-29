package com.event.service;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

@Service
public class ThemeManager {

    private static final String THEME_KEY = "app_theme";

    /**
     * Initialize theme from session storage or system preference
     */
    public void initializeTheme() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().executeJs(
                "if (!localStorage.getItem('" + THEME_KEY + "')) {" +
                "  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;" +
                "  localStorage.setItem('" + THEME_KEY + "', prefersDark ? 'dark' : 'light');" +
                "}" +
                "const theme = localStorage.getItem('" + THEME_KEY + "');" +
                "if (theme === 'dark') {" +
                "  document.documentElement.setAttribute('theme', 'dark');" +
                "}"
            );
        }
    }

    /**
     * Toggle between dark and light themes
     */
    public void toggleTheme() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().executeJs(
                "const currentTheme = localStorage.getItem('" + THEME_KEY + "') || 'light';" +
                "const newTheme = currentTheme === 'dark' ? 'light' : 'dark';" +
                "localStorage.setItem('" + THEME_KEY + "', newTheme);" +
                "if (newTheme === 'dark') {" +
                "  document.documentElement.setAttribute('theme', 'dark');" +
                "} else {" +
                "  document.documentElement.removeAttribute('theme');" +
                "}" +
                "return newTheme;"
            );
        }
    }

    /**
     * Get current theme preference
     */
    public String getCurrentTheme() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().executeJs(
                "return localStorage.getItem('" + THEME_KEY + "') || 'light';"
            );
        }
        return "light";
    }

    /**
     * Set theme explicitly
     */
    public void setTheme(String theme) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            if ("dark".equals(theme)) {
                ui.getPage().executeJs(
                    "localStorage.setItem('" + THEME_KEY + "', 'dark');" +
                    "document.documentElement.setAttribute('theme', 'dark');"
                );
            } else {
                ui.getPage().executeJs(
                    "localStorage.setItem('" + THEME_KEY + "', 'light');" +
                    "document.documentElement.removeAttribute('theme');"
                );
            }
        }
    }
}
