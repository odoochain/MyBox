package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class BaseFileEditorController_Pair extends BaseFileEditorController_Base {

    @Override
    public void checkRightPane() {
        super.checkRightPane();
        if (isSettingValues || splitPane == null || rightPane == null
                || rightPaneCheck == null || rightPaneControl == null) {
            return;
        }
        if (rightPaneCheck.isSelected()) {
            refreshPairAction();
        }
    }

    @FXML
    @Override
    public void controlRightPane() {
        if (splitPane == null || rightPane == null || rightPaneControl == null) {
            return;
        }
        super.controlRightPane();
        refreshPairAction();
    }

    protected void initPairBox() {
        try {
            if (pairArea == null) {
                return;
            }
            pairArea.setStyle("-fx-highlight-fill: black; -fx-highlight-text-fill: palegreen;");
            pairArea.setEditable(false);
            pairArea.scrollTopProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (UserConfig.getBoolean(baseName + "ScrollSynchronously", false)) {
                        isSettingValues = true;
                        mainArea.setScrollTop(newValue.doubleValue());
                        isSettingValues = false;
                    }
                }
            });
            pairArea.scrollLeftProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (UserConfig.getBoolean(baseName + "ScrollSynchronously", false)) {
                        isSettingValues = true;
                        mainArea.setScrollLeft(newValue.doubleValue());
                        isSettingValues = false;
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void updatePairArea() {
        if (isSettingValues || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        if (UserConfig.getBoolean(baseName + "UpdateSynchronously", false)
                || (pairArea != null && pairArea.getText().isEmpty())) {
            refreshPairAction();
        }
    }

    @FXML
    public void refreshPairAction() {
    }

    protected void setPairAreaSelection() {

    }

    protected void clearPairArea() {
        if (pairArea == null) {
            return;
        }
        pairArea.clear();
    }

    protected void scrollTopPairArea(double value) {
        if (isSettingValues || pairArea == null
                || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        isSettingValues = true;
        pairArea.setScrollTop(value);
        isSettingValues = false;
    }

    protected void scrollLeftPairArea(double value) {
        if (isSettingValues || pairArea == null
                || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        isSettingValues = true;
        pairArea.setScrollLeft(value);
        isSettingValues = false;
    }

    @FXML
    public void popPanesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            CheckMenuItem updateMenu = new CheckMenuItem(message("UpdateSynchronously"));
            updateMenu.setSelected(UserConfig.getBoolean(baseName + "UpdateSynchronously", false));
            updateMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "UpdateSynchronously", updateMenu.isSelected());
                    if (updateMenu.isSelected()) {
                        updatePairArea();
                    }
                }
            });
            popMenu.getItems().add(updateMenu);

            CheckMenuItem scrollMenu = new CheckMenuItem(message("ScrollSynchronously"));
            scrollMenu.setSelected(UserConfig.getBoolean(baseName + "ScrollSynchronously", false));
            scrollMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ScrollSynchronously", scrollMenu.isSelected());
                    if (scrollMenu.isSelected()) {
                        pairArea.setScrollLeft(mainArea.getScrollLeft());
                        pairArea.setScrollTop(mainArea.getScrollTop());
                    }
                }
            });
            popMenu.getItems().add(scrollMenu);

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
