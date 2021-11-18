package mara.mybox.controller;

import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.Data2D;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2D extends BaseController {

    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable tableController;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editTab, viewTab, attributesTab, columnsTab;
    @FXML
    protected ControlData2DEdit editController;
    @FXML
    protected ControlData2DView viewController;
    @FXML
    protected ControlData2DAttributes attributesController;
    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableController = editController.tableController;

            tableData2DDefinition = new TableData2DDefinition();
            tableData2DColumn = new TableData2DColumn();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // parent should call this before initControls()
    public void setDataType(BaseController parent, Data2D.Type type) {
        try {
            parentController = parent;

            data2D = Data2D.create(type);
            data2D.setTableData2DDefinition(tableData2DDefinition);
            data2D.setTableData2DColumn(tableData2DColumn);
            data2D.setTableController(tableController);
            data2D.setTableData(tableController.tableData);

            editController.setParameters(this);
            viewController.setParameters(this);
            attributesController.setParameters(this);
            columnsController.setParameters(this);

            data2D.getPageLoadedNotify().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        viewController.loadData();
                    });

            data2D.getTableChangedNotify().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        editTab.setText(message("Edit") + (data2D.isTableChanged() ? "*" : ""));
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData() {
        tableController.loadData();
        attributesController.loadData();
        columnsController.loadTableData();
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            data2D.resetData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        paigination
     */
    @FXML
    public void goPage() {
        tableController.goPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        tableController.pageNextAction();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        tableController.pagePreviousAction();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        tableController.pageFirstAction();
    }

    @FXML
    @Override
    public void pageLastAction() {
        tableController.pageLastAction();
    }


    /*
        interface
     */
    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!editController.changed && !attributesController.changed && !columnsController.changed) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                parentController.saveAction();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            if (task != null) {
                task.cancel();
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
        }
        return goOn;
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            try {
                Tab tab = tabPane.getSelectionModel().getSelectedItem();
                if (tab == editTab) {
                    return editController.keyEventsFilter(event);

                } else if (tab == viewTab) {
                    return viewController.keyEventsFilter(event);

                } else if (tab == attributesTab) {
                    return attributesController.keyEventsFilter(event);

                } else if (tab == columnsTab) {
                    return columnsController.keyEventsFilter(event);

                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            return false;
        } else {
            return true;
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
//                HtmlPopController.openWebView(this, webView);
                return true;

            } else if (tab == editTab) {
//                MarkdownPopController.open(this, markdownArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {

                return true;

            } else if (tab == editTab) {

                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == editTab) {
//                Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
//                MenuWebviewController.pop(webViewController, null, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == viewTab) {
//                Point2D localToScreen = codesArea.localToScreen(codesArea.getWidth() - 80, 80);
//                MenuHtmlCodesController.open(this, codesArea, localToScreen.getX(), localToScreen.getY());
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public void cleanPane() {
        try {
            tableController = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
