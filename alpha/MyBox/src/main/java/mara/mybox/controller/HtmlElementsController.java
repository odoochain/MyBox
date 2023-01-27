package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;

/**
 * @Author Mara
 * @CreateDate 2021-5-5
 * @License Apache License Version 2.0
 */
public class HtmlElementsController extends WebAddressController {

    protected int foundCount;
    protected HTMLDocument loadedDoc;
    protected String key, sourceAddress, sourceHtml;

    @FXML
    protected HBox elementsBox;
    @FXML
    protected RadioButton tagRadio, idRadio, nameRadio;
    @FXML
    protected TextField elementInput;
    @FXML
    protected ToggleGroup elementGroup;
    @FXML
    protected Button queryElementButton;

    public HtmlElementsController() {
        baseTitle = message("WebElements");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            key = baseName + "TagHistories";
            elementInput.setText("table");

            elementGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (tagRadio.isSelected()) {
                        key = baseName + "TagHistories";
                        elementInput.setText("table");
                    } else if (idRadio.isSelected()) {
                        key = baseName + "IdHistories";
                        elementInput.setText("id");
                    } else if (nameRadio.isSelected()) {
                        key = baseName + "NameHistories";
                        elementInput.setText("name");
                    }
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void elements(String html) {
        loadContents(html);
    }

    @Override
    public void pageLoading() {
        super.pageLoading();
        queryElementButton.setDisable(true);
        recoverButton.setDisable(true);
    }

    @Override
    public void pageLoaded() {
        try {
            super.pageLoaded();
            bottomLabel.setText(message("Count") + ": " + foundCount);
            if (loadedDoc == null) {
                loadedDoc = (HTMLDocument) webEngine.getDocument();
                sourceHtml = WebViewTools.getHtml(webEngine);
                sourceAddress = webViewController.address;
            }
            queryElementButton.setDisable(false);
            recoverButton.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void queryElement() {
        try {
            foundCount = 0;
            String value = elementInput.getText();
            if (value == null || value.isBlank()) {
                popError(message("InvalidData"));
                return;
            }
            TableStringValues.add(key, value);
            if (loadedDoc == null) {
                popInformation(message("NoData"));
                return;
            }
            loadContents(null);
            NodeList aList = null;
            Element e = null;
            if (tagRadio.isSelected()) {
                aList = loadedDoc.getElementsByTagName(value);
            } else if (idRadio.isSelected()) {
                e = loadedDoc.getElementById(value);
            } else if (nameRadio.isSelected()) {
                aList = loadedDoc.getElementsByName(value);
            }
            List<Element> elements = new ArrayList<>();
            if (aList != null && aList.getLength() > 0) {
                for (int i = 0; i < aList.getLength(); i++) {
                    org.w3c.dom.Node node = aList.item(i);
                    if (node != null) {
                        elements.add((Element) node);
                    }
                }
            } else if (e != null) {
                elements.add(e);
            }
            if (elements.isEmpty()) {
                loadContents("");
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Index"), message("Tag"), message("Name"),
                    message("Type"), message("Value"), message("Attributes"), message("Texts")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (Element el : elements) {
                List<String> row = new ArrayList<>();
                NamedNodeMap attrs = el.getAttributes();
                String attrsString = "";
                if (attrs != null) {
                    for (int i = 0; i < attrs.getLength(); i++) {
                        org.w3c.dom.Node node = attrs.item(i);
                        attrsString += node.getNodeName() + "=" + node.getNodeValue() + " ";
                    }
                }
                row.addAll(Arrays.asList(
                        index + "", el.getTagName(), el.getNodeName(), el.getNodeType() + "", el.getNodeValue(),
                        attrsString, el.getTextContent()
                ));
                table.add(row);
                index++;
            }
            foundCount = index - 1;
            String html = HtmlWriteTools.html(null, HtmlStyles.styleValue("Default"), StringTable.tableDiv(table));
            loadContents(html);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        loadContents(sourceAddress, sourceHtml);
    }

    @FXML
    public void popExamples(MouseEvent mouseEvent) {
        try {
            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    "p", "img", "a", "div", "li", "ul", "ol", "h1", "h2", "h3",
                    "button", "input", "label", "form", "table", "tr", "th", "td",
                    "script", "style", "font", "span", "b", "hr", "br", "frame", "pre"
            ));

            List<Node> buttons = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tagRadio.setSelected(true);
                        elementInput.setText(value);
                        queryElement();
                    }
                });
                buttons.add(button);
            }

            MenuController controller = MenuController.open(this, elementInput, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.addFlowPane(buttons);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popElementHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, elementInput, mouseEvent, key, true);
    }

}
