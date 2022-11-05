package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticObject;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.data2d.reader.DataTableGroupStatistic;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.chart.PieChartMaker;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class Data2DGroupStatisticController extends Data2DChartXYController {

    protected DescriptiveStatistic calculation;
    protected DataFileCSV dataFile;
    protected PieChartMaker pieMaker;
    protected List<List<String>> pieData;
    protected List<Data2DColumn> pieColumns;

    @FXML
    protected Tab xyChartTab, pieChartTab;
    @FXML
    protected ControlStatisticSelection statisticController;
    @FXML
    protected ControlData2DResults statisticDataController, chartDataController;
    @FXML
    protected ControlData2DChartPie pieChartController;
    @FXML
    protected FlowPane columnsDisplayPane, valuesDisplayPane;
    @FXML
    protected RadioButton xyParametersRadio, pieParametersRadio;
    @FXML
    protected ToggleGroup pieCategoryGroup;

    public Data2DGroupStatisticController() {
        baseTitle = message("GroupStatistic");
        TipsLabelKey = "GroupStatisticTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            pieMaker = pieChartController.pieMaker;
            pieChartController.redrawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawPieChart();
                }
            });

            statisticController.mustCount();

            chartDataController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    xyChartTab.setDisable(false);
                    pieChartTab.setDisable(false);
                    refreshAction();
                }
            });

            pieCategoryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    makeCharts(false, true);
                }
            });

            chartTypesController.disableBubbleChart();
            xyChartTab.setDisable(true);
            pieChartTab.setDisable(true);

            displayAllCheck.visibleProperty().unbind();
            displayAllCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(displayAllCheck, new Tooltip(message("AllRowsLoadComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean initData() {
        if (!groupController.pickValues()) {
            return false;
        }
        checkObject();
        checkInvalidAs();
        return true;
    }

    @Override
    public boolean initChart() {
        return initChart(false);
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        dataFile = null;
        groupDataController.loadNull();
        statisticDataController.loadNull();
        chartDataController.loadNull();
        xyChartTab.setDisable(true);
        pieChartTab.setDisable(true);
        calculation = statisticController.pickValues()
                .setStatisticObject(StatisticObject.Columns)
                .setScale(scale)
                .setInvalidAs(invalidAs)
                .setHandleController(this)
                .setData2D(data2D)
                .setColsIndices(checkedColsIndices)
                .setColsNames(checkedColsNames);
        columnsDisplayPane.getChildren().clear();
        for (String c : checkedColsNames) {
            columnsDisplayPane.getChildren().add(new CheckBox(c));
        }
        valuesDisplayPane.getChildren().clear();
        for (StatisticType t : calculation.types) {
            valuesDisplayPane.getChildren().add(new CheckBox(message(t.name())));
        }
        task = new SingletonTask<Void>(this) {

            private DataTableGroup group;
            private DataTableGroupStatistic statistic;

            @Override
            protected boolean handle() {
                try {
                    group = groupData(DataTableGroup.TargetType.Table,
                            checkedColsNames, null, -1, scale);
                    if (!group.run()) {
                        return false;
                    }
                    task.setInfo(message("Statistic") + "...");
                    statistic = new DataTableGroupStatistic()
                            .setGroups(group).setCountChart(true)
                            .setCalculation(calculation)
                            .setCalNames(checkedColsNames)
                            .setTask(task);
                    if (!statistic.run()) {
                        return false;
                    }
                    dataFile = statistic.getChartData();
                    return dataFile != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                chartDataController.loadData(dataFile.cloneAll());
                groupDataController.loadData(group.getTargetData().cloneAll());
                statisticDataController.loadData(statistic.getStatisticData().cloneAll());
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    @FXML
    public void makeCharts(boolean forXY, boolean forPie) {
        if (forXY) {
            outputColumns = null;
            outputData = null;
            chartMaker.clearChart();
        }
        if (forPie) {
            pieColumns = null;
            pieData = null;
            pieMaker.clearChart();
        }
        if (dataFile == null) {
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        backgroundTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    dataFile.startTask(backgroundTask, null);

                    List<List<String>> resultsData;
                    if (displayAllCheck.isSelected()) {
                        resultsData = dataFile.allRows(false);
                    } else {
                        resultsData = chartDataController.data2D.tableRows(false);
                    }
                    if (resultsData == null) {
                        return false;
                    }
                    if (forXY && !makeXYData(resultsData)) {
                        return false;
                    }
                    if (forPie && !makePieData(resultsData)) {
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (forXY) {
                    drawXYChart();
                }
                if (forPie) {
                    drawPieChart();
                }
            }

            @Override
            protected void whenFailed() {

            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataFile.stopTask();
                backgroundTask = null;
            }

        };
        start(backgroundTask, false);
    }

    protected boolean makeXYData(List<List<String>> resultsData) {
        try {
            if (resultsData == null) {
                return false;
            }
            outputColumns = new ArrayList<>();
            Data2DColumn xyCategoryColumn
                    = dataFile.column(xyParametersRadio.isSelected() ? 1 : 0);
            outputColumns.add(xyCategoryColumn);

            List<String> colNames = new ArrayList<>();
            List<String> allName = new ArrayList<>();
            for (Node n : columnsDisplayPane.getChildren()) {
                CheckBox cb = (CheckBox) n;
                String name = cb.getText();
                if (cb.isSelected()) {
                    colNames.add(name);
                }
                allName.add(name);
            }
            if (colNames.isEmpty()) {
                if (allName.isEmpty()) {
                    error = message("SelectToHanlde") + ": " + message("ColumnsDisplayed");
                    return false;
                }
                colNames = allName;
            }

            List<String> sTypes = new ArrayList<>();
            List<String> allTypes = new ArrayList<>();
            for (Node n : valuesDisplayPane.getChildren()) {
                CheckBox cb = (CheckBox) n;
                String tname = cb.getText();
                if (cb.isSelected()) {
                    sTypes.add(tname);
                }
                allTypes.add(tname);
            }
            if (sTypes.isEmpty()) {
                if (allTypes.isEmpty()) {
                    error = message("SelectToHanlde") + ": " + message("ValuesDisplayed");
                    return false;
                }
                sTypes = allTypes;
            }
            List<Integer> cols = new ArrayList<>();
            for (String stype : sTypes) {
                if (message("Count").equals(stype)) {
                    outputColumns.add(dataFile.column(2));
                    cols.add(2);
                } else {
                    for (String col : colNames) {
                        int colIndex = dataFile.colOrder(col + "_" + stype);
                        outputColumns.add(dataFile.column(colIndex));
                        cols.add(colIndex);
                    }
                }
            }
            outputData = new ArrayList<>();
            for (List<String> data : resultsData) {
                List<String> xyRow = new ArrayList<>();
                String category = data.get(xyParametersRadio.isSelected() ? 1 : 0);
                xyRow.add(category);
                for (int colIndex : cols) {
                    xyRow.add(data.get(colIndex));
                }
                outputData.add(xyRow);
            }
            selectedCategory = xyCategoryColumn.getColumnName();
            selectedValue = message("Statistic");
            return initChart();
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean makePieData(List<List<String>> resultsData) {
        try {
            if (resultsData == null) {
                return false;
            }
            pieColumns = new ArrayList<>();
            Data2DColumn pieCategoryColumn
                    = dataFile.columns.get(pieParametersRadio.isSelected() ? 1 : 0);
            pieColumns.add(pieCategoryColumn);
            pieColumns.add(dataFile.columns.get(2));
            pieColumns.add(new Data2DColumn(message("Percentage"), ColumnDefinition.ColumnType.Double));

            pieData = new ArrayList<>();
            double sum = 0, count;
            for (List<String> data : resultsData) {
                try {
                    sum += Double.valueOf(data.get(2));
                } catch (Exception e) {
                }
            }
            for (List<String> data : resultsData) {
                try {
                    String category = data.get(pieParametersRadio.isSelected() ? 1 : 0);
                    List<String> pieRow = new ArrayList<>();
                    pieRow.add(category);
                    count = Double.valueOf(data.get(2));
                    pieRow.add((long) count + "");
                    pieRow.add(DoubleTools.percentage(count, sum, scale));
                    pieData.add(pieRow);
                } catch (Exception e) {
                }
            }

            selectedCategory = pieCategoryColumn.getColumnName();
            String title = chartTitle();
            pieMaker.init(message("PieChart"))
                    .setDefaultChartTitle(title + " - " + message("Count"))
                    .setDefaultCategoryLabel(selectedCategory)
                    .setDefaultValueLabel(message("Count"))
                    .setValueLabel(message("Count"))
                    .setInvalidAs(invalidAs);

            return true;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void drawChart() {
        drawXYChart();
        drawPieChart();
    }

    @Override
    public void drawXYChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            chartController.writeXYChart(outputColumns, outputData);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void drawPieChart() {
        try {
            if (pieData == null || pieData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            pieChartController.writeChart(pieColumns, pieData);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void okXYchart() {
        makeCharts(true, false);
    }

    @FXML
    @Override
    public void refreshAction() {
        makeCharts(true, true);
    }

    @Override
    public void typeChanged() {
        initChart();
        okXYchart();
    }


    /*
        static
     */
    public static Data2DGroupStatisticController open(ControlData2DLoad tableController) {
        try {
            Data2DGroupStatisticController controller = (Data2DGroupStatisticController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DGroupStatisticFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
