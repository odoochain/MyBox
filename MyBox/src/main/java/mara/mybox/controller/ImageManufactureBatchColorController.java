package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.fxml.FxmlAdjustColorTools.ColorActionType;
import mara.mybox.fxml.FxmlAdjustColorTools.ColorObjectType;
import static mara.mybox.objects.AppVaribles.getMessage;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageAdjustColorTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-22
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchColorController extends ImageManufactureBatchController {

    private int colorValue;
    private boolean isIncrease;
    private ColorObjectType colorOperationType;
    private ColorActionType colorActionType;

    @FXML
    private ToggleGroup colorGroup, opGroup;
    @FXML
    private RadioButton setRadio, invertRadio, increaseRadio, decreaseRadio, filterRadio;
    @FXML
    private Slider colorSlider;
    @FXML
    private TextField colorInput;
    @FXML
    private Label colorUnit;

    public ImageManufactureBatchColorController() {

    }

    @Override
    protected void initializeNext2() {
        try {
            super.initOptionsSection();

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(colorInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorObjectType();
                }
            });
            checkColorObjectType();

            colorSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    colorValue = newValue.intValue();
                    colorInput.setText(colorValue + "");
                }
            });

            colorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorInput();
                }
            });
            checkColorInput();

            opGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorActionType();
                }
            });
            checkColorActionType();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkColorObjectType() {

        setRadio.setDisable(false);
        invertRadio.setDisable(false);
        filterRadio.setDisable(false);
        increaseRadio.setDisable(false);
        decreaseRadio.setDisable(false);
        setRadio.setSelected(true);
        RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
        if (getMessage("Brightness").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Brightness;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
        } else if (getMessage("Saturation").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Sauration;
            colorSlider.setMax(100);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
        } else if (getMessage("Hue").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Hue;
            colorSlider.setMax(359);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText(getMessage("Degree"));
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            filterRadio.setDisable(true);
            invertRadio.setDisable(true);
        } else if (getMessage("Red").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Red;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Green").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Green;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Blue").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Blue;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Yellow").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Yellow;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Cyan").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Cyan;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Magenta").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Magenta;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
        } else if (getMessage("Opacity").equals(selected.getText())) {
            colorOperationType = ColorObjectType.Opacity;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("50");
            }
            invertRadio.setDisable(true);
            filterRadio.setDisable(true);
            increaseRadio.setDisable(true);
            decreaseRadio.setDisable(true);
        } else if (getMessage("RGB").equals(selected.getText())) {
            colorOperationType = ColorObjectType.RGB;
            colorSlider.setMax(255);
            colorSlider.setMin(1);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            if (colorInput.getText().trim().isEmpty()) {
                colorInput.setText("10");
            }
            setRadio.setDisable(true);
            filterRadio.setDisable(true);
            invertRadio.setSelected(true);
        }
    }

    private void checkColorInput() {
        try {
            colorValue = Integer.valueOf(colorInput.getText());
            if (colorValue >= 0 && colorValue <= colorSlider.getMax()) {
                colorInput.setStyle(null);
                colorSlider.setValue(colorValue);
            } else {
                colorInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            colorInput.setStyle(badStyle);
        }
    }

    private void checkColorActionType() {
        RadioButton selected = (RadioButton) opGroup.getSelectedToggle();
        if (getMessage("Set").equals(selected.getText())) {
            colorActionType = ColorActionType.Set;
        } else if (getMessage("Increase").equals(selected.getText())) {
            colorActionType = ColorActionType.Increase;
        } else if (getMessage("Decrease").equals(selected.getText())) {
            colorActionType = ColorActionType.Decrease;
        } else if (getMessage("Filter").equals(selected.getText())) {
            colorActionType = ColorActionType.Filter;
        } else if (getMessage("Invert").equals(selected.getText())) {
            colorActionType = ColorActionType.Invert;
        } else {
            colorActionType = null;
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        if (null == colorOperationType || colorActionType == null) {
            return null;
        }
        try {
            float value = colorValue;
            if (colorActionType == ColorActionType.Decrease) {
                value = 0 - colorValue;
            }
            switch (colorOperationType) {
                case Brightness:
                case Sauration:
                    value = value / 100.0f;
                    break;
                case Opacity:
                    value = value * 255 / 100.0f;
                    break;
                case Hue:
                    value = value / 360.0f;
                    break;
                case Red:
                case Green:
                case Blue:
                case Yellow:
                case Cyan:
                case Magenta:
                case RGB:
                    break;
            }
            BufferedImage target = ImageAdjustColorTools.changeColor(source, colorOperationType, colorActionType, value);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}