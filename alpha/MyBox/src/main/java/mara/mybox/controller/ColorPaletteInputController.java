package mara.mybox.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-5
 * @License Apache License Version 2.0
 */
public class ColorPaletteInputController extends BaseChildController {

    protected ColorPalettePopupController paletteManager;

    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected TextField colorInput;

    public ColorPaletteInputController() {
        baseTitle = message("InputColors");
    }

    @Override
    public void initControls() {
        try {
            colorPicker.valueProperty().addListener((ObservableValue<? extends Color> ov, Color oldVal, Color newVal) -> {
                if (isSettingValues || newVal == null) {
                    return;
                }
                colorInput.setText(newVal.toString());
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(ColorPalettePopupController palette) {
        try {
            this.paletteManager = palette;

            getMyStage().sizeToScene();
            myStage.setX(paletteManager.getMyWindow().getX());
            myStage.setY(paletteManager.getMyWindow().getY() + 60);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void popColorHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, colorInput, mouseEvent, "ColorQueryColorHistories", true);
    }

    @FXML
    public void popExamples(MouseEvent mouseEvent) {
        PopTools.popColorExamples(this, colorInput, mouseEvent);
    }

    public ColorData getInputColor() {
        try {
            String value = colorInput.getText();
            if (value == null || value.isBlank()) {
                popError(message("InvalidParameters") + ": " + message("Color"));
                return null;
            }
            ColorData colorData = new ColorData(value).calculate();
            if (colorData.getSrgb() == null) {
                popError(message("InvalidParameters") + ": " + message("Color"));
                return null;
            }
            TableStringValues.add("ColorQueryColorHistories", value);
            return colorData;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void okAction() {
        ColorData colorData = getInputColor();
        if (colorData != null) {
            paletteManager.takeColor(colorData);
            close();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        paletteManager.addColor(getInputColor());
        close();
    }

    @FXML
    public void queryAction() {
        openStage(Fxmls.ColorQueryFxml);
    }


    /*
        static methods
     */
    public static ColorPaletteInputController open(ColorPalettePopupController palette) {
        ColorPaletteInputController controller = (ColorPaletteInputController) WindowTools.openChildStage(
                palette.parentController.getMyStage(), Fxmls.ColorPaletteInputFxml, false);
        controller.setParameters(palette);
        return controller;
    }
}
