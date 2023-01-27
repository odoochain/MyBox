package mara.mybox.controller;

import java.util.Arrays;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class JavaScriptEditor extends TreeNodeEditor {

    protected JavaScriptController jsController;

    @FXML
    protected Button clearCodesButton;

    public JavaScriptEditor() {
        defaultExt = "js";
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(clearCodesButton, new Tooltip(message("Clear") + "\nCTRL+g"));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(JavaScriptController jsController) {
        this.jsController = jsController;
    }

    @Override
    protected void showEditorPane() {
    }

    @FXML
    @Override
    public void startAction() {
        jsController.runScirpt(getScript());
    }

    public String getScript() {
        return valueInput.getText();
    }

    @FXML
    protected void popExamplesMenu(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", false)) {
            examplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(ActionEvent event) {
        examplesMenu(event);
    }

    protected void examplesMenu(Event event) {
        try {
            MenuController controller = PopTools.popJavaScriptExamples(this, event, valueInput, interfaceName + "Examples");

            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "''.search(//ig) >= 0", "''.length > 0", "''.indexOf('') >= 0",
                    "''.startsWith('')", "''.endsWith('')", "''.replace(//ig,'')"
            ), true, 4);
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "Math.PI", "Math.E", "Math.abs(-5.47)", "Math.random()",
                    "Math.trunc(3.51)", "Math.round(3.51)", "Math.ceil(3.15)", "Math.floor(3.51)",
                    "Math.pow(3, 4)", "Math.sqrt(9)", "Math.exp(2)", "Math.log(5)",
                    "Math.min(1,2,-3)", "Math.max(1,2,-3)", "Math.sin(Math.PI/2)", "Math.cos(0)", "Math.tan(2)"
            ), true, 5);
            PopTools.addButtonsPane(controller, valueInput, Arrays.asList(
                    "var array = [ 'A', 'B', 'C', 'D' ];\narray[3]",
                    "var object = { name1:'value1', name2:'value2', name3:'value3'}; \nobject.name2"
            ), true, 6);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popHistories(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean(interfaceName + "HistoriesPopWhenMouseHovering", false)) {
            PopTools.popStringValues(this, valueInput, mouseEvent, interfaceName + "Histories", false, true);
        }
    }

    @FXML
    protected void showHistories(ActionEvent event) {
        PopTools.popStringValues(this, valueInput, event, interfaceName + "Histories", false, true);
    }

    @Override
    public void cleanPane() {
        try {
            cancelAction();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
