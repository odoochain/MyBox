package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-15
 * @License Apache License Version 2.0
 */
public class ControlMatrixTable extends BaseSysTableController<Data2DDefinition> {

    protected MatricesBinaryCalculationController manageController;

    @FXML
    protected TableColumn<Data2DDefinition, Long> d2didColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Integer> rowsColumn, colsColumn;
    @FXML
    protected TableColumn<Data2DDefinition, String> nameColumn;
    @FXML
    protected TableColumn<Data2DDefinition, Date> modifyColumn;
    @FXML
    protected Button matrixAButton, matrixBButton, renameMatrixButton, clearMatricesButton, deleteMatricesButton;

    public ControlMatrixTable() {
    }

    public void setParameters(MatricesBinaryCalculationController manageController) {
        try {
            this.manageController = manageController;

            tableDefinition = manageController.dataAMatrix.tableData2DDefinition;
            queryConditions = "data_type=" + manageController.dataAMatrix.type();

            loadTableData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            d2didColumn.setCellValueFactory(new PropertyValueFactory<>("d2did"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("dataName"));
            colsColumn.setCellValueFactory(new PropertyValueFactory<>("colsNumber"));
            rowsColumn.setCellValueFactory(new PropertyValueFactory<>("rowsNumber"));
            modifyColumn.setCellValueFactory(new PropertyValueFactory<>("modifyTime"));
            modifyColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void itemClicked() {
    }

    @Override
    public void itemDoubleClicked() {
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        clearMatricesButton.setDisable(isEmpty);
        deleteMatricesButton.setDisable(none);
        renameMatrixButton.setDisable(none);
        matrixAButton.setDisable(none);
        matrixBButton.setDisable(none);
    }

    @FXML
    public void matrixAAction() {
        try {
            Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            manageController.dataAController.loadDef(selected);
            manageController.tabPane.getSelectionModel().select(manageController.dataATab);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void matrixBAction() {
        try {
            Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            manageController.dataBController.loadDef(selected);
            manageController.tabPane.getSelectionModel().select(manageController.dataBTab);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
    }

    @FXML
    public void renameAction() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        String newName = PopTools.askValue(getBaseTitle(), message("CurrentName") + ":" + selected.getDataName(),
                message("NewName"), selected.getDataName() + "m");
        if (newName == null || newName.isBlank()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private Data2DDefinition def;

                @Override
                protected boolean handle() {
                    selected.setDataName(newName);
                    def = manageController.dataAMatrix.tableData2DDefinition.updateData(selected);
                    return def != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    tableData.set(index, def);
                    if (def.getD2did() == manageController.dataAMatrix.getD2did()) {
                        manageController.dataAMatrix.setDataName(newName);
                        manageController.dataAController.attributesController.updateDataName();
                    }
                    if (def.getD2did() == manageController.dataBMatrix.getD2did()) {
                        manageController.dataBMatrix.setDataName(newName);
                        manageController.dataBController.attributesController.updateDataName();
                    }
                }

            };
            start(task);
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("SetAsMatrixA"), StyleTools.getIconImage("iconA.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                matrixAAction();
            });
            menu.setDisable(matrixAButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("SetAsMatrixB"), StyleTools.getIconImage("iconB.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                matrixBAction();
            });
            menu.setDisable(matrixBButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("Rename"), StyleTools.getIconImage("iconRename.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameAction();
            });
            menu.setDisable(renameMatrixButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImage("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteAction();
            });
            menu.setDisable(deleteMatricesButton.isDisable());
            items.add(menu);

            List<MenuItem> superItems = super.makeTableContextMenu();
            if (!superItems.isEmpty()) {
                items.add(new SeparatorMenuItem());
                items.addAll(superItems);
            }

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
