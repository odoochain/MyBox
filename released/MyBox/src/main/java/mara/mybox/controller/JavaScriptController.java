package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class JavaScriptController extends TreeManageController {

    protected ControlWebView htmlWebView;
    protected String outputs = "";

    @FXML
    protected Tab htmlTab;
    @FXML
    protected JavaScriptEditor editorController;
    @FXML
    protected ControlWebView outputController, htmlController;

    public JavaScriptController() {
        baseTitle = "JavaScript";
        TipsLabelKey = "JavaScriptTips";
        category = TreeNode.JavaScript;
        nameMsg = message("Name");
        valueMsg = "JavaScript";
    }

    @Override
    public void initControls() {
        try {
            nodeController = editorController;
            super.initControls();

            editorController.setParameters(this);
            outputController.setParent(this, ControlWebView.ScrollType.Bottom);

            htmlWebView = htmlController;
            if (htmlController != null) {
                htmlController.setParent(this, ControlWebView.ScrollType.Bottom);
                htmlController.loadContents(HtmlWriteTools.emptyHmtl(message("AppTitle")));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(ControlWebView sourceWebView) {
        htmlWebView = sourceWebView;
        tabPane.getTabs().remove(htmlTab);
    }

    @Override
    public void setStageStatus() {
        setAsNormal();
    }

    @Override
    public void itemClicked() {
    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }

    @FXML
    public void popHtmlStyle(MouseEvent mouseEvent) {
        PopTools.popHtmlStyle(mouseEvent, outputController);
    }

    @FXML
    public void editResults() {
        outputController.editAction();
    }

    @FXML
    public void clearResults() {
        outputs = "";
        outputController.loadContents("");
    }

    public void edit(String script) {
        editNode(null);
        editorController.valueInput.setText(script);
    }

    public void runScirpt(String script) {
        try {
            if (htmlWebView == null) {
                popError(message("InvalidParameters") + ": Source WebView ");
                return;
            }
            if (script == null || script.isBlank()) {
                popError(message("InvalidParameters") + ": JavaScript");
                return;
            }
            if (rightPaneCheck != null) {
                rightPaneCheck.setSelected(true);
            }
            String ret;
            try {
                Object o = htmlWebView.webEngine.executeScript(script);
                if (o != null) {
                    ret = o.toString();
                } else {
                    ret = "";
                }
            } catch (Exception e) {
                ret = e.toString();
            }

            outputs += DateTools.nowString() + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(script) + "</div>";
            outputs += "<div class=\"valueBox\">" + HtmlWriteTools.stringToHtml(ret) + "</div><br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            outputController.loadContents(html);
            TableStringValues.add("JavaScriptHistories", script.trim());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static JavaScriptController open(ControlWebView controlWebView) {
        try {
            JavaScriptController controller = (JavaScriptController) WindowTools.openChildStage(
                    controlWebView.getMyWindow(), Fxmls.JavaScriptFxml, false);
            controller.setParameters(controlWebView);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static JavaScriptController open(String script) {
        try {
            JavaScriptController controller = (JavaScriptController) WindowTools.openStage(Fxmls.JavaScriptFxml);
            controller.edit(script);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
