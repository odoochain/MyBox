package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-12
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureController_Image extends ImageViewerController {

    protected SimpleBooleanProperty imageLoaded;
    protected int newWidth, newHeight;
    protected ImageOperation operation;
    protected ImagePopController imagePopController;

    public static enum ImageOperation {
        Load, History, Saved, Recover, Clipboard, Paste, Arc, Color, Crop, Copy,
        Text, RichText, Mosaic, Convolution,
        Effects, Enhancement, Shadow, Scale2, Picture, Transform, Pen, Margins
    }

    @FXML
    protected TitledPane createPane;
    @FXML
    protected VBox mainBox;
    @FXML
    protected Tab imageTab, scopeTab, hisTab, backupTab;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected ImageView maskView;
    @FXML
    protected TextField newWidthInput, newHeightInput;
    @FXML
    protected ImageManufactureOperationsController operationsController;
    @FXML
    protected ImageManufactureScopeController scopeController;
    @FXML
    protected ImageManufactureScopesSavedController scopeSavedController;
    @FXML
    protected ImageManufactureHistory hisController;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected Button viewImageButton;

    @Override
    public void refinePane() {
        super.refinePane();
        maskView.setFitWidth(imageView.getFitWidth());
        maskView.setFitHeight(imageView.getFitHeight());
        maskView.setLayoutX(imageView.getLayoutX());
        maskView.setLayoutY(imageView.getLayoutY());
    }

    public void resetImagePane() {
        operation = null;
        scope = null;

        imageView.setRotate(0);
        imageView.setVisible(true);
        maskView.setImage(null);
        maskView.setVisible(false);
        maskView.toBack();
        initMaskControls(false);
    }

    public void imageTab() {
        tabPane.getSelectionModel().select(imageTab);
    }

    public void scopeTab() {
        tabPane.getSelectionModel().select(scopeTab);
    }

    public boolean isImageTabSelected() {
        return tabPane.getSelectionModel().getSelectedItem() == imageTab;
    }

    public boolean isScopeTabSelected() {
        return tabPane.getSelectionModel().getSelectedItem() == scopeTab;
    }

    @Override
    protected void zoomStepChanged() {
        xZoomStep = zoomStep;
        yZoomStep = zoomStep;
        scopeController.zoomStep = zoomStep;
        scopeController.xZoomStep = zoomStep;
        scopeController.yZoomStep = zoomStep;
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (!scopeController.keyEventsFilter(event)) {
                return operationsController.keyEventsFilter(event);
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean controlAltK() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                super.controlAltK();

            } else if (tab == scopeTab) {
                scopeController.controlAltK();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    @Override
    public boolean controlAltT() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                super.controlAltT();

            } else if (tab == scopeTab) {
                scopeController.controlAltT();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    @Override
    public boolean controlAlt1() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                super.controlAlt1();

            } else if (tab == scopeTab) {
                scopeController.controlAlt1();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    @Override
    public boolean controlAlt2() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                super.controlAlt2();

            } else if (tab == scopeTab) {
                scopeController.controlAlt2();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    @Override
    public boolean controlAlt3() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                super.controlAlt3();

            } else if (tab == scopeTab) {
                scopeController.controlAlt3();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    @Override
    public boolean controlAlt4() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == imageTab) {
                super.controlAlt4();

            } else if (tab == scopeTab) {
                scopeController.controlAlt4();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        super.imageClicked(event, p);
        operationsController.imageClicked(event, p);
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        operationsController.mousePressed(event);
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        operationsController.mouseDragged(event);
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        scrollPane.setPannable(true);
        operationsController.mouseReleased(event);
    }

    public void updateImage(ImageManufactureController_Image.ImageOperation operation, Image newImage) {
        updateImage(operation, null, null, newImage, -1);
    }

    public void updateImage(ImageManufactureController_Image.ImageOperation operation, Image newImage, long cost) {
        updateImage(operation, null, null, newImage, cost);
    }

    public void updateImage(ImageManufactureController_Image.ImageOperation operation, String objectType, String opType, Image newImage, long cost) {
        try {
            hisController.recordImageHistory(operation, objectType, opType, newImage);
            String info = operation == null ? "" : message(operation.name());
            if (objectType != null) {
                info += "  " + message(objectType);
            }
            if (opType != null) {
                info += "  " + message(opType);
            }
            if (cost > 0) {
                info += "  " + message("Cost") + ": " + DateTools.datetimeMsDuration(cost);
            }
            updateImage(newImage, info);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateImage(Image newImage, String info) {
        try {
            updateImage(newImage);
            scopeController.updateImage(newImage);
            resetImagePane();
            operationsController.resetOperationPanes();
            popInformation(info);
            updateLabelString(info);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    // Only update image and not reset image pane
    public void setImage(ImageManufactureController_Image.ImageOperation operation, Image newImage) {
        try {
            updateImage(newImage);
            scopeController.updateImage(newImage);
            hisController.recordImageHistory(operation, null, null, newImage);
            updateLabelsTitle();
            updateLabel(operation);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void updateLabel(ImageManufactureController_Image.ImageOperation operation) {
        updateLabelString(operation != null ? message(operation.name()) : null);
    }

    public void updateLabelString(String info) {
        try {
            if (imageLabel == null) {
                return;
            }
            imageLabel.setText(info);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
