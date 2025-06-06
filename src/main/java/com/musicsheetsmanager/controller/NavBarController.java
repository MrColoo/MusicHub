package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.config.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;

import static com.musicsheetsmanager.config.SessionManager.logout;

public class NavBarController implements Controller{

    @FXML private ToggleButton esploraBtn;
    @FXML private ToggleButton adminBtn;

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        esploraBtn.setSelected(true);

        changeNavBar(); // cambia aspetto navbar nel caso l'utente sia admin o meno

    }

    private void changeNavBar() {
        if (!SessionManager.getLoggedUser().isAdmin()) { // da cambiare e mettere admin
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
                mainController.show("Concerti");
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
                logout(); // imposta a null l'utente loggato in SessionManager
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

