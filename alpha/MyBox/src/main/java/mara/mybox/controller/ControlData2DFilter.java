package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.db.data.TreeNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-6-1
 * @License Apache License Version 2.0
 */
public class ControlData2DFilter extends ControlData2DRowExpression {

    protected long maxData = -1;
    protected DataFilter filter;

    @FXML
    protected RadioButton trueRadio, othersRadio;
    @FXML
    protected TextField maxInput;

    public ControlData2DFilter() {
        TipsLabelKey = "RowFilterTips";
        category = TreeNode.RowFilter;
    }

    @Override
    public void initCalculator() {
        filter = new DataFilter();
        calculator = filter.calculator;
    }

    public void setParameters(BaseController parent) {
        try {
            baseName = parent.baseName;

            if (maxInput != null) {
                maxData = UserConfig.getLong(baseName + "MaxDataNumber", -1);
                if (maxData > 0) {
                    maxInput.setText(maxData + "");
                }
                maxInput.setStyle(null);
                maxInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        String maxs = maxInput.getText();
                        if (maxs == null || maxs.isBlank()) {
                            maxData = -1;
                            maxInput.setStyle(null);
                            UserConfig.setLong(baseName + "MaxDataNumber", -1);
                        } else {
                            try {
                                maxData = Long.parseLong(maxs);
                                maxInput.setStyle(null);
                                UserConfig.setLong(baseName + "MaxDataNumber", maxData);
                            } catch (Exception e) {
                                maxInput.setStyle(UserConfig.badStyle());
                            }
                        }
                    }
                });
            } else {
                maxData = -1;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setData2D(Data2D data2D) {
        super.setData2D(data2D);
        data2D.filter = filter;
    }

    @Override
    protected void editNode(TreeItem<TreeNode> item) {
        if (item == null) {
            return;
        }
        TreeNode node = item.getValue();
        if (node == null) {
            return;
        }
        scriptInput.setText(node.getValue());
        maxInput.setText("");
        trueRadio.setSelected(true);
        String more = node.getMore();
        if (more != null && more.contains(TreeNode.TagsSeparater)) {
            try {
                String[] v = more.strip().split(TreeNode.TagsSeparater);
                if (StringTools.isFalse(v[0])) {
                    othersRadio.setSelected(true);
                } else {
                    trueRadio.setSelected(true);
                }
                long max = Long.parseLong(v[1]);
                if (max > 0) {
                    maxInput.setText(max + "");
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void clear() {
        isSettingValues = true;
        scriptInput.clear();
        maxInput.clear();
        placeholdersList.getItems().clear();
        trueRadio.setSelected(true);
        isSettingValues = false;
        filter.clear();
    }

    public void load(String script, boolean isTrue) {
        load(script, isTrue, -1);
    }

    public void load(String script, boolean isTrue, long max) {
        clear();
        isSettingValues = true;
        if (script == null || script.isBlank()) {
            scriptInput.clear();
            trueRadio.setSelected(true);
        } else {
            scriptInput.setText(script);
            if (isTrue) {
                trueRadio.setSelected(true);
            } else {
                othersRadio.setSelected(true);
            }
        }
        maxInput.setText(max > 0 ? max + "" : "");
        isSettingValues = false;
        filter.setSourceScript(script);
        filter.setReversed(!isTrue);
        filter.setMaxPassed(max);
    }

    public void load(Data2D data2D, DataFilter filter) {
        if (filter == null) {
            clear();
            return;
        }
        load(filter.getSourceScript(), !filter.isReversed(), filter.getMaxPassed());
        setData2D(data2D);
    }

    public DataFilter pickValues() {
        filter.setReversed(othersRadio.isSelected())
                .setMaxPassed(maxData).setPassedNumber(0)
                .setSourceScript(scriptInput.getText());
        if (data2D != null) {
            data2D.setFilter(filter);
        }
        return filter;
    }

    @Override
    public boolean checkExpression(boolean allPages) {
        if (!super.checkExpression(allPages)) {
            return false;
        }
        if (maxInput != null && UserConfig.badStyle().equals(maxInput.getStyle())) {
            error = message("InvalidParameter") + ": " + message("MaxSourceDataTake");
            return false;
        }
        pickValues();
        return true;
    }

    @FXML
    @Override
    public void editAction() {
        RowFilterController.open(scriptInput.getText(), trueRadio.isSelected(), maxData);
    }

    @FXML
    @Override
    public void dataAction() {
        RowFilterController.open();
    }

}
