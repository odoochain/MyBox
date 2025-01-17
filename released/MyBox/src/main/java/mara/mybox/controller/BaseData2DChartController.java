package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartController extends BaseData2DHandleController {

    protected String selectedCategory, selectedValue, groupParameters;
    protected DataTableGroup group;
    protected int chartMaxData, framesNumber;
    protected long groupid;
    protected Thread frameThread;
    protected Connection conn;
    protected List<List<String>> chartData;

    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector;
    @FXML
    protected Label noticeLabel;
    @FXML
    protected CheckBox displayAllCheck;
    @FXML
    protected TextField chartMaxInput;
    @FXML
    protected ControlData2DResults groupDataController;
    @FXML
    protected ControlPlay playController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTab();

            if (displayAllCheck != null) {
                displayAllCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayAll", true));
                displayAllCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "DisplayAll", displayAllCheck.isSelected());
                    noticeMemory();
                });

                displayAllCheck.visibleProperty().bind(allPagesRadio.selectedProperty());
            }

            chartMaxData = UserConfig.getInt(baseName + "ChartMaxData", 100);
            if (chartMaxData <= 0) {
                chartMaxData = 100;
            }
            if (chartMaxInput != null) {
                chartMaxInput.setText(chartMaxData + "");
            }

            if (playController != null) {
                frameThread = new Thread() {
                    @Override
                    public void run() {
                        loadFrame(playController.currentIndex);
                    }
                };
                playController.setParameters(this, frameThread, snapNode());

                playController.stopped.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        try {
                            if (conn != null) {
                                conn.close();
                                conn = null;
                            }
                        } catch (Exception ex) {
                        }
                    }
                });

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initDataTab() {
        try {
            if (categoryColumnSelector != null) {
                categoryColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
            }

            if (valueColumnSelector != null) {
                valueColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            makeOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeOptions() {
        try {
            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;
            if (categoryColumnSelector != null) {
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
                categoryColumnSelector.getItems().setAll(names);
                if (selectedCategory != null && names.contains(selectedCategory)) {
                    categoryColumnSelector.setValue(selectedCategory);
                } else {
                    categoryColumnSelector.getSelectionModel().select(0);
                }
            }
            if (valueColumnSelector != null) {
                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
                valueColumnSelector.getItems().setAll(names);
                if (selectedValue != null && names.contains(selectedValue)) {
                    valueColumnSelector.setValue(selectedValue);
                } else {
                    valueColumnSelector.getSelectionModel().select(names.size() > 1 ? 1 : 0);
                }
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        noticeMemory();
        return ok;
    }

    public void noticeMemory() {
        if (noticeLabel == null) {
            return;
        }
        noticeLabel.setVisible(isAllPages()
                && (displayAllCheck == null || displayAllCheck.isSelected()));
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            if (categoryColumnSelector != null) {
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            }
            if (valueColumnSelector != null) {
                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
            }

            group = null;
            framesNumber = -1;
            groupid = -1;
            groupParameters = null;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public String chartTitle() {
        if (groupController != null) {
            return groupChartTitle();
        } else {
            return baseChartTitle();
        }
    }

    public String baseChartTitle() {
        return baseTitle;
    }

    public String groupChartTitle() {
        if (group == null) {
            return baseChartTitle();
        }
        return baseChartTitle() + (this instanceof BaseData2DChartHtmlController ? "<BR>" : "\n")
                + group.getIdColName() + groupid + " - " + groupParameters;
    }

    public String categoryName() {
        return selectedCategory;
    }

    @Override
    protected void startOperation() {
        if (groupController != null) {
            startGroup();
        } else {
            startNoGroup();
        }
    }

    /*
        no group
     */
    protected void startNoGroup() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    readData();
                    data2D.stopFilter();
                    return outputData != null && !outputData.isEmpty();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                if (ok) {
                    outputData();
                }
            }

        };
        start(task);
    }

    public void readData() {
        try {
            boolean showRowNumber = showRowNumber();
            outputData = sortedData(dataColsIndices, showRowNumber);
            if (outputData == null || scaleSelector == null) {
                return;
            }
            outputColumns = data2D.makeColumns(dataColsIndices, showRowNumber);
            boolean needScale = false;
            for (Data2DColumn c : outputColumns) {
                if (c.needScale()) {
                    needScale = true;
                    break;
                }
            }
            if (!needScale) {
                return;
            }
            List<List<String>> scaled = new ArrayList<>();
            for (List<String> row : outputData) {
                List<String> srow = new ArrayList<>();
                for (int i = 0; i < outputColumns.size(); i++) {
                    String s = row.get(i);
                    if (s == null || !outputColumns.get(i).needScale()) {
                        srow.add(s);
                    } else {
                        srow.add(DoubleTools.scaleString(s, invalidAs, scale));
                    }
                }
                scaled.add(srow);
            }
            outputData = scaled;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
    }

    public void outputData() {
        drawChart();
    }

    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            chartMax();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void redrawChart() {
        drawChart();
    }

    @FXML
    @Override
    public void refreshAction() {
        if (chartMaxInput != null) {
            boolean ok;
            try {
                int v = Integer.valueOf(chartMaxInput.getText());
                if (v > 0) {
                    chartMaxData = v;
                    UserConfig.setInt(baseName + "ChartMaxData", chartMaxData);
                    ok = true;
                } else {
                    ok = false;
                }
            } catch (Exception ex) {
                ok = false;
            }
            if (ok) {
                chartMaxInput.setStyle(null);
            } else {
                chartMaxInput.setStyle(UserConfig.badStyle());
                popError(message("Invalid") + ": " + message("Maximum"));
                return;
            }
        }

        okAction();
    }

    public List<List<String>> chartMax() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return null;
            }
            if (chartMaxData > 0 && chartMaxData < outputData.size()) {
                chartData = outputData.subList(0, chartMaxData);
            } else {
                chartData = outputData;
            }
            return chartData;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void goMaxAction() {
        if (chartMaxInput != null) {
            boolean ok;
            String s = chartMaxInput.getText();
            if (s == null || s.isBlank()) {
                chartMaxData = -1;
                ok = true;
            } else {
                try {
                    int v = Integer.valueOf(s);
                    if (v > 0) {
                        chartMaxData = v;

                        ok = true;
                    } else {
                        ok = false;
                    }
                } catch (Exception ex) {
                    ok = false;
                }
            }
            if (ok) {
                UserConfig.setInt(baseName + "ChartMaxData", chartMaxData);
                chartMaxInput.setStyle(null);
            } else {
                chartMaxInput.setStyle(UserConfig.badStyle());
                popError(message("Invalid") + ": " + message("Maximum"));
                return;
            }
        }

        drawChart();
    }

    /*
        group
     */
    protected void startGroup() {
        if (task != null) {
            task.cancel();
        }
        playController.clear();
        groupDataController.loadNull();
        group = null;
        framesNumber = -1;
        task = new SingletonTask<Void>(this) {

            List<String> groupLabels;

            @Override
            protected boolean handle() {
                try {
                    List<Integer> cols = dataColsIndices;
                    List<String> sortNames = sortNames();
                    if (sortNames != null) {
                        for (String name : sortNames()) {
                            int col = data2D.colOrder(name);
                            if (!cols.contains(col)) {
                                cols.add(col);
                            }
                        }
                    }
                    outputColumns = data2D.makeColumns(cols, showRowNumber());
                    group = groupData(DataTableGroup.TargetType.Table,
                            cols, showRowNumber(), maxData, scale);
                    if (!group.run()) {
                        return false;
                    }
                    groupLabels = group.getParameterValues(task);
                    framesNumber = groupLabels.size();
                    return initGroups();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                task = null;
                if (ok) {
                    loadChartData();
                    playController.play(groupLabels);
                }
            }

        };
        start(task);
    }

    protected boolean initGroups() {
        return framesNumber > 0;
    }

    protected void loadChartData() {
        if (group.getTargetData() != null) {
            groupDataController.loadData(group.getTargetData().cloneAll());
        }
    }

    public boolean initFrame() {
        return outputData != null && !outputData.isEmpty();
    }

    public void loadFrame(int index) {
        if (group == null || framesNumber <= 0 || index < 0 || index > framesNumber) {
            playController.clear();
            return;
        }
        groupid = index + 1;  // groupid is 1-based
        if (makeFrameData()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    drawFrame();
                }
            });
        }
    }

    protected boolean makeFrameData() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DerbyBase.getConnection();
            }
            outputData = group.groupData(conn, groupid, outputColumns);
            groupParameters = group.parameterValue(conn, groupid);
            return initFrame();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return false;
        }
    }

    public void drawFrame() {
    }

    public Node snapNode() {
        return null;
    }

    @Override
    public void cleanPane() {
        try {
            if (conn != null) {
                conn.close();
            }
            if (playController != null) {
                playController.clear();
            }
            if (groupDataController != null) {
                groupDataController.loadData(null);
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
