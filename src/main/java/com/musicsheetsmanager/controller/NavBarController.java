package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.config.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import static com.musicsheetsmanager.config.SessionManager.logout;

public class NavBarController implements Controller{

    @FXML private ToggleButton esploraBtn;
    @FXML private ToggleButton adminBtn;

    private MainController mainController;

    @FXML
    private ToggleGroup toggleGroup;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        esploraBtn.setSelected(true);

        changeNavBar(); // cambia aspetto navbar nel caso l'utente sia admin o meno

    }

    /**
     *  Ottiene la pagina corrente dal toggle della NavBar
     *
     * @return Nome pagina corrente (in stringa)
     */
    public String getCurrentPage () {
        if(toggleGroup.getSelectedToggle() instanceof ToggleButton selectedBtn) {
            return selectedBtn.getId();
        }
        return null;
    }

    private void changeNavBar() {
        if (!SessionManager.getLoggedUser().isAdmin()) {
            adminBtn.setVisible(false);
        }else{
            adminBtn.setVisible(true);
        }
    }

    @FXML private void changeView(javafx.event.ActionEvent event){
        ToggleButton btn = (ToggleButton) event.getSource();

        // Determina quale FXML caricare in base al pulsante
        switch (btn.getId()) {
            case "esploraBtn":
                alreadySelected(btn);
                mainController.show("Esplora");
                break;
            case "concertiBtn":
                alreadySelected(btn);
                mainController.show("EsploraConcerti");
                break;
            case "cronologiaBtn":
                alreadySelected(btn);
                mainController.show("Cronologia");
                break;
            case "adminBtn":
                alreadySelected(btn);
                mainController.show("Admin");
                break;
            case "logoutBtn":
                alreadySelected(btn);
                mainController.hideNavBar();
                logout(); // Imposta a null l'utente loggato in SessionManager
                mainController.reloadTopBar();
                mainController.show("Login");
                break;
            default:
                break;
        }
    }

    private void alreadySelected(ToggleButton btn){
        if(!btn.isSelected()) btn.setSelected(true);
    }
}

