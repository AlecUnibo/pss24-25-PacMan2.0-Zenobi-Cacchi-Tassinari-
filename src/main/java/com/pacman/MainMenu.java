package com.pacman;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Classe MainMenu - gestisce il menu principale del gioco Pac-Man.
 * Mostra i pulsanti START, ISTRUZIONI e ARMADIO SKIN.
 */
public class MainMenu {

    private final Stage primaryStage; // Finestra principale dell'applicazione
    private final StackPane root;     // Radice della scena
    private final Scene scene;        // Scena da mostrare
    private final Font menuFont;      // Font personalizzato per il menu

    public MainMenu(Stage stage) {
        this.primaryStage = stage;
        this.root = new StackPane();
        this.scene = new Scene(root, PacMan.BOARD_WIDTH, PacMan.BOARD_HEIGHT + PacMan.TILE_SIZE);
        this.menuFont = loadMenuFont(); // Carica il font personalizzato
        buildMenu();                    // Costruisce il menu
    }

    /**
     * Carica il font "PressStart2P" dalla cartella delle risorse.
     * Se fallisce, ritorna il font di default.
     */
    private Font loadMenuFont() {
        URL fontUrl = getClass().getResource("/assets/fonts/PressStart2P.ttf");
        if (fontUrl == null) return Font.getDefault();
        Font loaded = Font.loadFont(fontUrl.toExternalForm(), 14);
        return (loaded != null ? loaded : Font.getDefault());
    }

    /**
     * Costruisce graficamente il menu principale con pulsanti e stili.
     */
    private void buildMenu() {
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setStyle("-fx-background-color: black;");

        // Crea i pulsanti/etichette principali
        Button startButton    = new Button("START");
        Label instructions    = new Label("ISTRUZIONI");
        Label skinCloset      = new Label("ARMADIO SKIN");

        // Applica font personalizzati
        startButton.setFont(menuFont);
        instructions.setFont(menuFont);
        skinCloset.setFont(menuFont);

        // Stili colori e sfondi
        startButton.setTextFill(Color.YELLOW);
        instructions.setTextFill(Color.WHITE);
        skinCloset.setTextFill(Color.WHITE);

        startButton.setStyle("-fx-background-color: transparent;");
        instructions.setStyle("-fx-cursor: hand;");
        skinCloset.setStyle("-fx-cursor: hand;");

        // Azione del pulsante START: avvia il gioco
        startButton.setOnAction(e -> launchGame());

        // Azione del pulsante ISTRUZIONI: mostra istruzioni di gioco
        instructions.setOnMouseClicked((MouseEvent e) -> {
            VBox instructionsBox = new VBox(20);
            instructionsBox.setAlignment(Pos.CENTER);
            instructionsBox.setStyle("-fx-background-color: black;");

            Label title = new Label("ISTRUZIONI");
            Label info  = new Label("""
                    • Usa le FRECCE per muoverti 
                    • Mangia i puntini per fare punti 
                    • Evita i fantasmi 
                    • Raccogli la frutta per punti extra 
                    """);
            Button back  = new Button("INDIETRO");

            // Applica font e colori anche nella schermata istruzioni
            title.setFont(menuFont);
            info.setFont(menuFont);
            back.setFont(menuFont);

            title.setTextFill(Color.YELLOW);
            info.setTextFill(Color.WHITE);
            back.setTextFill(Color.YELLOW);
            back.setStyle("-fx-background-color: transparent;");

            // Torna al menu principale
            back.setOnAction(ev -> root.getChildren().setAll(menuBox));
            instructionsBox.getChildren().setAll(title, info, back);
            root.getChildren().setAll(instructionsBox);
        });

        // Azione placeholder per il pulsante ARMADIO SKIN
        skinCloset.setOnMouseClicked(e -> System.out.println("ARMADIO SKIN: da implementare"));

        // Aggiunge gli elementi al menu
        menuBox.getChildren().addAll(startButton, instructions, skinCloset);
        root.getChildren().add(menuBox);
    }

    /**
     * Avvia una nuova partita creando un'istanza di PacMan e mostrando il gioco.
     */
    private void launchGame() {
        PacMan pacmanGame = new PacMan(this);
        root.getChildren().setAll(pacmanGame);
        pacmanGame.requestFocus(); // permette di ricevere input da tastiera
    }

    /**
     * Mostra la scena del menu principale.
     */
    public void show() {
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Metodo richiamato da PacMan al termine della partita per ritornare al menu.
     */
    public void returnToMenu() {
        buildMenu(); // Ricostruisce il menu (utile se il contenuto è dinamico)
        show();
    }
}
