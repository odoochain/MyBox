package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableRowSelectionCell;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @param <P> Data
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public abstract class BaseTableViewController<P> extends BaseController {

    protected ObservableList<P> tableData;
    protected String tableName, idColumn, queryConditions, queryConditionsString;
    protected int pageSize, pagesNumber, totalSize;
    protected int currentPage, startRowOfCurrentPage, editingIndex, viewingIndex;  // 0-based
    protected boolean paginate;

    @FXML
    protected TableView<P> tableView;
    @FXML
    protected TableColumn<P, Boolean> rowsSelectionColumn;
    @FXML
    protected Label dataSizeLabel, selectedLabel, pageLabel;
    @FXML
    protected CheckBox deleteConfirmCheck, allRowsCheck;
    @FXML
    protected Button moveUpButton, moveDownButton;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;

    public BaseTableViewController() {
        tableName = "";
        TipsLabelKey = "TableTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableData = FXCollections.observableArrayList();
            editingIndex = viewingIndex = -1;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initTable();
            initButtons();
            initPagination();
            initMore();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMore() {

    }

    /*
        table
     */
    protected void initTable() {
        try {
            if (tableView == null) {
                return;
            }

            tableData.addListener((ListChangeListener.Change<? extends P> change) -> {
                if (isSettingValues) {
                    return;
                }
                tableChanged();
            });

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends P> ov, P t, P t1) -> {
                        checkSelected();
                    });

            tableView.setOnMouseClicked((MouseEvent event) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    popTableMenu(event);
                } else if (event.getClickCount() == 1) {
                    itemClicked();
                } else if (event.getClickCount() > 1) {
                    itemDoubleClicked();
                }
            });

            tableView.setItems(tableData);

            if (deleteConfirmCheck != null) {
                deleteConfirmCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            UserConfig.setBoolean(baseName + "ConfirmDelete", deleteConfirmCheck.isSelected());
                        });
                deleteConfirmCheck.setSelected(UserConfig.getBoolean(baseName + "ConfirmDelete", true));
            }

            initColumns();

            checkSelected();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initColumns() {
        try {
            if (allRowsCheck != null) {
                allRowsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            for (int i = 0; i < tableData.size(); i++) {  // Fail to listen to selectAll() 
                                tableView.getSelectionModel().select(i);
                            }
                        } else {
                            tableView.getSelectionModel().clearSelection();
                        }
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean preLoadingTableData() {
        return true;
    }

    public void loadTableData() {
        if (!preLoadingTableData()) {
            return;
        }
        tableData.clear();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private List<P> data;

                @Override
                protected boolean handle() {
                    data = loadTableDataHandle();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (data != null && !data.isEmpty()) {
                        isSettingValues = true;
                        tableData.setAll(data);
                        isSettingValues = false;
                    }
                    postLoadedTableData();
                }
            };
            start(task, message("LoadingTableData"));
        }
    }

    protected List<P> loadTableDataHandle() {
        try {
            List<P> data;
            if (paginate) {
                totalSize = readDataSize();
                if (totalSize <= pageSize) {
                    pagesNumber = 1;
                } else {
                    pagesNumber = totalSize % pageSize == 0 ? totalSize / pageSize : totalSize / pageSize + 1;
                }
                if (currentPage < 0) {
                    currentPage = 0;
                }
                if (currentPage >= pagesNumber) {
                    currentPage = pagesNumber - 1;
                }
                startRowOfCurrentPage = pageSize * currentPage;
                data = readPageData();
            } else {
                pagesNumber = 1;
                currentPage = 0;
                data = readData();
                if (data != null) {
                    totalSize = data.size();
                } else {
                    totalSize = 0;
                }
            }
            return data;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public void postLoadedTableData() {
        tableView.refresh();
        checkSelected();
        editNull();
        viewNull();
        if (rowsSelectionColumn != null) {
            rowsSelectionColumn.setCellFactory(TableRowSelectionCell.create(tableView, startRowOfCurrentPage));
        }
    }

    public int readDataSize() {
        return 0;
    }

    public List<P> readData() {
        return null;
    }

    public List<P> readPageData() {
        return null;
    }

    protected void tableChanged() {
    }

    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        checkButtons();
    }

    public void itemClicked() {
    }

    public void itemDoubleClicked() {
        editAction();
    }

    protected void popTableMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        List<MenuItem> items = makeTableContextMenu();
        if (items == null || items.isEmpty()) {
            return;
        }
        items.add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(Languages.message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            List<MenuItem> group = new ArrayList<>();
            if (viewButton != null && viewButton.isVisible() && !viewButton.isDisabled()) {
                menu = new MenuItem(Languages.message("View"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    viewAction();
                });
                group.add(menu);
            }

            if (editButton != null && editButton.isVisible() && !editButton.isDisabled()) {
                menu = new MenuItem(Languages.message("Edit"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    editAction();
                });
                group.add(menu);
            }

            if (copyButton != null && copyButton.isVisible() && !copyButton.isDisabled()) {
                menu = new MenuItem(Languages.message("Copy"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    copyAction();
                });
                group.add(menu);
            }

            menu = new MenuItem(Languages.message("Delete"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteAction();
            });
            group.add(menu);

            menu = new MenuItem(Languages.message("Clear"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearAction();
            });
            group.add(menu);

            if (!group.isEmpty()) {
                items.addAll(group);
                items.add(new SeparatorMenuItem());
            }

            if (pageNextButton != null && pageNextButton.isVisible() && !pageNextButton.isDisabled()) {
                menu = new MenuItem(Languages.message("NextPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pageNextAction();
                });
                items.add(menu);
            }

            if (pagePreviousButton != null && pagePreviousButton.isVisible() && !pagePreviousButton.isDisabled()) {
                menu = new MenuItem(Languages.message("PreviousPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pagePreviousAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(Languages.message("Refresh"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                refreshAction();
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    /*
        buttons
     */
    protected void initButtons() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        if (deleteButton != null) {
            deleteButton.setDisable(none);
        }
        if (viewButton != null) {
            viewButton.setDisable(none);
        }
        if (editButton != null) {
            editButton.setDisable(none);
        }
        if (clearButton != null) {
            clearButton.setDisable(isEmpty);
        }
        if (moveUpButton != null) {
            moveUpButton.setDisable(none);
        }
        if (moveDownButton != null) {
            moveDownButton.setDisable(none);
        }
        if (selectedLabel != null) {
            selectedLabel.setText(Languages.message("Selected") + ": "
                    + (none ? 0 : tableView.getSelectionModel().getSelectedIndices().size()));
        }
    }

    @FXML
    @Override
    public void addAction() {
        editNull();
    }

    @FXML
    public void editAction() {
        edit(tableView.getSelectionModel().getSelectedIndex());
    }

    public void editNull() {
        editingIndex = -1;
    }

    public void edit(int index) {
        if (index < 0 || tableData == null || index >= tableData.size()) {
            editNull();
            return;
        }
        editingIndex = index;
    }

    @FXML
    public void viewAction() {
        view(tableView.getSelectionModel().getSelectedIndex());
    }

    public void viewNull() {
        viewingIndex = -1;
    }

    public void view(int index) {
        if (index < 0 || tableData == null || index >= tableData.size()) {
            viewNull();
            return;
        }
        viewingIndex = index;
    }

    @FXML
    @Override
    public void copyAction() {
    }

    @FXML
    @Override
    public void recoverAction() {
        edit(editingIndex);
    }

    @FXML
    @Override
    public void deleteAction() {
        List<Integer> indice = tableView.getSelectionModel().getSelectedIndices();
        if (indice == null || indice.isEmpty()) {
            return;
        }
        if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
            if (!PopTools.askSure(getBaseTitle(), Languages.message("SureDelete"))) {
                return;
            }
        }
        if (indice.contains(editingIndex)) {
            editNull();
        }
        if (indice.contains(viewingIndex)) {
            viewNull();
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private int deletedCount = 0;

                @Override
                protected boolean handle() {
                    deletedCount = deleteSelectedData();
                    return deletedCount >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(Languages.message("Deleted") + ":" + deletedCount);
                    if (deletedCount > 0) {
                        afterDeletion();
                    }
                }
            };
            start(task);
        }
    }

    protected int deleteSelectedData() {
        List<P> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            return 0;
        }
        return deleteData(selected);
    }

    protected int deleteData(List<P> data) {
        return 0;
    }

    protected void afterDeletion() {
        refreshAction();
    }

    @FXML
    @Override
    public void clearAction() {
        if (tableData == null || tableData.isEmpty()) {
            return;
        }
        if (!PopTools.askSure(getBaseTitle(), Languages.message("SureClear"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                int deletedCount = 0;

                @Override
                protected boolean handle() {
                    deletedCount = clearData();
                    return deletedCount >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(Languages.message("Deleted") + ":" + deletedCount);
                    if (deletedCount > 0) {
                        clearView();
                        afterClear();
                    }
                }
            };
            start(task);
        }
    }

    protected void afterClear() {
        editNull();
        viewNull();
        afterDeletion();
    }

    protected int clearData() {
        return 0;
    }

    public void clearView() {
        isSettingValues = true;
        tableData.clear();
        isSettingValues = false;
        tableView.refresh();
        totalSize = 0;
        checkSelected();
        editNull();
        viewNull();
    }

    @FXML
    public void refreshAction() {
        loadTableData();
    }

    @FXML
    public void moveUpAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (Integer index : selected) {
            if (index == 0 || newselected.contains(index - 1)) {
                newselected.add(index);
                continue;
            }
            P info = tableData.get(index);
            tableData.set(index, tableView.getItems().get(index - 1));
            tableData.set(index - 1, info);
            newselected.add(index - 1);
        }
        tableView.getSelectionModel().clearSelection();
        for (Integer index : newselected) {
            tableView.getSelectionModel().select(index);
        }
        tableView.refresh();
    }

    @FXML
    public void moveDownAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        List<Integer> newselected = new ArrayList<>();
        for (int i = selected.size() - 1; i >= 0; --i) {
            int index = selected.get(i);
            if (index == tableData.size() - 1
                    || newselected.contains(index + 1)) {
                newselected.add(index);
                continue;
            }
            P info = tableData.get(index);
            tableData.set(index, tableData.get(index + 1));
            tableData.set(index + 1, info);
            newselected.add(index + 1);
        }
        tableView.getSelectionModel().clearSelection();
        for (Integer index : newselected) {
            tableView.getSelectionModel().select(index);
        }
        tableView.refresh();
    }

    /*
        pagination
     */
    protected void initPagination() {
        try {
            if (pageSelector == null) {
                pageSize = Integer.MAX_VALUE;
                paginate = false;
                return;
            }
            paginate = true;
            currentPage = 0;
            pageSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkCurrentPage();
                    });

            pageSizeSelector.getItems().addAll(Arrays.asList("50", "30", "100", "20", "60", "200", "300",
                    "500", "1000", "2000", "5000", "10000", "20000", "50000"));
            pageSize = UserConfig.getInt(baseName + "PageSize", 50);
            if (pageSize < 1) {
                pageSize = 50;
            }
            pageSizeSelector.setValue(pageSize + "");
            pageSizeSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                if (newValue == null) {
                    return;
                }
                try {
                    int v = Integer.parseInt(newValue.trim());
                    if (v <= 0) {
                        pageSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    } else {
                        pageSize = v;
                        UserConfig.setInt(baseName + "PageSize", pageSize);
                        pageSizeSelector.getEditor().setStyle(null);
                        if (!isSettingValues) {
                            loadTableData();
                        }
                    }
                } catch (Exception e) {
                    pageSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected boolean checkCurrentPage() {
        if (isSettingValues || pageSelector == null) {
            return false;
        }
        String value = pageSelector.getEditor().getText();
        try {
            int v = Integer.parseInt(value);
            if (v <= 0) {
                pageSelector.getEditor().setStyle(UserConfig.badStyle());
                return false;
            } else {
                currentPage = v - 1;
                pageSelector.getEditor().setStyle(null);
                loadTableData();
                return true;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(UserConfig.badStyle());
            return false;
        }
    }

    protected void setPagination() {
        try {
            if (paginationBox != null) {
                paginationBox.setVisible(paginate);
            }
            if (pageSelector == null) {
                return;
            }
            isSettingValues = true;
            if (paginate) {
                pageSelector.setDisable(false);
                List<String> pages = new ArrayList<>();
                for (int i = Math.max(1, currentPage - 20);
                        i <= Math.min(pagesNumber, currentPage + 20); i++) {
                    pages.add(i + "");
                }
                pageSelector.getItems().clear();
                pageSelector.getItems().addAll(pages);
                pageSelector.getSelectionModel().select((currentPage + 1) + "");

                pageLabel.setText("/" + pagesNumber);
                dataSizeLabel.setText(Languages.message("Data") + ": " + tableData.size() + "/" + totalSize);
                if (currentPage > 0) {
                    pagePreviousButton.setDisable(false);
                    pageFirstButton.setDisable(false);
                } else {
                    pagePreviousButton.setDisable(true);
                    pageFirstButton.setDisable(true);
                }
                if (currentPage >= pagesNumber - 1) {
                    pageNextButton.setDisable(true);
                    pageLastButton.setDisable(true);
                } else {
                    pageNextButton.setDisable(false);
                    pageLastButton.setDisable(false);
                }
            } else {
                pageSelector.getItems().clear();
                pageSelector.setDisable(true);
                pageLabel.setText("");
                dataSizeLabel.setText("");
                pagePreviousButton.setDisable(true);
                pageFirstButton.setDisable(true);
                pageNextButton.setDisable(true);
                pageLastButton.setDisable(true);
            }
            pageSelector.getEditor().setStyle(null);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @FXML
    public void goPage() {
        checkCurrentPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        currentPage++;
        loadTableData();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        currentPage--;
        loadTableData();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        currentPage = 0;
        loadTableData();
    }

    @FXML
    @Override
    public void pageLastAction() {
        currentPage = Integer.MAX_VALUE;
        loadTableData();
    }

}