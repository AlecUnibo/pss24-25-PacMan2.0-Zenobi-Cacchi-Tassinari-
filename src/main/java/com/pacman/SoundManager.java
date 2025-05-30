package com.pacman;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe di utilità per la gestione dei suoni nel gioco PacMan.
 * Permette di caricare, riprodurre, mettere in loop, silenziare e controllare il volume dei suoni.
 */
public class SoundManager {

    // Mappa dei suoni caricati: nome logico → Clip audio
    private static final Map<String, Clip> soundClips = new HashMap<>();
    // Mappa dei controlli di muting per i suoni
    private static final Map<String, BooleanControl> muteControls = new HashMap<>();
    // Mappa dei controlli di volume per i suoni
    private static final Map<String, FloatControl> volumeControls = new HashMap<>();

    /**
     * Carica un file audio e lo associa a un nome identificativo.
     * @param name nome logico con cui si vuole identificare il suono
     * @param resourcePath percorso nella cartella delle risorse
     */
    public static void loadSound(String name, String resourcePath) {
        try {
            URL soundURL = SoundManager.class.getClassLoader().getResource(resourcePath);
            if (soundURL == null) {
                System.err.println("Impossibile trovare il file audio: " + resourcePath);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);

            // Salva il controllo MUTE se disponibile
            if (clip.isControlSupported(BooleanControl.Type.MUTE)) {
                BooleanControl bc = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
                muteControls.put(name, bc);
                bc.setValue(false); // non mutato all'inizio
            }

            // Salva il controllo VOLUME se disponibile
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControls.put(name, fc);
                fc.setValue(0.0f); // volume a livello normale (0 dB)
            }

            soundClips.put(name, clip); // Salva il clip nella mappa
            System.out.println("Suono caricato con successo: " + name);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Errore nel caricamento del suono: " + resourcePath);
            e.printStackTrace();
        }
    }

     /* Riproduce una singola istanza del suono indicato.*/
    public static void playSound(String name) {
        Clip clip = soundClips.get(name);
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

     /*Mette in muto tutti i suoni, usando BooleanControl se disponibile o volume a minimo.*/
    public static void muteAll() {
        // Boolean mute
        for (BooleanControl bc : muteControls.values()) {
            bc.setValue(true);
        }
        // fallback volume
        for (FloatControl fc : volumeControls.values()) {
            fc.setValue(fc.getMinimum());
        }
    }

    /**
     * Riattiva l'audio per tutti i suoni, usando mute o ripristinando il volume a 0 dB.
     */
    public static void unmuteAll() {
        for (BooleanControl bc : muteControls.values()) {
            bc.setValue(false); // Disattiva mute
        }
        for (FloatControl fc : volumeControls.values()) {
            fc.setValue(0.0f); // Volume normale
        }
    }

    /**
     * Riproduce il suono in loop continuo finché non viene fermato.
     * @param name nome logico del suono
     */
    public static void loopSound(String name) {
        Clip clip = soundClips.get(name);
        if (clip == null) return;
        if (!clip.isRunning()) {
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Avvia loop infinito
        }
    }

    /**
     * Ferma il suono specificato se attualmente in esecuzione.
     * @param name nome logico del suono
     */
    public static void stopSound(String name) {
        Clip clip = soundClips.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    /**
     * Restituisce il Clip audio associato a un nome, utile per controlli avanzati.
     * @param name nome logico del suono
     * @return oggetto Clip o null se non esiste
     */
    public static Clip getClip(String name) {
        return soundClips.get(name);
    }

}
