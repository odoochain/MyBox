package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.TmpTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-9
 * @License Apache License Version 2.0
 */
public class Data2DTransposeController extends BaseData2DTargetsController {

    @FXML
    protected CheckBox firstCheck;

    public Data2DTransposeController() {
        baseTitle = message("Transpose");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            firstCheck.setSelected(UserConfig.getBoolean(baseName + "FirstAsNames", false));
            firstCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "FirstAsNames", firstCheck.isSelected());
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean handleRows() {
        try {
            boolean showRowNumber = showRowNumber();
            outputData = tableFiltered(showRowNumber);
            if (outputData == null) {
                return false;
            }
            boolean showColNames = showColNames();
            int rowsNumber = outputData.size(), columnsNumber = outputData.get(0).size();
            outputColumns = new ArrayList<>();
            int nameIndex = showRowNumber ? 1 : 0;
            List<String> names = new ArrayList<>();
            if (firstCheck.isSelected()) {
                for (int c = 0; c < rowsNumber; ++c) {
                    String name = outputData.get(c).get(nameIndex);
                    if (name == null || name.isBlank()) {
                        name = message("Columns") + (c + 1);
                    }
                    DerbyBase.checkIdentifier(names, name, true);
                }
            } else {
                for (int c = 1; c <= rowsNumber; c++) {
                    names.add(message("Column") + c);
                }
            }
            if (showColNames) {
                DerbyBase.checkIdentifier(names, message("ColumnName"), true);
            }
            for (int c = 0; c < names.size(); c++) {
                outputColumns.add(new Data2DColumn(names.get(c), ColumnDefinition.ColumnType.String));
            }

            List<List<String>> transposed = new ArrayList<>();
            for (int c = 0; c < columnsNumber; ++c) {
                List<String> row = new ArrayList<>();
                if (showColNames) {
                    if (showRowNumber) {
                        if (c == 0) {
                            row.add(message("SourceRowNumber"));
                        } else {
                            row.add(checkedColsNames.get(c - 1));
                        }
                    } else {
                        row.add(checkedColsNames.get(c));
                    }
                }
                for (int r = 0; r < rowsNumber; ++r) {
                    row.add(outputData.get(r).get(c));
                }
                transposed.add(row);
            }
            outputData = transposed;

            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        try {
            Data2D tmp2D = data2D.cloneAll();
            tmp2D.startTask(task, filterController.filter);
            if (task != null) {
                task.setInfo(message("Filter") + "...");
            }
            TmpTable tmpTable = new TmpTable()
                    .setSourceData(tmp2D)
                    .setTargetName(data2D.getDataName())
                    .setSourcePickIndice(checkedColsIndices)
                    .setImportData(true)
                    .setForStatistic(false)
                    .setIncludeColName(showColNames())
                    .setIncludeRowNumber(showRowNumber())
                    .setInvalidAs(invalidAs);
            tmpTable.setTask(task);
            if (!tmpTable.createTable()) {
                tmpTable = null;
            }
            tmp2D.stopFilter();
            if (tmpTable == null) {
                return null;
            }
            DataFileCSV csvData = tmpTable.transpose(firstCheck.isSelected());
            tmpTable.drop();
            return csvData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        static
     */
    public static Data2DTransposeController open(ControlData2DLoad tableController) {
        try {
            Data2DTransposeController controller = (Data2DTransposeController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DTransposeFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
