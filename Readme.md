# Relazione finale PacMan 2.0 | Corso Progettazione e Sviluppo del Software - A.A. 2024/2025 | Laurea in Tecnologie dei Sistemi Informatici - UNIBO

## ANALISI 

### Analisi dei Requisiti

L'applicazione PacMan 2.0 dovrà soddisfare i seguenti requisiti funzionali:

* Il giocatore deve poter controllare il personaggio principale (PacMan) all'interno di un labirinto.

* Il labirinto deve contenere dei "dots" (cibo) che PacMan può raccogliere per accumulare punti.

* Devono essere presenti nemici (i fantasmi), che si muovono nel labirinto secondo un comportamento definito e non.

* Se un fantasma mangia PacMan, il giocatore perde una vita.

* Il gioco termina quando:

   * Il giocatore perde tutte le vite.

   * Il giocatore raccoglie tutte le pillole presenti nel labirinto.

* Devono essere previste condizioni di vittoria e di sconfitta.

* Il punteggio del giocatore deve essere aggiornato in tempo reale.

* Devono essere presenti elementi di interazione dinamica:
    
    * dots speciali che modificano temporaneamente il comportamento dei fantasmi.

* Il gioco deve essere rigiocabile, ovvero il giocatore può iniziare una nuova partita una volta terminata quella corrente.

Eventuali requisiti aggiuntivi rispetto al gioco originale (PacMan 2.0):

* Devono essere introdotti "poteri" speciali per PacMan

* Aggiungere una sezione "skin" al menù iniziale.

### Analisi del Problema

Il dominio applicativo è quello dei videogiochi di tipo arcade a scorrimento labirintico, in cui un giocatore interagisce con un ambiente chiuso e pieno di ostacoli, con lo scopo di completare un obiettivo (raccogliere tutti pallini) evitando minacce (i fantasmi). Il gioco si fonda su una logica a turni continui (real-time), che prevede il costante aggiornamento dello stato del gioco in risposta alle azioni del giocatore e al comportamento degli elementi autonomi (i nemici).

Il problema da affrontare è quello di modellare un ambiente interattivo in cui:

* Il comportamento dei nemici presenti sia sufficientemente vario e sfidante da offrire un'esperienza di gioco coinvolgente.

* Il percorso del giocatore sia influenzato dalla disposizione degli oggetti nel labirinto.

* Sia garantita una progressione di difficoltà attraverso l’introduzione di elementi variabili o aggiuntivi.

* Si gestiscano in modo corretto le condizioni di gioco (inizio, pausa, fine, punteggio, numero di vite).

**Entità principali:**

* PacMan (giocatore): entità principale controllata dall’utente. Si muove liberamente nel labirinto e interagisce con gli oggetti presenti.

* Labirinto: ambiente statico che rappresenta lo scenario di gioco. È delimitato da muri, con passaggi, corridoi e intersezioni. Contiene oggetti raccoglibili e nemici.

* Dots: oggetto raccoglibile che incrementa il punteggio. Esistono dots normali (food) e speciali (powerfood).

* Fantasma (nemico): entità autonoma che si muove nel labirinto cercando di intercettare il giocatore. In condizioni normali, il contatto tra un fantasma e PacMan causa la perdita di una vita. In presenza di un powerfood, i fantasmi diventano vulnerabili.

* Punteggio: rappresenta il risultato del giocatore ed è incrementato dalla raccolta degli oggetti.

* Vita: il giocatore dispone di un numero limitato di tentativi (vite); ogni contatto con un fantasma in stato normale comporta la perdita di una vita.

**Relazioni tra entità:**

* PacMan si muove nel Labirinto, raccoglie il food e può essere colpito dai Fantasmi.

* Fantasmi si muovono autonomamente nel Labirinto.

* Il food è posizionato nel Labirinto e scompare una volta raccolto, oppure dopo 8 secondo se non si è ancora mangiato.

* La raccolta di un powerfood modifica temporaneamente il comportamento dei Fantasmi. 

```mermaid
classDiagram
    class PacMan {
        +posizione
        +viteResidue
    }

    class Fantasma {
        +posizione
        +stato (normale/vulnerabile)
    }

    class Food {
        +valore
    }

    class PowerFood {
        +effetto (vulnerabilità nemici)
    }

    class Labirinto {
        +struttura
        +oggettiPresenti
    }

    class Livello {
        +numero
        +difficoltà
    }

    class Vita {
        +conteggio
    }

    class Punteggio {
        +valoreCorrente
    }

    PowerFood --|> Food : estensione
    PacMan --> Food : raccoglie
    PacMan --> PowerFood : raccoglie
    PacMan --> Fantasma : interazione
    PacMan --> Labirinto : si muove in
    Fantasma --> Labirinto : si muove in
    Labirinto --> Livello : fa parte di
    Livello --> Vita : gestisce
    Livello --> Punteggio : aggiorna
 ```

## DESIGN
In questo capitolo vengono illustrate le strategie adottate per soddisfare i requisiti emersi durante la fase di analisi, con 
un'attenzione particolare alla struttura architetturale e alle scelte progettuali più rilevanti.

L'applicazione PacMan 2.0 è stata sviluppata adottando un’architettura modulare, pensata per garantire chiarezza nella distribuzione 
delle responsabilità tra le diverse componenti. Il modello architetturale di riferimento si ispira al pattern MVC (Model-View-Controller),
adattato al contesto specifico del progetto.

Il Model ha il compito di gestire lo stato interno del gioco: la posizione dei personaggi, la configurazione della mappa, il punteggio 
accumulato e le vite rimanenti. La View, resa possibile attraverso l’uso di JavaFX, si occupa della rappresentazione visiva 
dell’interfaccia di gioco, inclusi i personaggi, gli oggetti e gli effetti animati.

Questa struttura ha permesso uno sviluppo ordinato e scalabile dell’applicazione, rendendo più agevole l’implementazione e la 
manutenzione di funzionalità specifiche come la gestione dei fantasmi, dei frutti bonus o degli elementi raccolti lungo il percorso.

### Architettura dei componenti

Il punto d’ingresso dell’applicazione è rappresentato dalla classe App.java, responsabile dell’inizializzazione dell’ambiente JavaFX. 
Questa classe mostra il menu principale, da cui è possibile avviare una partita o uscire dal gioco, e si occupa di avviare la schermata 
di gioco vera e propria.

Al centro della logica di gioco si trova PacMan.java, che gestisce il ciclo di vita della partita. Qui si coordinano l’avanzamento 
temporale, il movimento dei personaggi, il rilevamento delle collisioni e l’aggiornamento dello stato del gioco (vite, punteggio, 
oggetti raccolti). Questa classe funge da nucleo attorno al quale ruotano tutte le altre componenti.