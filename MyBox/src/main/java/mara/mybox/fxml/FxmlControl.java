package mara.mybox.fxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.tools.SoundTools;
import static mara.mybox.tools.FileTools.getFileSuffix;
import static mara.mybox.value.CommonValues.AppDataRoot;
import javafx.stage.Stage;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlControl {

    public static String badStyle = "-fx-text-box-border: red;   -fx-text-fill: red;";

    public static void miao3() {
        playSound("/sound/miao3.mp3", "miao3.mp3");
    }

    public static void miao8() {
        playSound("/sound/miao8.mp3", "miao8.mp3");
    }

    public static void playSound(final String file, final String userFile) {

        Task miaoTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    File miao = FxmlControl.getUserFile(getClass(), file, userFile);
                    FloatControl control = SoundTools.getControl(miao);
                    Clip player = SoundTools.playback(miao, control.getMaximum() * 0.6f);
                    player.start();
                } catch (Exception e) {
                }
                return null;
            }
        };
        Thread thread = new Thread(miaoTask);
        thread.setDaemon(true);
        thread.start();

    }

    public static void setScrollPane(ScrollPane scrollPane, double xOffset, double yOffset) {
        final Bounds visibleBounds = scrollPane.getViewportBounds();
        double scrollWidth = scrollPane.getContent().getBoundsInParent().getWidth() - visibleBounds.getWidth();
        double scrollHeight = scrollPane.getContent().getBoundsInParent().getHeight() - visibleBounds.getHeight();

        scrollPane.setHvalue(scrollPane.getHvalue() + xOffset / scrollWidth);
        scrollPane.setVvalue(scrollPane.getVvalue() + yOffset / scrollHeight);
    }

    public static boolean setRadioFirstSelected(ToggleGroup group) {
        if (group == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            radioButton.setSelected(true);
            return true;
        }
        return false;
    }

    public static boolean setRadioSelected(ToggleGroup group, String text) {
        if (group == null || text == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            if (text.equals(radioButton.getText())) {
                button.setSelected(true);
                return true;
            }
        }
        return false;
    }

    public static boolean setItemSelected(ComboBox<String> box, String text) {
        if (box == null || text == null) {
            return false;
        }
        ObservableList<String> items = box.getItems();
        for (String item : items) {
            if (text.equals(item)) {
                box.getSelectionModel().select(item);
                return true;
            }
        }
        return false;
    }

    // https://stackoverflow.com/questions/26854301/how-to-control-the-javafx-tooltips-delay
    public static void quickTooltip(final Node node, final Tooltip tooltip) {
        node.setOnMouseMoved(null);
        tooltip.setFont(new Font(AppVaribles.sceneFontSize));
        node.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                tooltip.show(node, event.getScreenX(), event.getScreenY() + 15);
            }
        });
        node.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                tooltip.hide();
            }
        });
    }

    public static void setComments(final Node node, final Tooltip tooltip) {
        node.setOnMouseMoved(null);
        tooltip.setFont(new Font(AppVaribles.sceneFontSize));
        node.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (AppVaribles.showComments) {
                    tooltip.show(node, event.getScreenX(), event.getScreenY() + 15);
                }
            }
        });
        node.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                tooltip.hide();
            }
        });
    }

    public static void removeTooltip(final Node node) {
        node.setOnMouseMoved(null);
        node.setOnMouseExited(null);
    }

    public static String getFxmlPath(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        int pos = fullPath.lastIndexOf("jar!");
        if (pos < 0) {
            return fullPath;
        }
        return fullPath.substring(pos + "jar!".length());
    }

    public static int getInputInt(TextField input) {
        try {
            return Integer.parseInt(input.getText());
        } catch (Exception e) {
            return CommonValues.InvalidValue;
        }
    }

    public static void setNonnegativeValidation(final TextField input) {
        setNonnegativeValidation(input, Integer.MAX_VALUE);
    }

    public static void setNonnegativeValidation(final TextField input, final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v >= 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setPositiveValidation(final TextField input) {
        setPositiveValidation(input, Integer.MAX_VALUE);
    }

    public static void setPositiveValidation(final TextField input, final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setFloatValidation(final TextField input) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    float v = Float.valueOf(newValue);
                    input.setStyle(null);
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setFileValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.exists() || !file.isFile()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static void setPathValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.isDirectory()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static void setPathExistedValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.exists() || !file.isDirectory()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static File getUserFile(Class someClass, String resourceFile, String userFile) {
        return getUserFile(someClass, resourceFile, userFile, false);
    }

    // Solution from https://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file
    public static File getUserFile(Class someClass, String resourceFile, String userFile,
            boolean deleteExisted) {
        if (someClass == null || resourceFile == null || userFile == null) {
            return null;
        }
        File file = new File(AppDataRoot + "/" + userFile);
        if (file.exists()) {
            if (deleteExisted) {
                file.delete();
            } else {
                return file;
            }
        }
        URL url = someClass.getResource(resourceFile);
        if (url.toString().startsWith("jar:")) {
            try {
                try (InputStream input = someClass.getResourceAsStream(resourceFile);
                        OutputStream out = new FileOutputStream(file)) {
                    int read;
                    byte[] bytes = new byte[1024];
                    while ((read = input.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(someClass.getResource(resourceFile).getFile());
        }
        return file;
    }

    public static File getResourceFile(Class someClass, String resourceFile) {
        if (someClass == null || resourceFile == null) {
            return null;
        }

        File file = null;
        URL url = someClass.getResource(resourceFile);
        if (url.toString().startsWith("jar:")) {
            try {
                InputStream input = someClass.getResourceAsStream(resourceFile);
                file = File.createTempFile("MyBox", "." + getFileSuffix(resourceFile));
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];
                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                file.deleteOnExit();
            } catch (Exception e) {
                logger.error(e.toString());
            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(someClass.getResource(resourceFile).getFile());
        }
        return file;
    }

    public static void setEditorStyle(final ComboBox box, final String style) {
        box.getEditor().setStyle(style);
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                box.getEditor().setStyle(style);
//            }
//        });
    }

    public static void setEditorBadStyle(final ComboBox box) {
        setEditorStyle(box, badStyle);
    }

    public static void setEditorNormal(final ComboBox box) {
        setEditorStyle(box, null);
    }

    public static void moveXCenter(Region pNnode, Node node) {
        try {
            if (node == null || pNnode == null) {
                return;
            }
            double xOffset = pNnode.getWidth() - node.getBoundsInLocal().getWidth();
            if (xOffset > 0) {
                node.setLayoutX(pNnode.getLayoutX() + xOffset / 2);
            } else {
                node.setLayoutX(0);
            }
        } catch (Exception e) {
        }
    }

    public static void moveYCenter(Region pNnode, Node node) {
        if (node == null || pNnode == null) {
            return;
        }
        double yOffset = pNnode.getHeight() - node.getBoundsInLocal().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(pNnode.getLayoutY() + yOffset / 2);
        }
    }

    public static void moveCenter(Region pNnode, Node node) {
        moveXCenter(pNnode, node);
        moveYCenter(pNnode, node);
    }

    public static void moveXCenter(Node node) {
        if (node == null || node.getParent() == null) {
            return;
        }
        double xOffset = node.getParent().getBoundsInLocal().getWidth() - node.getBoundsInLocal().getWidth();
        if (xOffset > 0) {
            node.setLayoutX(xOffset / 2);
        } else {
            node.setLayoutX(0);
        }
    }

    public static void moveYCenter(Node node) {
        if (node == null || node.getParent() == null) {
            return;
        }
        double yOffset = node.getParent().getBoundsInLocal().getHeight() - node.getBoundsInLocal().getHeight();
        if (yOffset > 0) {
            node.setLayoutY(yOffset / 2);
        }
    }

    public static void moveCenter(Node node) {
        moveXCenter(node);
        moveYCenter(node);
    }

    public static void paneSize(ScrollPane sPane, ImageView iView) {
        try {
            if (iView == null || iView.getImage() == null
                    || sPane == null) {
                return;
            }
            iView.setFitWidth(sPane.getWidth() - 1);
            iView.setFitHeight(sPane.getHeight() - 5);
            FxmlControl.moveXCenter(sPane, iView);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public static void imageSize(ScrollPane sPane, ImageView iView) {
        try {
            if (iView == null || iView.getImage() == null
                    || sPane == null) {
                return;
            }
            iView.setFitWidth(iView.getImage().getWidth());
            iView.setFitHeight(iView.getImage().getHeight());
            FxmlControl.moveXCenter(sPane, iView);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public static void zoomIn(ScrollPane sPane, ImageView iView, int xZoomStep, int yZoomStep) {
        double currentWidth = iView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = iView.getImage().getWidth();
        }
        iView.setFitWidth(currentWidth + xZoomStep);
        double currentHeight = iView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = iView.getImage().getHeight();
        }
        iView.setFitHeight(currentHeight + yZoomStep);
        FxmlControl.moveXCenter(sPane, iView);
    }

    public static void zoomOut(ScrollPane sPane, ImageView iView, int xZoomStep, int yZoomStep) {
        double currentWidth = iView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = iView.getImage().getWidth();
        }
        if (currentWidth <= xZoomStep) {
            return;
        }
        iView.setFitWidth(currentWidth - xZoomStep);
        double currentHeight = iView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = iView.getImage().getHeight();
        }
        if (currentHeight <= yZoomStep) {
            return;
        }
        iView.setFitHeight(currentHeight - yZoomStep);
        FxmlControl.moveXCenter(sPane, iView);
    }

    // https://stackoverflow.com/questions/38599588/javafx-stage-setmaximized-only-works-once-on-mac-osx-10-9-5
    public static void setMaximized(Stage stage, boolean max) {
        stage.setMaximized(max);
        if (max) {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX(primaryScreenBounds.getMinX());
            stage.setY(primaryScreenBounds.getMinY());
            stage.setWidth(primaryScreenBounds.getWidth());
            stage.setHeight(primaryScreenBounds.getHeight());
        }
    }

    public static void locateCenter(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
    }

    public static void locateBelow(Region region, PopupWindow window) {
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        window.show(region, bounds.getMinX() + 2, bounds.getMinY() + bounds.getHeight() + 5);
    }

    public static List<Node> traverseNode(Node node, List<Node> children) {
        if (node == null) {
            return children;
        }
        if (children == null) {
            children = new ArrayList();
        }
        children.add(node);
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                traverseNode(c, children);
            }
        }
        return children;
    }

}
