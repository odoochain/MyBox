package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-12
 * @License Apache License Version 2.0
 */
public class Data2DSimpleLinearRegressionCombinationController extends BaseData2DRegressionController {

    protected SimpleLinearRegression simpleRegression;

    @FXML
    protected ControlData2DSimpleLinearRegressionTable resultsController;

    public Data2DSimpleLinearRegressionCombinationController() {
        baseTitle = message("SimpleLinearRegressionCombination");
        TipsLabelKey = "SimpleLinearRegressionCombinationTips";
        defaultScale = 8;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            resultsController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        resultsController.clear();
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    if (otherColsIndices.isEmpty()) {
                        otherColsIndices = data2D.columnIndices();
                    }
                    for (int yIndex : otherColsIndices) {
                        for (int xIndex : checkedColsIndices) {
                            if (xIndex == yIndex) {
                                continue;
                            }
                            regress(xIndex, yIndex);
                        }
                    }
                    data2D.stopFilter();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected void regress(int xIndex, int yIndex) {
                try {
                    String xName = data2D.columnName(xIndex);
                    String yName = data2D.columnName(yIndex);
                    List<Integer> dataColsIndices = new ArrayList<>();
                    dataColsIndices.add(xIndex);
                    dataColsIndices.add(yIndex);
                    simpleRegression = new SimpleLinearRegression(interceptCheck.isSelected(), xName, yName, scale);
                    if (isAllPages()) {
                        data2D.simpleLinearRegression(null, dataColsIndices, simpleRegression, false);
                    } else {
                        simpleRegression.addData(tableFiltered(dataColsIndices, true), invalidAs);
                    }
                    List<String> row = new ArrayList<>();
                    row.add(yName);
                    row.add(xName);
                    row.add(NumberTools.format(simpleRegression.getRSquare(), scale));
                    row.add(NumberTools.format(simpleRegression.getR(), scale));
                    row.add(simpleRegression.getModel());
                    row.add(NumberTools.format(simpleRegression.getSlope(), scale));
                    row.add(NumberTools.format(simpleRegression.getIntercept(), scale));
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resultsController.addRow(row);
                        }
                    });

                } catch (Exception e) {
                    error = e.toString();
                }
            }

            @Override
            protected void whenSucceeded() {
                resultsController.afterRegression();
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


    /*
        static
     */
    public static Data2DSimpleLinearRegressionCombinationController open(ControlData2DLoad tableController) {
        try {
            Data2DSimpleLinearRegressionCombinationController controller
                    = (Data2DSimpleLinearRegressionCombinationController) WindowTools.openChildStage(
                            tableController.getMyWindow(), Fxmls.Data2DSimpleLinearRegressionCombinationFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
