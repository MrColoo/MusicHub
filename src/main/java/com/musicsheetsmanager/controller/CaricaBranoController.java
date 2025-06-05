package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Documento;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.stream.Collectors;

public class CaricaBranoController {

    @FXML private TextField campoTitolo;
    @FXML private TextField campoAutori;
    @FXML private TextField campoAnnoDiComposizione;
    @FXML private TextField campoGeneri;
    @FXML private TextField campoStrumentiMusicali;
    @FXML private TextField campoLinkYoutube;
    @FXML private Text errore;
    @FXML private Button allegaFileBtn;
    //@FXML private HBox listaFileBox;


    private final List<Documento> fileAllegati = new ArrayList<>();     // salva temporaneamente i file
                                                                        // allegati finché l'utente
                                                                        // non carica il brano

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    private static final Path DOCUMENTI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "documenti.json"
    );

    public void initialize(){
        errore.setVisible(false);
    }

    // form per l'aggiunta di un nuovo brano da parte dell'utente
    @FXML
    public void onAddBranoClick() {
        // genero stringa alfanumerica casuale per ogni brano
        String idBrano = UUID.randomUUID().toString();

        salvaFileAllegati(idBrano);

        Brano nuovoBrano = creaBrano(idBrano);

        // aggiorna file json contenente i documenti
        Type documentoType = new TypeToken<List<Documento>>() {}.getType();     // documenti letti dal json
        List<Documento> listaDocumenti = JsonUtils.leggiDaJson(DOCUMENTI_JSON_PATH, documentoType);
        listaDocumenti.addAll(fileAllegati);
        JsonUtils.scriviSuJson(listaDocumenti, DOCUMENTI_JSON_PATH);

        // aggiorna file json contenente tutti i brani
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType); // brani letti dal json
        listaBrani.add(nuovoBrano);
        JsonUtils.scriviSuJson(listaBrani, BRANI_JSON_PATH);
    }

    // crea il brano e aggiorna i vari dizionari (autori, generi, titoli, strumentiMusicali)
    private Brano creaBrano(String idBrano){
        String titolo = campoTitolo.getText().trim();
        aggiornaDizionario(Collections.singletonList(titolo), "titoli");

        List<String> autori = List.of(campoAutori.getText().trim().split(",\\s*"));
        aggiornaDizionario(autori, "autori");

        // controlla che l'anno di composizione sia un numero intero valido
        int anno = 0;
        try {
            anno = Integer.parseInt(campoAnnoDiComposizione.getText().trim());
            int annoCorrente = Year.now().getValue();
            if(anno > annoCorrente) {
                errore.setText("Siamo ancora nel " + annoCorrente);
                errore.setVisible(true);
            }
        } catch (NumberFormatException e) {
            errore.setText("Inserisci un numero intero");
            errore.setVisible(true);
        }

        List<String> generi = List.of(campoGeneri.getText().toLowerCase().trim().split(",\\s*"));
        aggiornaDizionario(generi, "generi");

        String strumentiText = campoStrumentiMusicali.getText().toLowerCase().trim();
        List<String> strumentiMusicali = strumentiText.isEmpty()
                ? new ArrayList<>()
                : List.of(strumentiText.split(",\\s*"));
        aggiornaDizionario(strumentiMusicali, "strumentiMusicali");

        String linkYoutube = campoLinkYoutube.getText().trim();
        if(linkYoutube.isEmpty()) {
            linkYoutube = "";
        }

        List<String> pathAllegati = fileAllegati.stream()
                .map(Documento::getPercorso)
                .collect(Collectors.toList());

        return new Brano(idBrano, titolo, autori, generi, anno, linkYoutube, strumentiMusicali, pathAllegati);
    }

    // permette all'utente di caricare documenti inerenti al brano(es. spartiti, testi...)
    @FXML
    private void onAllegaFileClick() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(allegaFileBtn.getScene().getWindow());

        if(selectedFile != null && !fileAllegati.contains(selectedFile)){
            // TODO mettere Utente.getUsername invece di Pippo
            Documento documento = new Documento("Pippo", selectedFile.getName(), selectedFile.getAbsolutePath());

            // controllo per duplicati
            boolean exist = fileAllegati.stream()
                    .anyMatch(doc -> doc.getPercorso().equals(documento.getPercorso()));
            if(!exist) {
                fileAllegati.add(documento);
                //viewFile(documento);
            }
        }

    }

    // TODO implementare visualizzazione allegati caricati e relativa possibilità di eliminarli
    // aggiunge il file alla Hbox
    /*
    private void viewFile(Documento documento) {
        Label nomeFile = new Label(documento.getNomeFile());
        listaFileBox.getChildren().add(nomeFile);
    }
    */


    // salva i documenti nella cartella /attachments se l'utente carica il brano
    private void salvaFileAllegati(String idBrano) {

        // nella cartella attachment crea directory con l'id del brano
        Path attachmentsDirectory = Paths.get(
                "src", "main", "resources",
                "attachments", idBrano
        );
        try{
            Files.createDirectories(attachmentsDirectory);      // crea la directory se non esiste

            for (Documento documento : fileAllegati) {
                Path sourcePath = Path.of(documento.getPercorso());
                Path destinationPath = attachmentsDirectory.resolve(documento.getNomeFile());   // ottieni path completa:
                                                                                                // path cartella + nome file
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // aggiorna in percorso relativo
                documento.setPercorso(destinationPath.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // aggiorna i vari dizionari
    public void aggiornaDizionario(List<String> nuoviElementiDizionario, String tipoDizionario) {
        Path DIZIONARIO_JSON_PATH = Paths.get( // percorso verso il file JSON
                "src", "main", "resources",
                "com", "musicsheetsmanager", "data", tipoDizionario + ".json"
        );

        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> listaDizionario = JsonUtils.leggiDaJson(DIZIONARIO_JSON_PATH, listType);

        for(String element: nuoviElementiDizionario) {
            if(!listaDizionario.contains(element)){         // controllo per duplicati
                    listaDizionario.add(element);
            }
        }

        JsonUtils.scriviSuJson(listaDizionario, DIZIONARIO_JSON_PATH);
    }
}
