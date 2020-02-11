package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-11-16
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifEditerController extends ImagesListController {

    protected int currentIndex, width, height;
    private boolean keepSize;

    @FXML
    protected ToggleGroup sizeGroup;
    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    private CheckBox loopCheck;

    public ImageGifEditerController() {
        baseTitle = AppVariables.message("ImageGifEditer");

        SourceFileType = VisitHistory.FileType.Gif;
        SourcePathType = VisitHistory.FileType.Gif;
        TargetFileType = VisitHistory.FileType.Gif;
        TargetPathType = VisitHistory.FileType.Gif;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        sourceExtensionFilter = CommonFxValues.GifExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initOptionsSection() {
        try {

            optionsBox.setDisable(true);
            tableBox.setDisable(true);

            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSizeType();
                }
            });
            checkSizeType();

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSize();
                }
            });

            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSize();
                }
            });

            saveButton.disableProperty().bind(
                    Bindings.isEmpty(tableData)
                            .or(widthInput.styleProperty().isEqualTo(badStyle))
                            .or(heightInput.styleProperty().isEqualTo(badStyle))
            );

            saveAsButton.disableProperty().bind(
                    saveButton.disableProperty()
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkSizeType() {
        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (message("KeepImagesSize").equals(selected.getText())) {
            keepSize = true;
            widthInput.setStyle(null);
            heightInput.setStyle(null);
        } else if (message("AllSetAs").equals(selected.getText())) {
            keepSize = false;
            checkSize();
        }
    }

    private void checkSize() {
        try {
            int v = Integer.valueOf(widthInput.getText());
            if (v > 0) {
                width = v;
                widthInput.setStyle(null);
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }

        try {
            int v = Integer.valueOf(heightInput.getText());
            if (v > 0) {
                height = v;
                heightInput.setStyle(null);
            } else {
                heightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            heightInput.setStyle(badStyle);
        }

    }

    @Override
    public void saveFileDo(final File outFile) {

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String ret;

                @Override
                protected boolean handle() {
                    ret = ImageGifFile.writeImages(tableData, outFile,
                            loopCheck.isSelected(), keepSize, width, height);
                    if (ret.isEmpty()) {
                        return true;
                    } else {
                        error = ret;
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    if (outFile.equals(sourceFile)) {
                        setImageChanged(false);
                    }
                    if (viewCheck.isSelected()) {
                        try {
                            final ImageGifViewerController controller
                                    = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                            controller.loadImage(outFile.getAbsolutePath());
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

}
