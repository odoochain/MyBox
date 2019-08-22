package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.controller.base.ImageManufactureController;
import mara.mybox.data.DoublePenLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.ListImageCell;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageScope.ScopeType;
import mara.mybox.image.PixelBlend;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-05
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureDoodleController extends ImageManufactureController {

    private String pix;
    private DoodleType opType;
    ObservableList<String> pixList = FXCollections.observableArrayList();
    private float opacity;
    private PixelBlend.ImagesBlendMode blendMode;
    private Image picture;
    private ScopeType shapeType;
    private int strokeWidth, arcWidth;
    private double lastX, lastY;

    @FXML
    private ToggleGroup doodleGroup;
    @FXML
    private VBox barsBox;
    @FXML
    private HBox setBox1, setBox2, setBox3;
    @FXML
    private ComboBox<Image> pixBox;
    @FXML
    private ComboBox<String> shapesBox, strokeWidthBox, strokeTypeBox, blendBox, opacityBox, arcBox;
    @FXML
    private Label blendLabel, opacityLabel, shapeLabel, strokeWidthLabel,
            strokeColorLabel, arcLabel, fitLabel;
    @FXML
    private Button pixSelectButton, withdrawButton, clearButton;
    @FXML
    private CheckBox keepRatioCheck, fillCheck, dottedCheck;
    @FXML
    private ImageView picView, maskImageView, shapeTipsView;
    @FXML
    private ColorPicker fillColorPicker;
    @FXML
    private ToggleButton pickFillColorButton;
    @FXML
    private RadioButton pictureRadio;

    public ImageManufactureDoodleController() {
    }

    public enum DoodleType {
        Shape, Picture, Pen, Line
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initDoodleTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();

        maskImageView.setVisible(false);
        lastX = lastY = -1;
        maskLineData = new DoublePolyline();
        maskLineLines = new ArrayList();
        maskPenData = new DoublePenLines();
        maskPenLines = new ArrayList();
        setBox1.getChildren().clear();
        setBox2.getChildren().clear();

        isSettingValues = true;
        fillColorPicker.setValue(Color.web(AppVaribles.getUserConfigValue("ImageDoodleFillColor", "#FFFFFF")));
        colorPicker.setValue(Color.web(AppVaribles.getUserConfigValue("ImageDoodleStrokerColor", "#FF0000")));
        dottedCheck.setSelected(AppVaribles.getUserConfigBoolean("ImageDoodleDotted", false));
        isSettingValues = false;

        opacityBox.getSelectionModel().select(0);
        blendBox.getSelectionModel().select(0);

        FxmlControl.setTooltip(clearButton, new Tooltip("Delete / CTRL+d"));
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "d":
                case "D":
                    if (clearButton != null) {
                        clearAction();
                    }
                    break;
                default:
                    break;
            }
        } else {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case DELETE:
                        if (clearButton != null) {
                            clearAction();
                        }
                        break;
                }
            }
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;
            tabPane.getSelectionModel().select(doodleTab);

            strokeWidthBox.getItems().clear();
            List<String> ws = new ArrayList();
            ws.addAll(Arrays.asList("3", "0", "1", "2", "5", "8"));
            int max = (int) (getImageWidth() / 20);
            int step = max / 10;
            for (int w = 10; w < max; w += step) {
                if (!ws.contains(w + "")) {
                    ws.add(w + "");
                }
            }
            strokeWidthBox.getItems().addAll(ws);

            arcBox.getItems().clear();
            List<String> as = new ArrayList();
            as.addAll(Arrays.asList("0", "20", "50", "100", "10", "15", "30", "150", "200"));
            max = (int) (getImageWidth() / 3);
            step = max / 10;
            for (int a = 10; a < max; a += step) {
                if (!as.contains(a + "")) {
                    as.add(a + "");
                }
            }
            arcBox.getItems().addAll(as);

            strokeWidthBox.getSelectionModel().select(AppVaribles.getUserConfigValue("ImageDoodleStrokerWidth", "3"));
            arcBox.getSelectionModel().select(AppVaribles.getUserConfigValue("ImageDoodleArcWidth", "0"));

            isSettingValues = false;

            if (values.isIsPaste()) {
                pasteAction();
            } else {
                checkDoodleType();
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initDoodleTab() {
        try {
            doodleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkDoodleType();
                }
            });

            List<Image> prePixList = Arrays.asList(
                    new Image("img/bee1.png"), new Image("img/buttefly1.png"), new Image("img/flower1.png"),
                    new Image("img/flower2.png"), new Image("img/flower3.png"), new Image("img/insect1.png"),
                    new Image("img/insect2.png"), new Image("img/p1.png"), new Image("img/p2.png"),
                    new Image("img/p3.png"), new Image("img/mybox.png"), new Image("img/About.png"),
                    new Image("img/DesktopTools.png"), new Image("img/FileTools.png"), new Image("img/ImageTools.png"),
                    new Image("img/PdfTools.png"), new Image("img/language.png"), new Image("img/position.png")
            );
            pixBox.getItems().addAll(prePixList);
            pixBox.setButtonCell(new ListImageCell());
            pixBox.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
                @Override
                public ListCell<Image> call(ListView<Image> param) {
                    return new ListImageCell();
                }
            });
            pixBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Image>() {
                @Override
                public void changed(ObservableValue ov, Image oldValue, Image newValue) {
                    picView.setImage(newValue);
                    picView.requestFocus();
                    picture = newValue;
                    if (isSettingValues) {
                        return;
                    }
                    drawMaskPicture();
                }
            });

            blendBox.getItems().addAll(PixelBlend.allBlendModes());
            blendBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    blendMode = PixelBlend.getBlendModeByName(newValue);
                    drawMaskPicture();
                    opacityBox.setDisable(blendMode != PixelBlend.ImagesBlendMode.NORMAL);
                }
            });

            opacityBox.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (f >= 0.0f && f <= 1.0f) {
                            opacity = f;
                            drawMaskPicture();
                            FxmlControl.setEditorNormal(opacityBox);
                        } else {
                            FxmlControl.setEditorBadStyle(opacityBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacityBox);
                    }
                }
            });

            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    drawMaskPicture();
                }
            });

            List<String> shapesList = Arrays.asList(message("Rectangle"), message("Circle"), message("Ellipse"), message("Polygon")
            );
            shapesBox.getItems().addAll(shapesList);
            shapesBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkShapeType();

                }
            });

            strokeWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            strokeWidth = v;
                            AppVaribles.setUserConfigInt("ImageDoodleStrokerWidth", strokeWidth);
                            if (!isSettingValues) {
                                updateMask();
                            }
                            FxmlControl.setEditorNormal(strokeWidthBox);
                        } else {
                            FxmlControl.setEditorBadStyle(strokeWidthBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(strokeWidthBox);
                    }
                }
            });

            arcBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            arcWidth = v;
                            AppVaribles.setUserConfigInt("ImageDoodleArcWidth", arcWidth);
                            if (!isSettingValues) {
                                updateShape();
                            }
                            FxmlControl.setEditorNormal(arcBox);
                        } else {
                            FxmlControl.setEditorBadStyle(arcBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(arcBox);
                    }
                }
            });

            colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov,
                        Color old_toggle, Color new_toggle) {
                    AppVaribles.setUserConfigValue("ImageDoodleStrokerColor", colorPicker.getValue().toString());
                    if (!isSettingValues) {
                        updateMask();
                    }
                }
            });

            dottedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVaribles.setUserConfigValue("ImageDoodleDotted", dottedCheck.isSelected());
                    if (!isSettingValues) {
                        updateMask();
                    }
                }
            });

            fillCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    updateShape();
                    fillColorPicker.setDisable(!fillCheck.isSelected());
                    pickFillColorButton.setDisable(!fillCheck.isSelected());
                }
            });
            fillColorPicker.setDisable(!fillCheck.isSelected());
            pickFillColorButton.setDisable(!fillCheck.isSelected());

            fillColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov,
                        Color old_toggle, Color new_toggle) {
                    AppVaribles.setUserConfigValue("ImageDoodleFillColor", fillColorPicker.getValue().toString());
                    updateShape();
                }
            });

            setBox3.getChildren().clear();
            barsBox.getChildren().remove(setBox3);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkDoodleType() {
        try {
            setBox1.getChildren().clear();
            setBox2.getChildren().clear();
            okButton.disableProperty().unbind();
            maskImageView.setVisible(false);
            initMaskControls(false);
            maskPane.getChildren().removeAll(maskLineLines);
            maskLineLines.clear();
            maskLineData.clear();
            for (List<Line> penline : maskPenLines) {
                maskPane.getChildren().removeAll(penline);
            }
            maskPenLines.clear();
            maskPenData.clear();
            pickColorButton.setSelected(false);
            pickFillColorButton.setSelected(false);

            RadioButton selected = (RadioButton) doodleGroup.getSelectedToggle();
            if (message("Picture").equals(selected.getText())) {
                opType = DoodleType.Picture;
                setBox1.getChildren().addAll(fitLabel, pixBox, pixSelectButton, pasteButton, picView);
                setBox2.getChildren().addAll(blendLabel, blendBox, opacityLabel, opacityBox, keepRatioCheck);
                if (!maskPane.getChildren().contains(maskImageView)) {
                    maskPane.getChildren().add(maskImageView);
                }
                setMaskStroke();
                initMaskRectangleLine(true);
                maskRectangleLine.setOpacity(1);
                if (picture != null) {
                    picView.setImage(picture);
                } else {
                    isSettingValues = true;
                    pixBox.getSelectionModel().select(0);
                    isSettingValues = false;
                }
                drawMaskPicture();
                promptLabel.setText(message("DoodlePictureTips"));

            } else if (message("Shape").equals(selected.getText())) {
                opType = DoodleType.Shape;
                setBox1.getChildren().addAll(fitLabel, shapeTipsView, shapeLabel, shapesBox,
                        strokeWidthLabel, strokeWidthBox, strokeColorLabel, colorPicker, pickColorButton);
                setBox2.getChildren().addAll(dottedCheck, fillCheck, fillColorPicker, pickFillColorButton);
                isSettingValues = true;
                shapesBox.getSelectionModel().select(0);
                isSettingValues = false;
                checkShapeType();

            } else if (message("Pen").equals(selected.getText())) {
                opType = DoodleType.Pen;
                setBox1.getChildren().addAll(strokeWidthLabel, strokeWidthBox, strokeColorLabel, colorPicker,
                        pickColorButton);
                setBox2.getChildren().addAll(dottedCheck, withdrawButton);
                promptLabel.setText(message("DoodlePenTips"));

            } else if (message("Aline").equals(selected.getText())) {
                opType = DoodleType.Line;
                setBox1.getChildren().addAll(strokeWidthLabel, strokeWidthBox, strokeColorLabel, colorPicker,
                        pickColorButton);
                setBox2.getChildren().addAll(dottedCheck, withdrawButton);
                promptLabel.setText(message("DoodleLineTips"));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkShapeType() {

        if (isSettingValues) {
            return;
        }
        setBox2.getChildren().removeAll(arcLabel, arcBox, withdrawButton);
        okButton.disableProperty().unbind();
        maskImageView.setVisible(false);
        initMaskControls(false);

        String selected = shapesBox.getSelectionModel().getSelectedItem();
        if (message("Rectangle").equals(selected)) {
            shapeType = ScopeType.Rectangle;
            initMaskRectangleLine(true);
            maskRectangleLine.setOpacity(0);
            setBox2.getChildren().add(0, arcLabel);
            setBox2.getChildren().add(1, arcBox);
            promptLabel.setText(message("DoodleShapeTips"));

        } else if (message("Circle").equals(selected)) {
            shapeType = ScopeType.Circle;
            initMaskCircleLine(true);
            maskCircleLine.setOpacity(0);
            promptLabel.setText(message("DoodleShapeTips"));

        } else if (message("Ellipse").equals(selected)) {
            shapeType = ScopeType.Ellipse;
            initMaskEllipseLine(true);
            maskEllipseLine.setOpacity(0);
            promptLabel.setText(message("DoodleShapeTips"));

        } else if (message("Polygon").equals(selected)) {
            shapeType = ScopeType.Polygon;
            initMaskPolygonLine(true);
            maskPolygonLine.setOpacity(0);
            setBox2.getChildren().add(withdrawButton);
            promptLabel.setText(message("DoodlePolygonTips"));
        }
        strokeWidthLabel.requestFocus();
    }

    public void updateMask() {
        updateShape();
        drawMaskLine();
        drawMaskPenLines();
    }

    public void updateShape() {
        if (isSettingValues || maskPane == null || imageView.getImage() == null) {
            return;
        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            drawMaskRectangleLine();
        }
        if (maskCircleLine != null && maskCircleLine.isVisible()) {
            drawMaskCircleLine();
        }
        if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            drawMaskEllipseLine();
        }
        if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            drawMaskPolygonLine();
        }

    }

    @FXML
    public void selectPixAction() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            pixSelected(file);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void pixSelected(final File file) {
        try {
            if (file == null) {
                return;
            }
            recordFileOpened(file);

            Task selectTask = new Task<Void>() {
                private boolean ok;

                @Override
                protected Void call() throws Exception {
                    BufferedImage bufferImage = ImageFileReaders.readImage(file);
                    picture = SwingFXUtils.toFXImage(bufferImage, null);

                    ok = true;
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (ok) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                picView.setImage(picture);
                                drawMaskPicture();
                            }
                        });
                    }
                }
            };
            openHandlingStage(selectTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(selectTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void pasteAction() {
        values.setIsPaste(false);
        Image clipImage = SystemTools.fetchImageInClipboard(false);
        if (clipImage != null) {
            picture = clipImage;
            if (!pictureRadio.isSelected()) {
                doodleGroup.selectToggle(pictureRadio);
            } else {
                picView.setImage(clipImage);
                drawMaskPicture();
            }
        } else {
            popInformation(message("NoImageInClipboard"));
        }

    }

    @FXML
    public void picClicked() {
        picture = picView.getImage();
        drawMaskPicture();
    }

    @FXML
    public void popPixFile(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectPixAction();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                pixSelected(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void clearAction() {
        switch (opType) {
            case Picture:
                if (maskImageView != null) {
                    maskImageView.setImage(null);
                }
                break;
            case Shape:
                if (shapeType == ScopeType.Polygon) {
                    maskPolygonData.clear();
                    drawMaskPolygonLine();
                } else {
                    initMaskControls(false);
                    shapesBox.getSelectionModel().select(null);
                }
                break;
            case Line:
                maskPane.getChildren().removeAll(maskLineLines);
                maskLineLines.clear();
                maskLineData.clear();
                break;
            case Pen:
                for (List<Line> penline : maskPenLines) {
                    maskPane.getChildren().removeAll(penline);
                }
                maskPenLines.clear();
                maskPenData.clear();
                break;
            default:
                break;
        }
    }

    @FXML
    public void withdrawAction() {
        switch (opType) {
            case Shape:
                if (shapeType == ScopeType.Polygon) {
                    maskPolygonData.removeLast();
                    drawMaskPolygonLine();
                }
                break;
            case Line:
                maskLineData.removeLast();
                drawMaskLine();
                break;
            case Pen:
                maskPenData.removeLastLine();
                drawMaskPenLines();
                break;
            default:
                break;
        }

    }

    public void drawMaskPicture() {

        if (imageView == null || imageView.getImage() == null
                || maskImageView == null || !maskPane.getChildren().contains(maskImageView)) {
            return;
        }
        if (picture == null) {
            maskImageView.setImage(null);
            maskImageView.setVisible(false);
            setMaskRectangleLineVisible(false);
            return;
        }
        task = new Task<Void>() {
            private Image mixedImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                Image foreImage = FxmlImageManufacture.scaleImage(picture,
                        (int) maskRectangleData.getWidth(), (int) maskRectangleData.getHeight(),
                        keepRatioCheck.isSelected(), ImageManufacture.KeepRatioType.BaseOnLarger);

                if (keepRatioCheck.isSelected()) {
                    maskRectangleData = new DoubleRectangle(
                            maskRectangleData.getSmallX(), maskRectangleData.getSmallY(),
                            maskRectangleData.getSmallX() + foreImage.getWidth() - 1,
                            maskRectangleData.getSmallY() + foreImage.getHeight() - 1);
                }

                mixedImage = FxmlImageManufacture.blendImages(foreImage, imageView.getImage(),
                        (int) maskRectangleData.getSmallX(),
                        (int) maskRectangleData.getSmallY(),
                        blendMode, opacity);
                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            maskImageView.setImage(mixedImage);
                            maskImageView.setFitWidth(imageView.getFitWidth());
                            maskImageView.setFitHeight(imageView.getFitHeight());
                            maskImageView.setLayoutX(imageView.getLayoutX());
                            maskImageView.setLayoutY(imageView.getLayoutY());
                            maskImageView.setVisible(true);
                            setMaskRectangleLineVisible(true);
                            drawMaskRectangleLineAsData();

                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public void drawMaskControls() {
        super.drawMaskControls();

        drawMaskLine();
        drawMaskPenLines();

    }

    @Override
    public boolean drawMaskRectangleLine() {

        if (opType == DoodleType.Picture) {
            if (maskImageView != null && maskImageView.isVisible()) {
                drawMaskPicture();
            }
            return true;
        }

        if (imageView == null || imageView.getImage() == null
                || opType != DoodleType.Shape
                || maskRectangleLine == null || !maskPane.getChildren().contains(maskRectangleLine)) {
            return false;
        }

        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                newImage = FxmlImageManufacture.drawRectangle(imageView.getImage(),
                        maskRectangleData, colorPicker.getValue(), strokeWidth,
                        arcWidth, dottedCheck.isSelected(),
                        fillCheck.isSelected(), fillColorPicker.getValue());
                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            maskImageView.setImage(newImage);
                            maskImageView.setFitWidth(imageView.getFitWidth());
                            maskImageView.setFitHeight(imageView.getFitHeight());
                            maskImageView.setLayoutX(imageView.getLayoutX());
                            maskImageView.setLayoutY(imageView.getLayoutY());
                            maskImageView.setVisible(true);
                            drawMaskRectangleLineAsData();

                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    @Override
    public boolean drawMaskCircleLine() {
        if (imageView == null || imageView.getImage() == null
                || maskCircleLine == null || !maskPane.getChildren().contains(maskCircleLine)) {
            return false;
        }
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                newImage = FxmlImageManufacture.drawCircle(imageView.getImage(),
                        maskCircleData, colorPicker.getValue(), strokeWidth,
                        dottedCheck.isSelected(),
                        fillCheck.isSelected(), fillColorPicker.getValue());
                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            maskImageView.setImage(newImage);
                            maskImageView.setFitWidth(imageView.getFitWidth());
                            maskImageView.setFitHeight(imageView.getFitHeight());
                            maskImageView.setLayoutX(imageView.getLayoutX());
                            maskImageView.setLayoutY(imageView.getLayoutY());
                            maskImageView.setVisible(true);
                            drawMaskCircleLineAsData();

                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    @Override
    public boolean drawMaskEllipseLine() {
        if (imageView == null || imageView.getImage() == null
                || maskEllipseLine == null || !maskPane.getChildren().contains(maskEllipseLine)) {
            return false;
        }
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                newImage = FxmlImageManufacture.drawEllipse(imageView.getImage(),
                        maskEllipseData, colorPicker.getValue(), strokeWidth,
                        dottedCheck.isSelected(),
                        fillCheck.isSelected(), fillColorPicker.getValue());
                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            maskImageView.setImage(newImage);
                            maskImageView.setFitWidth(imageView.getFitWidth());
                            maskImageView.setFitHeight(imageView.getFitHeight());
                            maskImageView.setLayoutX(imageView.getLayoutX());
                            maskImageView.setLayoutY(imageView.getLayoutY());
                            maskImageView.setVisible(true);
                            drawMaskEllipseLineAsData();

                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    @Override
    public boolean drawMaskPolygonLine() {
        if (imageView == null || imageView.getImage() == null
                || maskPolygonLine == null || !maskPane.getChildren().contains(maskPolygonLine)) {
            return false;
        }
        maskImageView.setVisible(false);
        if (maskPolygonData.getSize() <= 2) {
            return drawMaskPolygonLineAsData();
        }
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                newImage = FxmlImageManufacture.drawPolygon(imageView.getImage(),
                        maskPolygonData, colorPicker.getValue(), strokeWidth,
                        dottedCheck.isSelected(),
                        fillCheck.isSelected(), fillColorPicker.getValue());
                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            maskImageView.setImage(newImage);
                            maskImageView.setFitWidth(imageView.getFitWidth());
                            maskImageView.setFitHeight(imageView.getFitHeight());
                            maskImageView.setLayoutX(imageView.getLayoutX());
                            maskImageView.setLayoutY(imageView.getLayoutY());
                            maskImageView.setVisible(true);
                            maskPolygonLine.setOpacity(0);
                            polygonP1.setOpacity(0);
                            polygonP2.setOpacity(0);
                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    // Polyline of Java shows weird results. So I just use lines directly.
    public boolean drawMaskLine() {
        if (opType != DoodleType.Line || imageView == null || imageView.getImage() == null) {
            return false;
        }
        return drawMaskLine(strokeWidth, colorPicker.getValue(), dottedCheck.isSelected());

    }

    public boolean drawMaskPenLines() {
        if (opType != DoodleType.Pen || imageView == null || imageView.getImage() == null) {
            return false;
        }
        return drawMaskPenLines(strokeWidth, colorPicker.getValue(), dottedCheck.isSelected());
    }

    @FXML
    public void mousePressed(MouseEvent event) {

        DoublePoint p = getImageXY(event, imageView);
        showXY(event, p);
        if (pickColorButton.isSelected() || pickFillColorButton.isSelected()
                || event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        if (opType == DoodleType.Line) {
            if (lastX == event.getX() && lastY == event.getY()) {
                return;
            }
            scrollPane.setPannable(false);
            maskLineData.add(p);
            lastX = event.getX();
            lastY = event.getY();
            drawMaskLine();

        } else if (opType == DoodleType.Pen) {

            scrollPane.setPannable(false);
            maskPenData.startLine(p);
            lastX = event.getX();
            lastY = event.getY();
            drawMaskPenLines();

        }
    }

    @FXML
    public void mouseDragged(MouseEvent event) {

        DoublePoint p = getImageXY(event, imageView);
        showXY(event, p);
        if (pickColorButton.isSelected() || pickFillColorButton.isSelected()
                || event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        if (opType == DoodleType.Line) {
            if (lastX == event.getX() && lastY == event.getY()) {
                return;
            }
            scrollPane.setPannable(false);
            maskLineData.add(p);
            lastX = event.getX();
            lastY = event.getY();
            drawMaskLine();

        } else if (opType == DoodleType.Pen) {

            scrollPane.setPannable(false);
            maskPenData.addPoint(p);
            lastX = event.getX();
            lastY = event.getY();
            drawMaskPenLines();

        }

    }

    @FXML
    public void mouseReleased(MouseEvent event) {

        scrollPane.setPannable(true);
        DoublePoint p = getImageXY(event, imageView);
        showXY(event, p);
        if (pickColorButton.isSelected() || pickFillColorButton.isSelected()
                || event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        if (opType == DoodleType.Line) {
            if (lastX == event.getX() && lastY == event.getY()) {
                return;
            }
            maskLineData.add(p);
            lastX = event.getX();
            lastY = event.getY();
            drawMaskLine();

        } else if (opType == DoodleType.Pen) {
            maskPenData.endLine(p);
            lastX = event.getX();
            lastY = event.getY();
            drawMaskPenLines();

        }
    }

    @FXML
    @Override
    public void paneClicked(MouseEvent event) {

        if (imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (pickColorButton.isSelected()) {
            DoublePoint p = getImageXY(event, imageView);
            if (p == null) {
                return;
            }
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            Color color = pixelReader.getColor((int) Math.round(p.getX()), (int) Math.round(p.getY()));
            colorPicker.setValue(color);

        } else if (pickFillColorButton.isSelected()) {
            DoublePoint p = getImageXY(event, imageView);
            if (p == null) {
                return;
            }
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            Color color = pixelReader.getColor((int) Math.round(p.getX()), (int) Math.round(p.getY()));
            fillColorPicker.setValue(color);

        } else {

            super.paneClicked(event);
            if (event.getButton() != MouseButton.SECONDARY) {
                imageView.setCursor(Cursor.OPEN_HAND);
                return;
            }
            DoublePoint p = getImageXY(event, imageView);
            if (p == null) {
                return;
            }
            if (opType == DoodleType.Line) {
                DoublePoint p0 = maskLineData.get(0);
                double offsetX = p.getX() - p0.getX();
                double offsetY = p.getY() - p0.getY();
                if (offsetX != 0 || offsetY != 0) {
                    maskLineData = maskLineData.move(offsetX, offsetY);
                    drawMaskLine();
                }

            } else if (opType == DoodleType.Pen) {
                DoublePoint p0 = maskPenData.getPoint(0);
                double offsetX = p.getX() - p0.getX();
                double offsetY = p.getY() - p0.getY();
                if (offsetX != 0 || offsetY != 0) {
                    maskPenData = maskPenData.move(offsetX, offsetY);
                    drawMaskPenLines();
                }

            }

        }
    }

    @FXML
    @Override
    public void okAction() {
        if (okButton.isDisabled()) {
            return;
        }

        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {

                newImage = null;

                switch (opType) {
                    case Shape:
                    case Picture:
                        if (maskImageView != null && maskImageView.isVisible()) {
                            newImage = maskImageView.getImage();
                        } else {
                            newImage = imageView.getImage();
                        }
                        break;
                    case Line:
                        if (maskLineData == null && maskLineData.getSize() < 2) {
                            return null;
                        }
                        newImage = FxmlImageManufacture.drawLines(imageView.getImage(),
                                maskLineData, colorPicker.getValue(), strokeWidth,
                                dottedCheck.isSelected());
                        break;
                    case Pen:
                        if (maskPenData == null && maskPenData.getPointsSize() == 0) {
                            return null;
                        }
                        newImage = FxmlImageManufacture.drawLines(imageView.getImage(),
                                maskPenData, colorPicker.getValue(), strokeWidth,
                                dottedCheck.isSelected());
                        break;
                    default:
                        return null;
                }

                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Doodle, newImage);

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok && newImage != null) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            values.setUndoImage(imageView.getImage());
                            values.setCurrentImage(newImage);
                            imageView.setImage(newImage);
                            setImageChanged(true);
                            double offset;
                            switch (opType) {
                                case Picture:
                                    offset = maskRectangleData.getWidth() / 2;
                                    maskRectangleData = new DoubleRectangle(
                                            maskRectangleData.getSmallX() + offset, maskRectangleData.getSmallY() + offset,
                                            maskRectangleData.getBigX() + offset, maskRectangleData.getBigY() + offset);
                                    drawMaskPicture();
                                    drawMaskRectangleLine();
                                    break;
                                case Shape:
                                    switch (shapeType) {
                                        case Rectangle: {
                                            offset = maskRectangleData.getWidth() / 4;
                                            maskRectangleData = maskRectangleData.move(offset);
                                            drawMaskRectangleLine();
                                            break;
                                        }
                                        case Circle: {
                                            offset = maskCircleData.getRadius() / 4;
                                            maskCircleData = maskCircleData.move(offset);
                                            drawMaskCircleLine();
                                            break;
                                        }
                                        case Ellipse: {
                                            offset = maskEllipseData.getRadiusX() / 4;
                                            maskEllipseData = maskEllipseData.move(offset);
                                            drawMaskEllipseLine();
                                            break;
                                        }
                                        case Polygon: {
                                            offset = imageView.getFitWidth() / 4;
                                            maskPolygonData = maskPolygonData.move(offset);
                                            drawMaskPolygonLine();
                                            break;
                                        }
                                        default:
                                            break;
                                    }
                                    break;
                                case Line:
                                    offset = imageView.getFitWidth() / 3;
                                    maskLineData = maskLineData.move(offset);
                                    drawMaskLine();
                                    break;
                                case Pen:
                                    offset = imageView.getFitWidth() / 3;
                                    maskPenData = maskPenData.move(offset);
                                    drawMaskPenLines();
                                    break;
                                default:
                                    break;
                            }

                        }
                    });
                }
            }
        };

        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}