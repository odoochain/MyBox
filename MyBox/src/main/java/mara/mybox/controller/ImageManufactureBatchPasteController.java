package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.PixelBlend;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2021-6-22
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchPasteController extends BaseImageManufactureBatchController {

    protected int positionType, margin, posX, posY;
    protected PixelBlend.ImagesBlendMode blendMode;
    protected float opacity;
    protected BufferedImage clipSource;
    protected int rotateAngle;

    @FXML
    protected ComboBox<String> opacitySelector, blendSelector, angleSelector;
    @FXML
    protected CheckBox enlargeCheck, clipTopCheck;
    @FXML
    protected ToggleGroup positionGroup;
    @FXML
    protected TextField xInput, yInput, marginInput;
    @FXML
    protected Button demoButton;

    private class PositionType {

        static final int RightBottom = 0;
        static final int RightTop = 1;
        static final int LeftBottom = 2;
        static final int LeftTop = 3;
        static final int Center = 4;
        static final int Custom = 5;
    }

    public ImageManufactureBatchPasteController() {
        baseTitle = AppVariables.message("ImageManufactureBatchPaste");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(Bindings.isEmpty(sourceFileInput.textProperty()))
                    .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
                    .or(xInput.styleProperty().isEqualTo(badStyle))
                    .or(yInput.styleProperty().isEqualTo(badStyle))
                    .or(marginInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            rotateAngle = 0;

            enlargeCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "EnlargerImageAsClip", true));
            enlargeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue(baseName + "EnlargerImageAsClip", enlargeCheck.isSelected());
                }
            });

            clipTopCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ClipOnTop", true));
            clipTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue(baseName + "ClipOnTop", clipTopCheck.isSelected());
                }
            });

            String mode = AppVariables.getUserConfigValue(baseName + "TextBlendMode", message("NormalMode"));
            blendMode = PixelBlend.getBlendModeByName(mode);
            blendSelector.getItems().addAll(PixelBlend.allBlendModes());
            blendSelector.setValue(mode);
            blendSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    String mode = blendSelector.getSelectionModel().getSelectedItem();
                    blendMode = PixelBlend.getBlendModeByName(mode);
                    AppVariables.setUserConfigValue(baseName + "TextBlendMode", mode);
                }
            });

            opacity = AppVariables.getUserConfigInt(baseName + "Opacity", 100) / 100f;
            opacity = (opacity >= 0.0f && opacity <= 1.0f) ? opacity : 1.0f;
            opacitySelector.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacitySelector.setValue(opacity + "");
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            AppVariables.setUserConfigInt(baseName + "Opacity", (int) (f * 100));
                            FxmlControl.setEditorNormal(opacitySelector);
                        } else {
                            FxmlControl.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            angleSelector.getItems().addAll(Arrays.asList("0", "90", "180", "45", "30", "60", "15", "5", "10", "1", "75", "120", "135"));
            angleSelector.setVisibleRowCount(10);
            angleSelector.setValue("0");
            angleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.valueOf(newValue);
                        FxmlControl.setEditorNormal(angleSelector);
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(angleSelector);
                    }
                }
            });

            xInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterPosition();
                }
            });
            yInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterPosition();
                }
            });

            margin = AppVariables.getUserConfigInt(baseName + "Margin", 20);
            marginInput.setText(margin + "");
            positionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkPositionType();
                }
            });
            checkPositionType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkPositionType() {
        xInput.setDisable(true);
        xInput.setStyle(null);
        yInput.setDisable(true);
        yInput.setStyle(null);
        marginInput.setDisable(true);
        marginInput.setStyle(null);

        RadioButton selected = (RadioButton) positionGroup.getSelectedToggle();
        if (message("RightBottom").equals(selected.getText())) {
            positionType = PositionType.RightBottom;
            marginInput.setDisable(false);
            checkMargin();

        } else if (message("RightTop").equals(selected.getText())) {
            positionType = PositionType.RightTop;
            marginInput.setDisable(false);
            checkMargin();

        } else if (message("LeftBottom").equals(selected.getText())) {
            positionType = PositionType.LeftBottom;
            marginInput.setDisable(false);
            checkMargin();

        } else if (message("LeftTop").equals(selected.getText())) {
            positionType = PositionType.LeftTop;
            marginInput.setDisable(false);
            checkMargin();

        } else if (message("Center").equals(selected.getText())) {
            positionType = PositionType.Center;

        } else if (message("Custom").equals(selected.getText())) {
            positionType = PositionType.Custom;
            xInput.setDisable(false);
            yInput.setDisable(false);
            checkWaterPosition();
        }
    }

    private void checkMargin() {
        try {
            int v = Integer.valueOf(marginInput.getText());
            if (v >= 0) {
                margin = v;
                AppVariables.setUserConfigInt(baseName + "Margin", margin);
                marginInput.setStyle(null);
            } else {
                marginInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            marginInput.setStyle(badStyle);
        }

    }

    private void checkWaterPosition() {
        try {
            int v = Integer.valueOf(xInput.getText());
            if (v >= 0) {
                posX = v;
                xInput.setStyle(null);
            } else {
                xInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            xInput.setStyle(badStyle);
        }

        try {
            int v = Integer.valueOf(yInput.getText());
            if (v >= 0) {
                posY = v;
                yInput.setStyle(null);
            } else {
                yInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            yInput.setStyle(badStyle);
        }

    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }

        clipSource = ImageFileReaders.readImage(sourceFile);
        if (clipSource != null) {
            clipSource = ImageManufacture.rotateImage(clipSource, rotateAngle);
        }
        return clipSource != null;
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage bgImage = source;
            if (enlargeCheck.isSelected()) {
                if (clipSource.getWidth() > bgImage.getWidth()) {
                    bgImage = ImageManufacture.addMargins(bgImage,
                            CommonFxValues.TRANSPARENT, clipSource.getWidth() - bgImage.getWidth() + 1,
                            false, false, false, true);
                }
                if (clipSource.getHeight() > bgImage.getHeight()) {
                    bgImage = ImageManufacture.addMargins(bgImage,
                            CommonFxValues.TRANSPARENT, clipSource.getHeight() - bgImage.getHeight() + 1,
                            false, true, false, false);
                }
            }

            int x, y;
            switch (positionType) {
                case PositionType.Center:
                    x = (bgImage.getWidth() - clipSource.getWidth()) / 2;
                    y = (bgImage.getHeight() - clipSource.getHeight()) / 2;
                    break;
                case PositionType.RightBottom:
                    x = bgImage.getWidth() - 1 - margin;
                    y = bgImage.getHeight() - 1 - margin;
                    break;
                case PositionType.RightTop:
                    x = bgImage.getWidth() - 1 - margin;
                    y = margin;
                    break;
                case PositionType.LeftBottom:
                    x = margin;
                    y = bgImage.getHeight() - 1 - margin;
                    break;
                case PositionType.Custom:
                    x = posX;
                    y = posY;
                    break;
                default:
                    x = margin;
                    y = margin;
                    break;
            }

            BufferedImage target = ImageManufacture.blendImages(clipSource, bgImage, x, y,
                    blendMode, opacity, !clipTopCheck.isSelected());
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

    @FXML
    protected void demo() {
        FxmlControl.blendDemo(this, demoButton, null, null, 20, 20, opacity, !clipTopCheck.isSelected());
    }

}