package com.amalitech.smartecommerce;

import com.amalitech.smartecommerce.app.Main;

/**
 * Launcher class that doesn't extend Application.
 * This bypasses the JavaFX module system check when running from IDE.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
