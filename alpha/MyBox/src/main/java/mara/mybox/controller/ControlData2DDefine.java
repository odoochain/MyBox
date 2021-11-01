package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.Data2D;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableComboBoxCell;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class ControlData2DDefine extends BaseDataTableController<ColumnDefinition> {

    protected ControlData2D dataController;
    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;
    protected Data2D data2D;
    protected int maxRandom;
    protected short scale;
    protected boolean changed;

    @FXML
    protected TableColumn<ColumnDefinition, String> nameColumn, typeColumn;
    @FXML
    protected TableColumn<ColumnDefinition, Boolean> notNullColumn;
    @FXML
    protected TableColumn<ColumnDefinition, Integer> indexColumn, lengthColumn, widthColumn;
    @FXML
    protected Label idLabel;
    @FXML
    protected TextField timeInput, dataTypeInput, dataNameInput;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector;

    public ControlData2DDefine() {
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            indexColumn.setEditable(false);

            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ColumnDefinition, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<ColumnDefinition, String> t) {
                    if (t == null) {
                        return;
                    }
                    ColumnDefinition column = t.getRowValue();
                    String v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    if (!v.equals(column.getName())) {
                        column.setName(v);
                        changed(true);
                    }
                }
            });

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeString"));
            ObservableList<String> types = FXCollections.observableArrayList();
            for (ColumnType type : ColumnDefinition.editTypes()) {
                types.add(message(type.name()));
            }
            typeColumn.setCellFactory(TableComboBoxCell.forTableColumn(types));
            typeColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ColumnDefinition, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<ColumnDefinition, String> t) {
                    if (t == null) {
                        return;
                    }
                    ColumnDefinition column = t.getRowValue();
                    String v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    for (ColumnType type : ColumnDefinition.editTypes()) {
                        if (type.name().equals(v) || message(type.name()).equals(v)) {
                            if (type != column.getType()) {
                                column.setType(type);
                                changed(true);
                            }
                            return;
                        }
                    }
                }
            });

            notNullColumn.setCellValueFactory(new PropertyValueFactory<>("notNull"));
            notNullColumn.setCellFactory(CheckBoxTableCell.forTableColumn(notNullColumn));
            notNullColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ColumnDefinition, Boolean>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<ColumnDefinition, Boolean> t) {
                    if (t == null) {
                        return;
                    }
                    ColumnDefinition column = t.getRowValue();
                    Boolean v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    if (v != column.isNotNull()) {
                        t.getRowValue().setNotNull(v);
                        changed(true);
                    }
                }
            });

            lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
            lengthColumn.setCellFactory(TableAutoCommitCell.forIntegerColumn());
            lengthColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ColumnDefinition, Integer>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<ColumnDefinition, Integer> t) {
                    if (t == null) {
                        return;
                    }
                    ColumnDefinition column = t.getRowValue();
                    Integer v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    if (v != column.getLength()) {
                        column.setLength(v);
                        changed(true);
                    }
                }
            });

            widthColumn.setCellValueFactory(new PropertyValueFactory<>("width"));
            widthColumn.setCellFactory(TableAutoCommitCell.forIntegerColumn());
            widthColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ColumnDefinition, Integer>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<ColumnDefinition, Integer> t) {
                    if (t == null) {
                        return;
                    }
                    ColumnDefinition column = t.getRowValue();
                    Integer v = t.getNewValue();
                    if (column == null || v == null) {
                        return;
                    }
                    if (v != column.getWidth()) {
                        column.setWidth(v);
                        changed(true);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;
            tableDataDefinition = dataController.tableDataDefinition;
            tableDataColumn = dataController.tableDataColumn;
            tableDefinition = dataController.tableDataColumn;
            baseName = dataController.baseName;
            data2D = dataController.data2D;

            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(scaleSelector.getValue());
                            if (v >= 0 && v <= 15) {
                                scale = (short) v;
                                UserConfig.setInt(baseName + "Scale", v);
                                scaleSelector.getEditor().setStyle(null);
                                changed(true);
                            } else {
                                scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            maxRandom = UserConfig.getInt(baseName + "MaxRandom", 100000);
            if (maxRandom < 0) {
                maxRandom = 100000;
            }
            randomSelector.getItems().addAll(Arrays.asList("1", "100", "10", "1000", "10000", "1000000", "10000000"));
            randomSelector.setValue(maxRandom + "");
            randomSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                maxRandom = v;
                                UserConfig.setInt(baseName + "MaxRandom", v);
                                randomSelector.getEditor().setStyle(null);
                                changed(true);
                            } else {
                                randomSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            randomSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            saveButton.disableProperty().bind(Bindings.isEmpty(dataNameInput.textProperty())
                    .or(dataNameInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(scaleSelector.getEditor().textProperty()))
                    .or(scaleSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(randomSelector.getEditor().textProperty()))
                    .or(randomSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void loadTableData() {
        try {
            tableData.clear();
            tableView.refresh();
            checkSelected();
            idLabel.setText("");
            dataNameInput.setText("");
            editNull();
            changed(false);
            if (data2D == null || data2D.getDefinition() == null) {
                return;
            }
            isSettingValues = true;
            idLabel.setText(data2D.getDefinition().getDfid() + "");
            timeInput.setText(DateTools.datetimeToString(data2D.getDefinition().getModifyTime()));
            dataTypeInput.setText(message(data2D.getDefinition().getDataType().name()));
            dataNameInput.setText(data2D.getDefinition().getDataName());
            scaleSelector.setValue(data2D.getDefinition().getScale() + "");
            randomSelector.setValue(data2D.getDefinition().getMaxRandom() + "");
            isSettingValues = false;
            if (data2D.isColumnsValid()) {
                for (ColumnDefinition column : data2D.getColumns()) {
                    tableData.add(column.cloneBase());
                }
            }
            tableView.refresh();
            checkSelected();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void changed(boolean changed) {
        this.changed = changed;
        dataController.defineTab.setText(message("Define") + (changed ? "*" : ""));
    }

    @FXML
    @Override
    public void copyAction() {
        List<ColumnDefinition> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (ColumnDefinition column : selected) {
            tableData.add(column.cloneBase());
        }
    }

    @FXML
    public void insertAction() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void trimAction() {
        try {
            String prefix = message(data2D.colPrefix());
            for (int i = 0; i < tableData.size(); i++) {
                tableData.get(i).setName(prefix + (i + 1));
            }
            tableView.refresh();
            changed(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (data2D.isFile() && data2D.getFile() == null) {
            dataController.saveAction();
            return;
        }
        String name = dataNameInput.getText();
        if (name == null || name.isBlank()) {
            popError(message("InvalidParameter") + ": " + message("ColumnName"));
            return;
        }
        StringTable validateTable = ColumnDefinition.validate(tableData);
        if (validateTable != null && !validateTable.isEmpty()) {
            validateTable.htmlTable();
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        DataDefinition def = data2D.getDefinition();
                        if (def == null) {
                            def = new DataDefinition();
                        }
                        def.setDataName(name);
                        def.setScale(scale);
                        def.setMaxRandom(maxRandom);
                        def.setModifyTime(new Date());
                        def = tableDataDefinition.writeData(conn, def);
                        data2D.setDefinition(def);

                        long defid = def.getDfid();
                        List<ColumnDefinition> columns = new ArrayList<>();
                        for (int i = 0; i < tableData.size(); i++) {
                            ColumnDefinition column = tableData.get(i);
                            column.setDataDefinition(def);
                            column.setDataid(defid);
                            column.setIndex(i);
                            columns.add(column);
                        }
                        tableDataColumn.save(defid, columns);
                        data2D.setColumns(columns);

                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    dataController.updateInterface();
                    popSuccessful();
                }
            };
            start(task);
        }

    }

}