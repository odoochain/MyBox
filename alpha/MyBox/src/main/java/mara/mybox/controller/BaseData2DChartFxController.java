package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.chart.Chart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.chart.ChartTools.LabelType;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartFxController extends BaseData2DChartController {

    protected LabelType labelType;
    protected int tickFontSize, titleFontSize, labelFontSize;
    protected Side titleSide, legendSide;
    protected Chart chart;

    @FXML
    protected TextField titleInput;
    @FXML
    protected ComboBox<String> titleFontSizeSelector;
    @FXML
    protected CheckBox autoTitleCheck, popLabelCheck, animatedCheck;
    @FXML
    protected ToggleGroup titleSideGroup, labelGroup, legendGroup;

    public abstract void makeChart();

    public abstract void writeChartData();

    @Override
    public void initControls() {
        try {
            super.initControls();

            initPlotTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initPlotTab() {
        try {
            tickFontSize = UserConfig.getInt(baseName + "TickFontSize", 12);
            if (tickFontSize < 0) {
                tickFontSize = 12;
            }

            labelType = LabelType.Value;
            labelGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    if (message("NameAndValue").equals(value)) {
                        labelType = LabelType.NameAndValue;
                    } else if (message("Value").equals(value)) {
                        labelType = LabelType.Value;
                    } else if (message("Name").equals(value)) {
                        labelType = LabelType.Name;
                    } else if (message("Point").equals(value)) {
                        labelType = LabelType.Point;
                    } else if (message("NotDisplay").equals(value)) {
                        labelType = LabelType.NotDisplay;
                    } else {
                        labelType = LabelType.NameAndValue;
                    }
                    redrawChart();
                }
            });

            popLabelCheck.setSelected(UserConfig.getBoolean(baseName + "PopLabel", true));
            popLabelCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "PopLabel", popLabelCheck.isSelected());
                    if (chart != null) {
                        redrawChart();
                    }
                }
            });

            autoTitleCheck.setSelected(UserConfig.getBoolean(baseName + "AutoTitle", true));
            autoTitleCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "AutoTitle", autoTitleCheck.isSelected());
                checkAutoTitle();
            });

            titleFontSize = UserConfig.getInt(baseName + "TitleFontSize", 12);
            if (titleFontSize < 0) {
                titleFontSize = 12;
            }
            titleFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            titleFontSizeSelector.getSelectionModel().select(titleFontSize + "");
            titleFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            titleFontSize = v;
                            titleFontSizeSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "TitleFontSize", titleFontSize);
                            if (chart != null) {
                                redrawChart();
                            }
                        } else {
                            titleFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        titleFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            labelFontSize = UserConfig.getInt(baseName + "LabelFontSize", 12);
            if (labelFontSize < 0) {
                labelFontSize = 2;
            }

            titleSide = Side.TOP;
            titleSideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    if (message("Top").equals(value)) {
                        titleSide = Side.TOP;
                    } else if (message("Bottom").equals(value)) {
                        titleSide = Side.BOTTOM;
                    } else if (message("Left").equals(value)) {
                        titleSide = Side.LEFT;
                    } else if (message("Right").equals(value)) {
                        titleSide = Side.RIGHT;
                    }
                    if (chart != null) {
                        chart.setTitleSide(titleSide);
                        chart.requestLayout();
                        setChartStyle();
                    }
                }
            });

            legendSide = Side.TOP;
            legendGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        String value = ((RadioButton) newValue).getText();
                        if (message("NotDisplay").equals(value)) {
                            legendSide = null;
                        } else if (message("Left").equals(value)) {
                            legendSide = Side.LEFT;
                        } else if (message("Top").equals(value)) {
                            legendSide = Side.TOP;
                        } else if (message("Bottom").equals(value)) {
                            legendSide = Side.BOTTOM;
                        } else {
                            legendSide = Side.RIGHT;
                        }
                        if (chart != null) {
                            if (legendSide == null) {
                                chart.setLegendVisible(false);
                            } else {
                                chart.setLegendVisible(true);
                                chart.setLegendSide(legendSide);
                            }
                            chart.requestLayout();
                            setChartStyle();
                        }
                    });

            animatedCheck.setSelected(UserConfig.getBoolean(baseName + "Animated", false));
            animatedCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Animated", animatedCheck.isSelected());
                if (chart != null) {
                    chart.setAnimated(animatedCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkAutoTitle() {
        try {
            if (autoTitleCheck.isSelected()) {
                defaultTitle();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterInit() {
        checkChartType();
        okAction();
    }

    public String categoryName() {
        return categoryColumnSelector.getSelectionModel().getSelectedItem();
    }

    public String valueName() {
        return valueColumnSelector.getSelectionModel().getSelectedItem();
    }

    public boolean isCategoryNumbers() {
        return false;
    }

    public String title() {
        String prefix = categoryName() + " - ";
        if (valuesNames() != null) {
            return prefix + valuesNames();
        } else {
            return prefix + valueName();
        }
    }

    public String valuesNames() {
        try {
            return sourceController.checkedColsNames().toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String numberName(int index) {
        try {
            return sourceController.checkedColsNames().get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean displayLabel() {
        return labelType != null && labelType != LabelType.NotDisplay;
    }

    public void checkChartType() {
        try {
            setSourceLabel(message("SelectRowsColumnsToHanlde"));
            checkAutoTitle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        checkAutoTitle();
        return super.checkOptions();
    }

    @Override
    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            clearChart();
            makeChart();
            writeChartData();
            setChartStyle();

            loadDataHtml();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeFinalChart() {
        try {
            if (chart == null) {
                return;
            }
            chart.setStyle("-fx-font-size: " + titleFontSize + "px; -fx-tick-label-font-size: " + tickFontSize + "px; ");

            chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(chart, Priority.ALWAYS);
            HBox.setHgrow(chart, Priority.ALWAYS);
            chart.setAnimated(animatedCheck.isSelected());
            chart.setTitle(titleInput.getText());
            chart.setTitleSide(titleSide);
            chartPane.getChildren().add(chart);
            AnchorPane.setTopAnchor(chart, 2d);
            AnchorPane.setBottomAnchor​(chart, 2d);
            AnchorPane.setLeftAnchor(chart, 2d);
            AnchorPane.setRightAnchor​(chart, 2d);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void defaultTitle() {
        titleInput.setText(title());
    }

    @FXML
    public void goTitle() {
        if (chart != null) {
            chart.setTitle(titleInput.getText());
        }
    }

    @Override
    public String chartTitle() {
        return titleInput.getText();
    }

    @Override
    public void clearChart() {
        chart = null;
        chartPane.getChildren().clear();
    }

    @FXML
    public void paneSize() {
        if (chartPane == null || chart == null) {
            return;
        }
        try {
            AnchorPane.setTopAnchor(chart, 2d);
            AnchorPane.setBottomAnchor​(chart, 2d);
            AnchorPane.setLeftAnchor(chart, 2d);
            AnchorPane.setRightAnchor​(chart, 2d);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void zoomIn() {
        if (chartPane == null || chart == null) {
            return;
        }
        try {
            Bounds bounds = chart.getBoundsInLocal();
            AnchorPane.clearConstraints(chart);
            chart.setPrefSize(bounds.getWidth() + 40, bounds.getHeight() + 40);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public Image snapChart() {
        return NodeTools.snap(chart);
    }

    @Override
    public boolean controlAlt2() {
        paneSize();
        return true;
    }

    @Override
    public boolean controlAlt3() {
        zoomIn();
        return true;
    }

    /*
        get/set
     */
    public CheckBox getPopLabelCheck() {
        return popLabelCheck;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public int getLabelFontSize() {
        return labelFontSize;
    }

}
