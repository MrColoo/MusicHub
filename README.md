# 🎵 MusicHub

**MusicHub** è un'applicazione desktop per la gestione collaborativa di brani musicali e concerti. Il progetto è stato realizzato come lavoro finale del corso di **Ingegneria del Software 2025** presso l'Università degli Studi di Verona.

![Interfaccia MusicHub](https://github.com/user-attachments/assets/2895ca27-cfb7-4d4b-8267-b0052de51c97)
> *Interfaccia principale per l’esplorazione dei brani*

![Interfaccia MusicHub](https://github.com/user-attachments/assets/76fc99e5-efa9-4b3e-a2ab-93b6537dcf92)
> *Interfaccia di caricamento brani*

![Interfaccia MusicHub](https://github.com/user-attachments/assets/38bc03a1-72fc-433c-bcae-2cba8513ec89)
> *Interfaccia principale per l’esplorazione dei generi musicali.*

## 📽️ Demo

Guarda il video dimostrativo dell'applicazione:  
👉 [Video Demo](https://github.com/user-attachments/assets/acce29eb-a1ae-48e4-88ba-52a09466dd46)

---

## 🧠 Funzionalità principali

- **Registrazione e Login**
  - Gestione account utente con approvazione da parte dell'amministratore.
- **Gestione Brani Musicali**
  - Inserimento brani con autocompletamento dei metadati via API (Spotify/iTunes).
  - Upload di spartiti, testi, audio e video.
- **Gestione Concerti**
  - Inserimento concerti tramite link YouTube.
  - Segmentazione dei concerti per indicare i brani eseguiti nei vari timestamp.
- **Commenti e Annotazioni**
  - Possibilità di aggiungere e rispondere a commenti o note su brani e file multimediali.
- **Ricerca e Filtri**
  - Esplorazione dei contenuti con filtro per autore, genere, esecutore.
- **Amministrazione**
  - Moderazione utenti e commenti da parte dell’admin.
- **Tracciamento Cronologia**
  - Cronologia delle interazioni dell’utente con il sistema.

---

## ⚙️ Architettura del Sistema

Il progetto è sviluppato in **Java** utilizzando **JavaFX** per l'interfaccia grafica e segue il pattern architetturale **MVC (Model-View-Controller)**.

- `Model`: gestione dei dati (utenti, brani, concerti, commenti, documenti).
- `View`: interfaccia utente JavaFX.
- `Controller`: logica applicativa e interazione tra model e view.

---

## 📂 Struttura del Progetto

