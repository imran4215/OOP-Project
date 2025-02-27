package com;

import java.util.prefs.Preferences;

public class Auth {
    // Why use Preferences instead of a static variable?
    // Preferences is a built-in Java class that allows you to store user preferences in txt files.
    // The username is stored in a file (auth_data.txt) after login.
    // When the user logs in, their username is written to the file.
    // When the app starts again, it reads the stored username from the file.
    private static final Preferences prefs = Preferences.userNodeForPackage(Auth.class);

    public static boolean isAuthenticated() {
        return prefs.getBoolean("isLoggedIn", false);
    }

    public static String getUsername() {
        return prefs.get("username", "");
    }

    public static void login(String user) {
        prefs.putBoolean("isLoggedIn", true);
        prefs.put("username", user);
    }

    public static void logout() {
        prefs.putBoolean("isLoggedIn", false);
        prefs.remove("username");  // Remove the username from the preferences after logout
    }
}
