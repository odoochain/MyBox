package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-13
 * @License Apache License Version 2.0
 */
public class Data2DConvertToDataBaseController extends BaseData2DHandleController {

    @FXML
    protected Tab dataTab, logsTab, attributesTab;
    @FXML
    protected CheckBox importCheck;
    @FXML
    protected VBox dataVBox, filterVBox, attributesBox;
    @FXML
    protected ControlNewDataTable attributesController;
    @FXML
    protected Data2DConvertToDataBaseTask taskController;

    public Data2DConvertToDataBaseController() {
        baseTitle = message("ConvertToDatabaseTable");
        TipsLabelKey = message("SqlIdentifierComments");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            importCheck.setSelected(UserConfig.getBoolean(baseName + "Import", true));
            importCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Import", importCheck.isSelected());
                }
            });

            okButton = startButton;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setParameters(ControlData2DEditTable editController) {
        try {
            super.setParameters(editController);

            taskController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void sourceChanged() {
        if (tableController == null) {
            return;
        }
        super.sourceChanged();
        attributesController.nameInput.setText(data2D.shortName());
        attributesController.columnsController.setValues(data2D.columnNames());
    }

    public void columnSelected() {
        if (isSettingValues) {
            return;
        }
        checkColumns();
        attributesController.setColumns(checkedColsIndices);
    }

    @FXML
    @Override
    public void startAction() {
        taskController.startAction();
    }

    /*
        static
     */
    public static Data2DConvertToDataBaseController open(ControlData2DEditTable tableController) {
        try {
            Data2DConvertToDataBaseController controller = (Data2DConvertToDataBaseController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DConvertToDatabaseFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
