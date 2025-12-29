# 🌙 Mode Sombre/Clair - Quick Reference

## What Was Added?

A **modern theme toggle button** in the application header that allows users to switch between light and dark modes with a single click.

## Visual Overview

```
┌─────────────────────────────────────────────────────────────┐
│ ☰  🎫 EventPro                    🌙 Login  Register      │  ← Light Mode
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ ☰  🎫 EventPro                    ☀️  Login  Register       │  ← Dark Mode (toggled)
└─────────────────────────────────────────────────────────────┘

For authenticated users:
┌─────────────────────────────────────────────────────────────┐
│ ☰  🎫 EventPro      👤 John Doe  🌙  Déconnexion          │  ← Light Mode
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ ☰  🎫 EventPro      👤 John Doe  ☀️   Déconnexion          │  ← Dark Mode
└─────────────────────────────────────────────────────────────┘
```

## Light Mode vs Dark Mode

### Light Mode (Default)

```
Background: White (#ffffff)
Text: Dark gray (#333333)
Accents: Purple gradient (#667eea → #764ba2)
Cards: Light gray backgrounds
```

### Dark Mode

```
Background: Dark gray (#1a1a1a)
Text: Light gray (#e0e0e0)
Accents: Purple gradient (maintained)
Cards: Darker gray (#2a2a2a)
Inputs: Very dark (#333333)
```

## How to Use

1. **Click the Theme Button**

   - Located in top right of header
   - Shows moon icon (🌙) in light mode
   - Shows sun icon (☀️) in dark mode

2. **Theme Switches Instantly**

   - No page reload required
   - Smooth color transitions
   - All UI elements update automatically

3. **Your Preference is Saved**
   - Browser remembers your choice
   - Same theme on next visit
   - Works across all pages

## Color Palettes

### Dark Mode Colors

| Element          | Color     | Usage                   |
| ---------------- | --------- | ----------------------- |
| Base Background  | `#1a1a1a` | Main page background    |
| Card Background  | `#2a2a2a` | Cards, containers       |
| Text Primary     | `#e0e0e0` | Main text content       |
| Text Secondary   | `#a0a0a0` | Secondary text          |
| Border           | `#404040` | Input borders, dividers |
| Input Background | `#333333` | Text fields, dropdowns  |
| Primary Color    | `#667eea` | Buttons, highlights     |

### Light Mode Colors

- Uses Vaadin Lumo default light theme
- High contrast for readability
- Clean, professional appearance

## Features

✅ **Smart Detection**

- Checks system dark mode preference on first visit
- Respects user's operating system settings

✅ **Persistent Storage**

- Saved in browser localStorage
- Remembers choice across sessions
- No backend required

✅ **Smooth Transitions**

- 0.3s ease transitions
- No jarring color changes
- Professional appearance

✅ **Full Coverage**

- All text fields styled
- All buttons styled
- All components supported
- Grids, dialogs, notifications all themed

## Technical Details

### Files Involved

1. **ThemeManager.java**

   - Service class managing theme state
   - Uses JavaScript to interact with DOM
   - Spring @Service component

2. **MainLayout.java**

   - Contains theme toggle button
   - Initializes theme on app startup
   - Injects ThemeManager

3. **dark-mode.css**
   - Complete dark theme stylesheet
   - CSS custom property overrides
   - Responsive design support

### Key Methods

```
ThemeManager.initializeTheme()    → Initialize theme on startup
ThemeManager.toggleTheme()        → Switch between light/dark
ThemeManager.setTheme(theme)      → Set specific theme
ThemeManager.getCurrentTheme()    → Get current theme
```

## Browser Storage

The theme preference is stored in `localStorage`:

```javascript
Key:   'app_theme'
Value: 'light' or 'dark'
```

You can check it in browser DevTools → Application → Local Storage

## Customization Guide

### Change Button Position

Edit `MainLayout.java`:

```java
// Move themeToggle button in HorizontalLayout
HorizontalLayout userLayout = new HorizontalLayout(avatar, themeToggle, userInfo, logoutButton);
```

### Change Colors

Edit `dark-mode.css`:

```css
html[theme~="dark"] {
  --lumo-base-color: #your-color;
  /* Update other colors */
}
```

### Change Icons

Edit `createThemeToggleButton()` in `MainLayout.java`:

```java
Icon themeIcon = new Icon(VaadinIcon.YOUR_ICON);
```

Available icons:

- `VaadinIcon.MOON` - Moon
- `VaadinIcon.CLOUD_O` - Sun (currently used)
- `VaadinIcon.STAR` - Star
- And many more...

## Testing

To test the theme toggle:

1. Start the application
2. Click the theme button in the header
3. Verify colors change smoothly
4. Refresh the page
5. Confirm the theme persists
6. Try on different pages (dashboard, events, etc.)
7. All components should be themed consistently

## Troubleshooting

**Theme not changing?**

- Clear browser localStorage: `localStorage.clear()`
- Check browser console for JS errors
- Ensure JavaScript is enabled

**Colors not right?**

- Check `dark-mode.css` is loaded
- Verify CSS custom properties are applied
- Check browser DevTools styles

**Icons not showing?**

- Verify Vaadin icon is imported correctly
- Check Icon component initialization
- Look for console errors

---

**💡 Pro Tips:**

- Use DevTools to inspect theme attribute: `<html theme="dark">`
- Can manually set theme in console: `localStorage.setItem('app_theme', 'dark')`
- Works great for accessibility and eye comfort during night usage
