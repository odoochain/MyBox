package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ColumnFilter;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-7-5
 * @License Apache License Version 2.0
 */
public class ControlData2DColumnFilter extends ControlData2DRowExpression {

    protected ColumnFilter columnFilter;

    @FXML
    protected CheckBox emptyCheck, zeroCheck, negativeCheck, positiveCheck,
            numberCheck, nonNumbericCheck, equalCheck, largerCheck, lessCheck, expressionCheck;
    @FXML
    protected RadioButton noneRadio, workRadio, compareNumberRadio, compareStringRadio, trueRadio, othersRadio;
    @FXML
    protected ComboBox<String> equalSelector, largerSelector, lessSelector;
    @FXML
    protected VBox conditionsBox;

    public ControlData2DColumnFilter() {
        TipsLabelKey = "ColumnFilterTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> values = new ArrayList<>();
            values.add(message("MinimumQ0"));
            values.add(message("LowerQuartile"));
            values.add(message("Median"));
            values.add(message("Mean"));
            values.add(message("UpperQuartile"));
            values.add(message("MaximumQ4"));
            values.add(message("LowerExtremeOutlierLine"));
            values.add(message("LowerMildOutlierLine"));
            values.add(message("UpperMildOutlierLine"));
            values.add(message("UpperExtremeOutlierLine"));
            values.add(message("Mode"));
            equalSelector.getItems().addAll(values);
            largerSelector.getItems().addAll(values);
            lessSelector.getItems().addAll(values);

            conditionsBox.disableProperty().bind(noneRadio.selectedProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseController parent, ControlData2DEditTable tableController) {
        try {
            baseName = parent.baseName;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(ColumnFilter columnFilter) {
        if (columnFilter == null || !columnFilter.isWork()) {
            noneRadio.fire();
            emptyCheck.setSelected(false);
            zeroCheck.setSelected(false);
            negativeCheck.setSelected(false);
            positiveCheck.setSelected(false);
            equalCheck.setSelected(false);
            largerCheck.setSelected(false);
            lessCheck.setSelected(false);
            numberCheck.setSelected(false);
            nonNumbericCheck.setSelected(false);
            expressionCheck.setSelected(false);
            equalSelector.setValue(null);
            largerSelector.setValue(null);
            lessSelector.setValue(null);
            compareNumberRadio.fire();
            scriptInput.clear();
            trueRadio.fire();
        } else {
            workRadio.fire();
            emptyCheck.setSelected(columnFilter.isEmpty());
            zeroCheck.setSelected(columnFilter.isZero());
            negativeCheck.setSelected(columnFilter.isNegative());
            positiveCheck.setSelected(columnFilter.isPositive());
            numberCheck.setSelected(columnFilter.isNumber());
            nonNumbericCheck.setSelected(columnFilter.isNonNumeric());
            equalCheck.setSelected(columnFilter.isEqual());
            equalSelector.setValue(columnFilter.value2Name(columnFilter.getEqualValue()));
            largerCheck.setSelected(columnFilter.isLarger());
            largerSelector.setValue(columnFilter.value2Name(columnFilter.getLargerValue()));
            lessCheck.setSelected(columnFilter.isLess());
            lessSelector.setValue(columnFilter.value2Name(columnFilter.getLessValue()));
            if (columnFilter.compareString) {
                compareStringRadio.fire();
            } else {
                compareNumberRadio.fire();
            }
            expressionCheck.setSelected(columnFilter.isColumnExpression());
            scriptInput.setText(columnFilter.getScript());
            if (columnFilter.reversed) {
                othersRadio.fire();
            } else {
                trueRadio.fire();
            }
        }
    }

    public ColumnFilter pickValues() {
        if (noneRadio.isSelected()) {
            columnFilter = null;
            return null;
        }
        columnFilter = ColumnFilter.create()
                .setWork(workRadio.isSelected())
                .setEmpty(emptyCheck.isSelected())
                .setZero(zeroCheck.isSelected())
                .setPositive(positiveCheck.isSelected())
                .setNegative(negativeCheck.isSelected())
                .setNumber(numberCheck.isSelected())
                .setNonNumeric(nonNumbericCheck.isSelected())
                .setEqual(equalCheck.isSelected())
                .setLarger(largerCheck.isSelected())
                .setLess(lessCheck.isSelected())
                .setCompareString(compareStringRadio.isSelected())
                .setColumnExpression(expressionCheck.isSelected());
        if (equalCheck.isSelected()) {
            columnFilter.setEqualValue(columnFilter.name2Value(equalSelector.getValue()));
        } else {
            columnFilter.setEqualValue(null);
        }
        if (largerCheck.isSelected()) {
            columnFilter.setLargerValue(columnFilter.name2Value(largerSelector.getValue()));
        } else {
            columnFilter.setLargerValue(null);
        }
        if (lessCheck.isSelected()) {
            columnFilter.setLessValue(columnFilter.name2Value(lessSelector.getValue()));
        } else {
            columnFilter.setLessValue(null);
        }
        if (expressionCheck.isSelected()) {
            columnFilter.setScript(scriptInput.getText());
            columnFilter.setReversed(othersRadio.isSelected());
        } else {
            columnFilter.setScript(null);
        }
        return columnFilter;
    }

    @Override
    protected void scriptExampleButtons(MenuController controller) {
        try {
            List<String> names = new ArrayList<>();
            String placehold = ColumnFilter.placehold();
            names.add(placehold);
            PopTools.addButtonsPane(controller, scriptInput, names);
            controller.addNode(new Separator());

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    placehold + " == 100",
                    placehold + " != 6",
                    placehold + " >= 0 && " + placehold + " <= 100 ",
                    placehold + " < 0 || " + placehold + " > 100 "
            ));

            PopTools.addButtonsPane(controller, scriptInput, Arrays.asList(
                    "'" + placehold + "'.search(/Hello/ig) >= 0",
                    "'" + placehold + "'.length > 0",
                    "'" + placehold + "'.indexOf('Hello') == 3",
                    "'" + placehold + "'.startsWith('Hello')",
                    "'" + placehold + "'.endsWith('Hello')",
                    "var array = [ 'A', 'B', 'C', 'D' ];\n"
                    + "array.includes('" + placehold + "')"
            ));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkFilter() {
        error = null;
        columnFilter = null;
        pickValues();
        if (expressionCheck.isSelected()) {
            String script = scriptInput.getText();
            if (script == null || script.isBlank()) {
                return true;
            }
            if (calculator.validateDataColumnExpression(data2D, script)) {
                TableStringValues.add(interfaceName + "Histories", script.trim());
                return true;
            } else {
                error = calculator.getError();
                return false;
            }
        }
        return true;
    }

}
