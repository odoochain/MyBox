package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DSelect extends ControlData2DLoad {

    protected ControlData2DEditTable tableController;
    protected List<Integer> checkedRowsIndices, checkedColsIndices;
    protected boolean idExclude = false, noColumnSelection = false;
    protected ChangeListener<Boolean> tableStatusListener;

    @FXML
    protected ToggleGroup rowsGroup;
    @FXML
    protected RadioButton selectedRadio, allPagesRadio, currentPageRadio;
    @FXML
    protected Label titleLabel;
    @FXML
    protected FlowPane rowsPane, columnsPane;

    @Override
    public void initControls() {
        try {
            super.initControls();

            String rowsType = UserConfig.getString(baseName + "RowsSelection", "CurrentPage");
            if (rowsType == null) {
                currentPageRadio.fire();
            } else if ("AllPages".equals(rowsType)) {
                allPagesRadio.fire();
            } else if ("Selected".equals(rowsType)) {
                selectedRadio.fire();
            }
            rowsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (allPagesRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelection", "AllPages");
                    } else if (selectedRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelection", "Selected");
                    } else {
                        UserConfig.setString(baseName + "RowsSelection", "CurrentPage");
                    }
                    notifySelected();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseController parent, ControlData2DEditTable tableController) {
        try {
            if (tableController == null) {
                return;
            }
            this.parentController = parent;
            this.tableController = tableController;
            updateData();

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    updateData();
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

            tableView.requestFocus();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void idExclude(boolean idExclude) {
        this.idExclude = idExclude;
    }

    public void noColumnSelection(boolean noColumnSelection) {
        this.noColumnSelection = noColumnSelection;
        if (noColumnSelection) {
            if (thisPane.getChildren().contains(columnsPane)) {
                thisPane.getChildren().remove(columnsPane);
            }
        } else {
            if (!thisPane.getChildren().contains(columnsPane)) {
                thisPane.getChildren().add(2, columnsPane);
            }
        }
    }

    public void updateData() {
        if (tableController == null) {
            return;
        }
        data2D = tableController.data2D.cloneAll();
        makeColumns();
        isSettingValues = true;
        tableData.setAll(tableController.tableData);
        currentPage = tableController.currentPage;
        startRowOfCurrentPage = tableController.startRowOfCurrentPage;
        pageSize = tableController.pageSize;
        pagesNumber = tableController.pagesNumber;
        dataSize = tableController.dataSize;
        dataSizeLoaded = true;
        isSettingValues = false;
        setPagination();
        notifyLoaded();
        checkChanged();
    }

    public void checkChanged() {
        if (data2D.isMutiplePages()) {
            allPagesRadio.setDisable(false);
            showPaginationPane(true);
        } else {
            if (allPagesRadio.isSelected()) {
                currentPageRadio.fire();
            }
            allPagesRadio.setDisable(true);
            showPaginationPane(false);
        }
    }

    @Override
    protected void showPaginationPane(boolean show) {
        paginationPane.setVisible(show);
        if (show) {
            if (!thisPane.getChildren().contains(paginationPane)) {
                thisPane.getChildren().add(paginationPane);
            }
        } else {
            if (thisPane.getChildren().contains(paginationPane)) {
                thisPane.getChildren().remove(paginationPane);
            }
        }
    }

    public boolean allPages() {
        checkChanged();
        return allPagesRadio.isSelected();
    }

    @Override
    public void makeColumns() {
        try {
            if (!validateData()) {
                return;
            }
            super.makeColumns();
            if (noColumnSelection) {
                return;
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = new CheckBox(tableColumn.getText());
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        notifySelected();
                    }
                });
                tableColumn.setGraphic(cb);
                tableColumn.setText(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        restoreSelections();
    }

    // If none selected then select all
    public List<Integer> checkedRowsIndices() {
        try {
            checkedRowsIndices = new ArrayList<>();
            List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (allPages() || selected == null || selected.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    checkedRowsIndices.add(i);
                }
            } else {
                for (int i : selected) {
                    checkedRowsIndices.add(i);
                }
            }
            return checkedRowsIndices;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public List<List<String>> selectedRows() {
        try {
            List<List<String>> data = new ArrayList<>();
            List<List<String>> selected = tableView.getSelectionModel().getSelectedItems();
            if (allPages() || selected == null || selected.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    data.add(tableData.get(i));
                }
            } else {
                for (List<String> d : selected) {
                    data.add(d);
                }
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public void selectRows(List<Integer> rows) {
        try {
            isSettingValues = true;
            if (rows != null && !rows.isEmpty() && rows.size() != tableData.size()) {
                for (int i = 0; i < tableData.size(); i++) {
                    if (rows.contains(i)) {
                        tableView.getSelectionModel().select(i);
                    } else {
                        tableView.getSelectionModel().clearSelection(i);
                    }
                }
            } else {
                tableView.getSelectionModel().clearSelection();
            }
            isSettingValues = false;
            notifySelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectAllColumns() {
        try {
            if (noColumnSelection) {
                return;
            }
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(true);
            }
            isSettingValues = false;
            notifySelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void selectNoneColumn() {
        try {
            if (noColumnSelection) {
                return;
            }
            isSettingValues = true;
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                cb.setSelected(false);
            }
            isSettingValues = false;
            notifySelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    // If none selected then select all
    public List<Integer> checkedColsIndices() {
        try {
            if (noColumnSelection) {
                return null;
            }
            checkedColsIndices = new ArrayList<>();
            List<Integer> all = new ArrayList<>();
            int idOrder = -1;
            if (idExclude) {
                idOrder = data2D.idOrder();
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                int col = data2D.colOrder(cb.getText());
                if (col >= 0 && col != idOrder) {
                    all.add(col);
                    if (cb.isSelected()) {
                        checkedColsIndices.add(col);
                    }
                }
            }
            if (checkedColsIndices.isEmpty()) {
                checkedColsIndices = all;
            }
            return checkedColsIndices;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    // If none selected then select all
    public List<String> checkedColsNames() {
        try {
            if (noColumnSelection) {
                return null;
            }
            List<String> names = new ArrayList<>();
            List<String> all = new ArrayList<>();
            int idOrder = -1;
            if (idExclude) {
                idOrder = data2D.idOrder();
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                int col = data2D.colOrder(cb.getText());
                if (col >= 0 && col != idOrder) {
                    all.add(cb.getText());
                    if (cb.isSelected()) {
                        names.add(cb.getText());
                    }
                }
            }
            if (names.isEmpty()) {
                names = all;
            }
            return names;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    // If none selected then select all
    public List<Data2DColumn> checkedCols() {
        try {
            if (noColumnSelection) {
                return null;
            }
            List<Data2DColumn> cols = new ArrayList<>();
            List<Data2DColumn> all = new ArrayList<>();
            int idOrder = -1;
            if (idExclude) {
                idOrder = data2D.idOrder();
            }
            for (int i = 2; i < tableView.getColumns().size(); i++) {
                TableColumn tableColumn = tableView.getColumns().get(i);
                CheckBox cb = (CheckBox) tableColumn.getGraphic();
                int col = data2D.colOrder(cb.getText());
                if (col >= 0 && col != idOrder) {
                    Data2DColumn dcol = data2D.getColumns().get(col).cloneAll();
                    all.add(dcol);
                    if (cb.isSelected()) {
                        cols.add(dcol);
                    }
                }
            }
            if (cols.isEmpty()) {
                cols = all;
            }
            return cols;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    // If none selected then select all
    public List<List<String>> selectedData(boolean rowNumber) {
        try {
            if (!checkSelections()) {
                return null;
            }
            return selectedData(checkedRowsIndices, checkedColsIndices, rowNumber);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public List<List<String>> selectedData(List<Integer> rows, List<Integer> cols, boolean rowNumber) {
        try {
            if (rows == null || rows.isEmpty()
                    || cols == null || cols.isEmpty()) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            int size = tableData.size();
            for (int row : rows) {
                if (row < 0 || row >= size) {
                    continue;
                }
                List<String> tableRow = tableData.get(row);
                List<String> newRow = new ArrayList<>();
                if (rowNumber) {
                    newRow.add((row + 1) + "");
                }
                for (int col : cols) {
                    int index = col + 1;
                    if (index < 0 || index >= tableRow.size()) {
                        continue;
                    }
                    newRow.add(tableRow.get(index));
                }
                data.add(newRow);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public boolean checkSelections() {
        checkedRowsIndices = checkedRowsIndices();
        checkedColsIndices = checkedColsIndices();
        return checkedRowsIndices != null && !checkedRowsIndices.isEmpty()
                && (noColumnSelection || (checkedColsIndices != null && !checkedColsIndices.isEmpty()));
    }

    public boolean isSquare() {
        checkedRowsIndices = checkedRowsIndices();
        checkedColsIndices = checkedColsIndices();
        return checkedRowsIndices != null && checkedColsIndices != null
                && !checkedRowsIndices.isEmpty()
                && checkedRowsIndices.size() == checkedColsIndices.size();
    }

    public void selectCols(List<Integer> cols) {
        try {
            if (noColumnSelection) {
                return;
            }
            isSettingValues = true;
            if (cols != null && !cols.isEmpty() && cols.size() != tableView.getColumns().size() - 2) {
                for (int i = 2; i < tableView.getColumns().size(); i++) {
                    TableColumn tableColumn = tableView.getColumns().get(i);
                    CheckBox cb = (CheckBox) tableColumn.getGraphic();
                    int col = data2D.colOrder(cb.getText());
                    cb.setSelected(col >= 0 && cols.contains(col));
                }
            } else {
                selectNoneColumn();
            }
            isSettingValues = false;
            notifySelected();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void restoreSelections() {
        selectRows(checkedRowsIndices);
        selectCols(checkedColsIndices);
    }

    public void setLabel(String s) {
        titleLabel.setText(s);
    }

    public boolean notSelectColumn() {
        if (noColumnSelection) {
            return true;
        }
        for (int i = 2; i < tableView.getColumns().size(); i++) {
            TableColumn tableColumn = tableView.getColumns().get(i);
            CheckBox cb = (CheckBox) tableColumn.getGraphic();
            if (cb.isSelected()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            tableController.statusNotify.removeListener(tableStatusListener);
            tableStatusListener = null;
            tableController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
