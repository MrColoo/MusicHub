package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Concerto;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TopBarController implements Controller{

    @FXML private Button mainButton; // "Accedi" o "Carica Brano"
    @FXML private Button caricaConcertoBtn; // Bottone per caricare caricare un concerto
    @FXML private HBox searchBar; // Box campo di ricerca
    @FXML private TextField campoRicerca;

    private EsploraController esploraController;
    private EsploraConcertiController esploraConcertiController;
    private CronologiaController cronologiaController;
    private NavBarController navBarController;

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    private static final Path CONCERTI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "concerti.json"
    );

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEsploraController(EsploraController esploraController) {
        this.esploraController = esploraController;
    }

    public void setNavBarController(NavBarController navBarController) {
        this.navBarController = navBarController;
    }

    public void setEsploraConcertiController(EsploraConcertiController esploraConcertiController) {
        this.esploraConcertiController = esploraConcertiController;
    }

    public void setCronologiaController(CronologiaController cronologiaController) {
        this.cronologiaController = cronologiaController;
    }

    public void initialize(){
        campoRicerca.setOnAction(event -> onSearchBarEnter());

        changeTopBar(); // cambia aspetto topbar nel caso l'utente sia loggato o meno
    }

    private void changeTopBar() {
        if (!SessionManager.isLoggedIn()) {
            searchBar.setVisible(false);
            searchBar.setManaged(false);
            mainButton.setText("Accedi");
            mainButton.setOnAction(event -> mainController.show("Login"));
            caricaConcertoBtn.setVisible(false);
            caricaConcertoBtn.setManaged(false);
        }else{
            searchBar.setVisible(true);
            searchBar.setManaged(true);
            mainButton.setText("Carica Brano");
            mainButton.setOnAction(event -> mainController.show("CaricaBrano"));
            caricaConcertoBtn.setVisible(true);
            caricaConcertoBtn.setManaged(true);
            caricaConcertoBtn.setOnAction(event -> {
                mainController.show("CaricaConcerto");
            });
        }
    }

    /**
     * Mostra i brani/cataloghi trovati inserendo una determinata chiave
     */
    @FXML
    private void onSearchBarEnter() {
        String pagina = navBarController.getCurrentPage();
        String chiave = campoRicerca.getText();

        // Toggle della navbar
        switch (pagina) {
            case "esploraBtn" -> gestisciRicercaEsplora(chiave);
            case "concertiBtn" -> gestisciRicercaConcerti(chiave);
            case "cronologiaBtn" -> gestisciRicercaCronologia(chiave);
        }
    }

    /**
     * Mostra i brani/cataloghi trovati inserendo una determinata chiave
     *
     * @param chiave Chiave di ricerca inserita
     */
    private void gestisciRicercaEsplora(String chiave) {
        String viewType = esploraController.getViewType();

        Path dizionarioPath = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "data", viewType + ".json");
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        // controlla il tipo di toggle attivato
        switch (viewType) {
            case "esplora" -> {
                List<Brano> risultati = Brano.cercaBrano(listaBrani, chiave);
                esploraController.generaCatalogo(risultati, brano -> esploraController.creaCardBrano(brano));
            }
            case "generi" -> {
                List<String> dizionario = leggiDizionario(dizionarioPath);
                List<String> risultati = cercaCatalogo(dizionario, chiave);
                esploraController.generaCatalogo(risultati, genere -> esploraController.creaCardGenere(listaBrani, genere));
            }
            default -> {
                List<String> dizionario = leggiDizionario(dizionarioPath);
                List<String> risultati = cercaCatalogo(dizionario, chiave);
                esploraController.generaCatalogo(risultati, esploraController::creaCardCatalogo);
            }
        }
    }

    /**
     * Risultati ricerca degli elementi di un dizionario
     *
     * @param dizionario Dizionario di cui si vuole fare la ricerca
     * @param chiave Chiave di ricerca inserita
     */
    private static List<String> cercaCatalogo (List<String> dizionario, String chiave){
        if(chiave == null || chiave.isBlank()) return dizionario;

        String key = chiave.toLowerCase();
        return dizionario.stream()
                .filter(b -> b.contains(key))
                .collect(Collectors.toList());
    }

    /**
     * Gestisce ricerca concerti tramite chiave
     *
     * @param chiave Chiave di ricerca inserita
     */
    private void gestisciRicercaConcerti(String chiave) {
        Type concertoType = new TypeToken<List<Concerto>>() {}.getType();
        List<Concerto> listaConcerti = JsonUtils.leggiDaJson(CONCERTI_JSON_PATH, concertoType);
        List<Concerto> risultati = Concerto.cercaConcerti(listaConcerti, chiave);

        // aspetta che il controller venga caricato
        if (esploraConcertiController != null) {
            esploraConcertiController.mostraCardConcerti(risultati);
        }
    }

    /**
     * Gestisce ricerca brani commentati dall'utente
     *
     * @param chiave Chiave di ricerca inserita
     */
    private void gestisciRicercaCronologia(String chiave) {
        List<Brano> lista = cronologiaController.getBraniCommentati();
        List<Brano> risultati = Brano.cercaBrano(lista, chiave);
        cronologiaController.generaCatalogo(risultati, brano -> cronologiaController.creaCardBrano(brano));
    }

    /**
     * Funzione che restituisce gli elementi letti in un dizionario
     *
     * @param path Percorso del dizionario
     */
    private List<String> leggiDizionario(Path path) {
        Type stringType = new TypeToken<List<String>>() {}.getType();
        return JsonUtils.leggiDaJson(path, stringType);
    }
}

