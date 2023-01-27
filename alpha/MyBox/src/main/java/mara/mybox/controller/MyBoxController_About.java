package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.fxml.HelpTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_About extends MyBoxController_Settings {

    @FXML
    protected void showAboutMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem About = new MenuItem(Languages.message("About"));
        About.setOnAction((ActionEvent event1) -> {
            HelpTools.about();
        });

        MenuItem FunctionsList = new MenuItem(Languages.message("FunctionsList"));
        FunctionsList.setOnAction((ActionEvent event1) -> {
            openStage(Fxmls.FunctionsListFxml);
        });

        MenuItem Shortcuts = new MenuItem(Languages.message("Shortcuts"));
        Shortcuts.setOnAction((ActionEvent event1) -> {
            openStage(Fxmls.ShortcutsFxml);
        });

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                About, Shortcuts, FunctionsList
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(Languages.message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(aboutBox, event);

    }

}
