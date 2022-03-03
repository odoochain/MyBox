package mara.mybox.controller;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.dev.TestCase;
import mara.mybox.dev.TestCase.Status;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.errorNotify;
import static mara.mybox.value.AppVariables.isTesting;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-6
 * @License Apache License Version 2.0
 */
public class AutoTestingExecutionController extends BaseTableViewController<TestCase> {

    protected AutoTestingCasesController casesController;
    protected int currentIndex, interval = 2000;
    protected TestCase currentCase;
    protected List<TestCase> testCases;
    protected boolean canceled;
    protected ChangeListener<Boolean> errorListener;

    @FXML
    protected TableColumn<TestCase, Integer> aidColumn;
    @FXML
    protected TableColumn<TestCase, String> typeColumn, operationColumn, objectColumn, versionColumn, stageColumn, statusColumn;

    public AutoTestingExecutionController() {
        baseTitle = Languages.message("TestExecution");
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            aidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeName"));
            operationColumn.setCellValueFactory(new PropertyValueFactory<>("operationName"));
            objectColumn.setCellValueFactory(new PropertyValueFactory<>("object"));
            versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
            stageColumn.setCellValueFactory(new PropertyValueFactory<>("stage"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusName"));

            tableView.setRowFactory((TableView<TestCase> param) -> {
                try {
                    return new TableRow<TestCase>() {
                        @Override
                        protected void updateItem(TestCase item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setGraphic(null);
                                setText(null);
                                setTextFill(null);
                                setStyle(null);
                                return;
                            }
                            if (this.isSelected()) {
                                setStyle(NodeStyleTools.selectedData);
                            } else {
                                switch (item.getStatus()) {
                                    case Success:
                                        setStyle("-fx-background-color: honeydew");
                                        break;
                                    case Fail:
                                        setStyle("-fx-background-color: aliceblue");
                                        break;
                                    case Testing:
                                        setStyle("-fx-background-color: lightyellow");
                                        break;
                                    default:
                                        setStyle(null);
                                }
                            }
                        }
                    };
                } catch (Exception e) {
                    return null;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            errorListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (isTesting && currentCase != null && currentIndex >= 0) {
                        currentCase.setStatus(Status.Fail);
                        tableData.set(currentIndex, currentCase);
                    }
                }
            };

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(AutoTestingCasesController parent, List<TestCase> testCases) {
        try {
            this.casesController = parent;
            this.testCases = testCases;
            tableData.setAll(testCases);

            startAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        errorNotify.removeListener(errorListener);
        if (startButton.getUserData() != null) {
            stopCases();
            return;
        }
        AppVariables.isTesting = true;
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        startButton.setUserData("started");
        canceled = false;
        errorNotify.addListener(errorListener);

        Window window = getMyWindow();
        window.setX(0);
        window.setY(0);
        currentIndex = 0;
        for (TestCase testCase : tableData) {
            testCase.setStatus(Status.NotTested);
        }
        tableView.refresh();
        goCurrentCase();
    }

    public void stopCases() {
        canceled = true;
        errorNotify.removeListener(errorListener);
        currentIndex = -1;
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        startButton.setUserData(null);
        AppVariables.isTesting = false;
    }

    public void goCurrentCase() {
        try {
            currentCase = null;
            if (canceled || testCases == null || currentIndex < 0 || currentIndex >= testCases.size()) {
                stopCases();
                return;
            }
            AppVariables.isTesting = true;
            currentCase = tableData.get(currentIndex);
            currentCase.setStatus(Status.Testing);
            tableData.set(currentIndex, currentCase);
            tableView.scrollTo(currentIndex);
            runCurrentCase();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void runCurrentCase() {
        try {
            if (canceled || currentCase == null) {
                return;
            }
            BaseController currentController = openStage(currentCase.getFxml());

            if (currentController == null) {
                currentCase.setStatus(Status.Fail);
                tableData.set(currentIndex, currentCase);
            } else {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (currentController != null) {
                                currentController.close();
                            }
                            if (currentCase != null && currentCase.getStatus() != Status.Fail) {
                                currentCase.setStatus(Status.Success);
                                tableData.set(currentIndex, currentCase);
                            }
                            if (canceled) {
                                return;
                            }
                            currentIndex++;
                            goCurrentCase();
                        });
                    }
                }, interval);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            if (currentCase != null) {
                currentCase.setStatus(Status.Fail);
                tableData.set(currentIndex, currentCase);
            }
        }

    }

    @Override
    public void cleanPane() {
        try {
            errorNotify.removeListener(errorListener);
            errorListener = null;
            casesController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static
     */
    public static AutoTestingExecutionController open(AutoTestingCasesController parent, List<TestCase> testCases) {
        try {
            AutoTestingExecutionController controller = (AutoTestingExecutionController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.AutoTestingExecutionFxml, false);
            controller.setParameters(parent, testCases);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
