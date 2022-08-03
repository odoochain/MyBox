package mara.mybox.controller;

import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ExpressionCalculator;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class Data2DSetValuesController extends BaseData2DHandleController {

    @FXML
    protected ControlData2DSetValue valueController;

    public Data2DSetValuesController() {
        baseTitle = message("SetValues");
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            idExclude(true);
            valueController.setParameter(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceChanged() {
        super.sourceChanged();
        valueController.setData2D(data2D);
    }

    @Override
    public void refreshControls() {
        try {
            if (data2D == null) {
                return;
            }
            if (data2D.isMutiplePages()) {
                allPagesRadio.setDisable(false);
            } else {
                if (allPagesRadio.isSelected()) {
                    currentPageRadio.fire();
                }
                allPagesRadio.setDisable(true);
            }
            showPaginationPane(false);
            restoreSelections();
            checkOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        ok = valueController.checkSelection() && ok;
        okButton.setDisable(!ok);
        return ok;
    }

    @Override
    public void handleAllTask() {
        if (!tableController.checkBeforeNextAction()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    if (!data2D.isTmpData() && tableController.dataController.backupController != null
                            && tableController.dataController.backupController.isBack()) {
                        tableController.dataController.backupController.addBackup(task, data2D.getFile());
                    }
                    fillExpression();
                    data2D.startTask(task, filterController.filter);
                    ok = data2D.setValue(checkedColsIndices, valueController.value, valueController.errorContinueCheck.isSelected());
                    data2D.stopFilter();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                tableController.dataController.goPage();
                tableController.requestMouse();
                tableController.popDone();
                tabPane.getSelectionModel().select(dataTab);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
                valueController.expressionController.calculator.stop();
            }

        };
        start(task);
    }

    public boolean fillExpression() {
        if (valueController.expressionRadio.isSelected()) {
            String filled = data2D.calculateScriptStatistic(valueController.expression());
            if (filled == null || filled.isBlank()) {
                error = message("Invalid") + ": " + message("RowExpression");
                return false;
            }
            valueController.setExpression(filled);
        }
        return true;
    }

    @Override
    public boolean handleRows() {
        if (!fillExpression()) {
            return false;
        }
        return super.handleRows();
    }

    @Override
    public void ouputRows() {
        try {
            tableController.isSettingValues = true;
            if (valueController.gaussianDistributionRadio.isSelected()) {
                gaussianDistribution();
            } else if (valueController.identifyRadio.isSelected()) {
                identifyMatrix();
            } else if (valueController.upperTriangleRadio.isSelected()) {
                upperTriangleMatrix();
            } else if (valueController.lowerTriangleRadio.isSelected()) {
                lowerTriangleMatrix();
            } else {
                setValue();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
        tableController.isSettingValues = false;
    }

    @Override
    public boolean updateTable() {
        tableController.tableView.refresh();
        popDone();
        tableController.isSettingValues = false;
        tableController.tableChanged(true);
        tableController.requestMouse();
        tabPane.getSelectionModel().select(dataTab);
        return true;
    }

    public void setValue() {
        try {
            Random random = new Random();
            String script = valueController.expression();
            ExpressionCalculator calculator = valueController.expressionController.calculator;
            for (int row : filteredRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                String v = valueController.value;
                if (valueController.blankRadio.isSelected()) {
                    v = "";
                } else if (valueController.blankRadio.isSelected()) {
                    v = "";
                } else if (valueController.expressionRadio.isSelected()) {
                    if (!calculator.calculateTableRowExpression(data2D, script, values, row)) {
                        if (valueController.errorContinueCheck.isSelected()) {
                            continue;
                        } else {
                            if (data2D.getError() != null) {
                                popError(data2D.getError());
                            }
                            return;
                        }
                    }
                    v = calculator.getResult();
                }
                for (int col : checkedColsIndices) {
                    if (valueController.randomRadio.isSelected()) {
                        v = tableController.data2D.random(random, col, false);
                    } else if (valueController.randomNnRadio.isSelected()) {
                        v = tableController.data2D.random(random, col, true);
                    }
                    values.set(col + 1, v);
                }
                tableController.tableData.set(row, values);
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void gaussianDistribution() {
        try {
            float[][] m = ConvolutionKernel.makeGaussMatrix((int) filteredRowsIndices.size() / 2);
            int rowIndex = 0, colIndex;
            for (int row : filteredRowsIndices) {
                List<String> tableRow = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    try {
                        tableRow.set(col + 1, DoubleTools.format(m[rowIndex][colIndex], scale));
                    } catch (Exception e) {
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, tableRow);
                rowIndex++;
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void identifyMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : filteredRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex == colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void upperTriangleMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : filteredRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex <= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    public void lowerTriangleMatrix() {
        try {
            int rowIndex = 0, colIndex;
            for (int row : filteredRowsIndices) {
                List<String> values = tableController.tableData.get(row);
                colIndex = 0;
                for (int col : checkedColsIndices) {
                    if (rowIndex >= colIndex) {
                        values.set(col + 1, "1");
                    } else {
                        values.set(col + 1, "0");
                    }
                    colIndex++;
                }
                tableController.tableData.set(row, values);
                rowIndex++;
            }
            updateTable();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(message(e.toString()));
        }
    }

    /*
        static
     */
    public static Data2DSetValuesController open(ControlData2DEditTable tableController) {
        try {
            Data2DSetValuesController controller = (Data2DSetValuesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSetValuesFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
