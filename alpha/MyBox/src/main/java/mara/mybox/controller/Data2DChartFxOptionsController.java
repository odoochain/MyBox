package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.Chart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.chart.ChartOptions;
import mara.mybox.fxml.chart.ChartOptions.LabelType;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-11
 * @License Apache License Version 2.0
 */
public class Data2DChartFxOptionsController extends BaseController {

    protected ControlData2DChartFx chartController;
    protected ChartOptions options;
    protected Chart chart;

    @FXML
    protected Tab dataTab, plotTab;
    @FXML
    protected Label titleLabel;
    @FXML
    protected ToggleGroup labelGroup, titleSideGroup, legendGroup;
    @FXML
    protected CheckBox popLabelCheck, nameCheck, animatedCheck;
    @FXML
    protected RadioButton pointRadio, valueRadio, categoryValueRadio, categoryRadio, noRadio;
    @FXML
    protected ComboBox<String> titleFontSizeSelector, labelFontSizeSelector;
    @FXML
    protected TextField titleInput;

    public Data2DChartFxOptionsController() {
    }

    /*
        data
     */
    public void initDataTab() {
        try {
            labelGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkLabelType();
                }
            });

            popLabelCheck.setSelected(options.isPopLabel());
            popLabelCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    options.setPopLabel(popLabelCheck.isSelected());
                    chartController.drawChart();
                }
            });

            nameCheck.setSelected(options.isDisplayLabelName());
            nameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    options.setDisplayLabelName(nameCheck.isSelected());
                    chartController.drawChart();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkLabelType() {
        try {
            if (isSettingValues) {
                return;
            }
            LabelType labelType = LabelType.Point;
            if (categoryValueRadio.isSelected()) {
                labelType = LabelType.CategoryAndValue;

            } else if (valueRadio.isSelected()) {
                labelType = LabelType.Value;

            } else if (categoryRadio.isSelected()) {
                labelType = LabelType.Category;

            } else if (noRadio.isSelected()) {
                labelType = LabelType.NotDisplay;

            }

            options.setLabelType(labelType);

            chartController.drawChart();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initLabelType(String defaultLabel) {
        try {
            String type = options.getLabelType().name();
            isSettingValues = true;
            switch (type) {
                case "CategoryAndValue":
                    categoryValueRadio.fire();
                    break;
                case "Value":
                    valueRadio.fire();
                    break;
                case "Category":
                    categoryRadio.fire();
                    break;
                case "Point":
                    pointRadio.fire();
                    break;
                case "NotDisplay":
                    noRadio.fire();
                    break;
            }
            isSettingValues = false;
            checkLabelType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        plot
     */
    public void initPlotTab() {
        try {
            titleLabel.setText(chartController.baseTitle);

            int titleFontSize = options.getTitleFontSize();
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
                            options.setTitleFontSize(v);
                            titleFontSizeSelector.getEditor().setStyle(null);
                            chartController.drawChart();
                        } else {
                            titleFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        titleFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            NodeTools.setRadioSelected(titleSideGroup, options.getTitleSide().name());
            titleSideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    options.setTitleSide(Side.LEFT);
                    if (message("Top").equals(value)) {
                        options.setTitleSide(Side.TOP);
                    } else if (message("Bottom").equals(value)) {
                        options.setTitleSide(Side.BOTTOM);
                    } else if (message("Left").equals(value)) {
                        options.setTitleSide(Side.LEFT);
                    } else if (message("Right").equals(value)) {
                        options.setTitleSide(Side.RIGHT);
                    }
                }
            });

            if (options.getLegendSide() == null) {
                NodeTools.setRadioSelected(legendGroup, message("NotDisplay"));
            } else {
                NodeTools.setRadioSelected(legendGroup, options.getLegendSide().name());
            }
            legendGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                if (isSettingValues || newValue == null) {
                    return;
                }
                String value = ((RadioButton) newValue).getText();
                Side side = null;
                if (message("Left").equals(value)) {
                    side = Side.LEFT;
                } else if (message("Top").equals(value)) {
                    side = Side.TOP;
                } else if (message("Bottom").equals(value)) {
                    side = Side.BOTTOM;
                } else if (message("Right").equals(value)) {
                    side = Side.RIGHT;
                }
                options.setLegendSide(side);
            });

            animatedCheck.setSelected(options.isPlotAnimated());
            animatedCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                options.setPlotAnimated(animatedCheck.isSelected());
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void defaultTitle() {
//        titleInput.setText(chartController.titleName());
    }

    @FXML
    public void goTitle() {
        chart.setTitle(titleInput.getText());
//        chartController.chartTitle = titleInput.getText();
    }

}
