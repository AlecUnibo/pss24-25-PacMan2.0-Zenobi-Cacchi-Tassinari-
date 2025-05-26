package com.pacman;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Classe principale dell'applicazione JavaFX per Pac-Man.
 * Avvia l'interfaccia grafica mostrando il menu iniziale.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Pac-Man");

        // Uscita completa all'uscita della finestra
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });

        // Mostra il menu principale
        MainMenu menu = new MainMenu(primaryStage);
        menu.show();
    }

    // Metodo di avvio dell'applicazione
    public static void main(String[] args) {
        launch(args);
    }
}
