package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.robot.Robot;
import javafx.util.Callback;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.data.TreeNode.NodeSeparater;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @param <P>
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public abstract class BaseNodeSelector<P> extends BaseController {

    protected static final int AutoExpandThreshold = 1000;
    protected P ignoreNode = null;
    protected boolean expandAll, manageMode, nodeExecutable;
    protected int serialStartLevel = 1;
    protected String defaultClickAction = "PopMenu";
    protected final SimpleBooleanProperty loadedNotify;

    @FXML
    protected TreeView<P> treeView;
    @FXML
    protected Label titleLabel;

    /*
        abstract methods
     */
    protected abstract String name(P node);

    protected abstract long id(P node);

    protected abstract String display(P node);

    protected abstract String tooltip(P node);

    protected abstract String serialNumber(P node);

    protected abstract P dummy();

    protected abstract boolean isDummy(P node);

    protected abstract P root(Connection conn);

    protected abstract int categorySize(Connection conn);

    protected abstract boolean childrenEmpty(Connection conn, P node);

    protected abstract List<P> children(Connection conn, P node);

    protected abstract List<P> ancestor(Connection conn, P node);

    protected abstract P createNode(P targetNode, String name);

    protected abstract void delete(Connection conn, P node);

    protected abstract void clearTree(Connection conn, P node);

    protected abstract P rename(P node, String name);

    protected abstract void doubleClicked(TreeItem<P> item);

    protected abstract void listChildren(TreeItem<P> item);

    protected abstract void listDescentants(TreeItem<P> item);

    protected abstract void copyNode(TreeItem<P> item);

    protected abstract void moveNode(TreeItem<P> item);

    protected abstract void editNode(TreeItem<P> item);

    protected abstract void pasteNode(TreeItem<P> item);

    protected abstract void executeNode(TreeItem<P> item);

    protected abstract void exportNode(TreeItem<P> item);

    protected abstract void importAction();

    protected abstract void importExamples();

    protected abstract void nodeAdded(P parent, P newNode);

    protected abstract void nodeDeleted(P node);

    protected abstract void nodeRenamed(P node);

    protected abstract void nodeMoved(P parent, P node);

    protected abstract void treeView(Connection conn, P node, int indent, String parentNumber, StringBuilder s);

    public abstract TreeManageController openManager();

    public BaseNodeSelector() {
        loadedNotify = new SimpleBooleanProperty(false);
    }

    public void notifyLoaded() {
        loadedNotify.set(!loadedNotify.get());
    }


    /*
        Common methods may need not changed
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initTree();

            if (okButton != null) {
                okButton.disableProperty().bind(treeView.getSelectionModel().selectedItemProperty().isNull());
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String serialNumber(TreeItem<P> item) {
        if (treeView.getTreeItemLevel(item) < serialStartLevel) {
            return "";
        }
        TreeItem<P> parent = item.getParent();
        if (parent == null) {
            return "";
        }
        String p = serialNumber(parent);
        return (p == null || p.isBlank() ? "" : p + ".") + (parent.getChildren().indexOf(item) + 1);
    }

    public void initTree() {
        treeView.setCellFactory(new Callback<TreeView<P>, TreeCell<P>>() {
            @Override
            public TreeCell<P> call(TreeView<P> param) {
                TreeCell<P> cell = new TreeCell<P>() {
                    @Override
                    public void updateItem(P item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }
                        if (UserConfig.getBoolean("TreeDisplaySequenceNumber", true)) {
                            String serialNumber = serialNumber(getTreeItem());
                            setText(serialNumber + "  " + display(item));
                        } else {
                            setText(display(item));
                        }
                        String tips = tooltip(item);
                        if (tips != null && !tips.isBlank()) {
                            NodeStyleTools.setTooltip(this, tips);
                        } else {
                            NodeStyleTools.removeTooltip(this);
                        }
                    }
                };
                return cell;
            }
        });
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                TreeItem<P> item = treeView.getSelectionModel().getSelectedItem();
                if (event.getButton() == MouseButton.SECONDARY) {
                    popFunctionsMenu(event, item);
                } else {
                    if (event.getClickCount() > 1) {
                        doubleClicked(item);
                    } else {
                        itemSelected(item);
                    }
                }
            }
        });
    }

    public void setManager(BaseController parent, boolean manageMode) {
        this.parentController = parent;
        if (parent != null) {
            this.baseName = parent.baseName;
        }
        this.ignoreNode = getIgnoreNode();
        this.manageMode = manageMode;
    }

    public void loadTree() {
        loadTree(null);
    }

    public void loadTree(P selectNode) {
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
                private boolean expand;
                private TreeItem<P> rootItem;

                @Override

                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        P rootNode = root(conn);
                        rootItem = new TreeItem(rootNode);
                        ignoreNode = getIgnoreNode();
                        int size = categorySize(conn);
                        if (size < 1) {
                            return true;
                        }
                        expand = size <= AutoExpandThreshold;
                        if (expand) {
                            expandChildren(conn, rootItem);
                        } else {
                            loadChildren(conn, rootItem);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    treeView.setRoot(rootItem);
                    rootItem.setExpanded(true);
                    if (selectNode != null) {
                        TreeItem<P> selecItem = find(selectNode);
                        if (selecItem != null) {
                            select(selecItem);
                        } else {
                            select(rootItem);
                        }
                    }
                }

                @Override
                protected void finalAction() {
                    super.finalAction();
                    notifyLoaded();
                }

            };
            start(task);
        }
    }

    public P getIgnoreNode() {
        return ignoreNode;
    }

    public boolean equal(P node1, P node2) {
        return id(node1) == id(node2);
    }

    protected boolean isRoot(P node) {
        if (treeView.getRoot() == null || node == null) {
            return false;
        }
        return equal(treeView.getRoot().getValue(), node);
    }

    public String chainName(Connection conn, P node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<P> ancestor = ancestor(conn, node);
        if (ancestor != null) {
            for (P a : ancestor) {
                chainName += name(a) + NodeSeparater;
            }
        }
        chainName += name(node);
        return chainName;
    }

    public String chainName(TreeItem<P> node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<TreeItem<P>> ancestor = ancestor(node);
        if (ancestor != null) {
            for (TreeItem<P> a : ancestor) {
                chainName += name(a.getValue()) + NodeSeparater;
            }
        }
        chainName += name(node.getValue());
        return chainName;
    }

    public void itemSelected(TreeItem<P> item) {
        if (item == null || !manageMode) {
            return;
        }
        String clickAction = UserConfig.getString(baseName + "TreeWhenClickNode", defaultClickAction);
        if (null == clickAction) {
            popFunctionsMenu(null, item);
        } else {
            switch (clickAction) {
                case "PopMenu":
                    popFunctionsMenu(null, item);
                    break;
                case "Edit":
                    editNode(item);
                    break;
                case "Paste":
                    pasteNode(item);
                    break;
                case "Execute":
                    executeNode(item);
                    break;
                case "LoadChildren":
                    listChildren(item);
                    break;
                case "LoadDescendants":
                    listDescentants(item);
                    break;
                default:
                    break;
            }
        }
    }

    @FXML
    public void popFunctionsMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        popFunctionsMenu(event, currectSelected());
    }

    public void popFunctionsMenu(MouseEvent event, TreeItem<P> node) {
        if (getMyWindow() == null) {
            return;
        }
        List<MenuItem> items = makeNodeMenu(node);
        items.add(new SeparatorMenuItem());

        MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        if (event == null) {
            Robot r = new Robot();
            popMenu.show(treeView, r.getMouseX() + 40, r.getMouseY() + 20);
        } else {
            popMenu.show(treeView, event.getScreenX(), event.getScreenY());
        }
    }

    protected List<MenuItem> makeNodeMenu(TreeItem<P> item) {
        TreeItem<P> targetItem = item == null ? treeView.getRoot() : item;
        boolean isRoot = targetItem == null || isRoot(targetItem.getValue());

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(PopTools.limitMenuName(chainName(targetItem)));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        CheckMenuItem editableMenu = new CheckMenuItem(message("SequenceNumber"), StyleTools.getIconImageView("iconNumber.png"));
        editableMenu.setSelected(UserConfig.getBoolean("TreeDisplaySequenceNumber", true));
        editableMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("TreeDisplaySequenceNumber", editableMenu.isSelected());
                treeView.refresh();
            }
        });
        items.add(editableMenu);

        if (manageMode) {
            Menu clickMenu = new Menu(message("WhenClickNode"), StyleTools.getIconImageView("iconSelect.png"));
            ToggleGroup clickGroup = new ToggleGroup();
            String currentClick = UserConfig.getString(baseName + "TreeWhenClickNode", defaultClickAction);

            RadioMenuItem clickPopMenu = new RadioMenuItem(message("PopMenu"), StyleTools.getIconImageView("iconMenu.png"));
            clickPopMenu.setSelected(currentClick == null || "PopMenu".equals(currentClick));
            clickPopMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "TreeWhenClickNode", "PopMenu");
                }
            });
            clickPopMenu.setToggleGroup(clickGroup);

            RadioMenuItem editNodeMenu = new RadioMenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
            editNodeMenu.setSelected("Edit".equals(currentClick));
            editNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "TreeWhenClickNode", "Edit");
                }
            });
            editNodeMenu.setToggleGroup(clickGroup);

            RadioMenuItem pasteNodeMenu = new RadioMenuItem(message("Paste"), StyleTools.getIconImageView("iconPaste.png"));
            pasteNodeMenu.setSelected("Paste".equals(currentClick));
            pasteNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "TreeWhenClickNode", "Paste");
                }
            });
            pasteNodeMenu.setToggleGroup(clickGroup);

            clickMenu.getItems().addAll(clickPopMenu, editNodeMenu, pasteNodeMenu);

            if (nodeExecutable) {
                RadioMenuItem executeNodeMenu = new RadioMenuItem(message("Execute"), StyleTools.getIconImageView("iconGo.png"));
                executeNodeMenu.setSelected("Execute".equals(currentClick));
                executeNodeMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setString(baseName + "TreeWhenClickNode", "Execute");
                    }
                });
                executeNodeMenu.setToggleGroup(clickGroup);
                clickMenu.getItems().add(executeNodeMenu);
            }

            RadioMenuItem loadChildrenMenu = new RadioMenuItem(message("LoadChildren"), StyleTools.getIconImageView("iconList.png"));
            loadChildrenMenu.setSelected("LoadChildren".equals(currentClick));
            loadChildrenMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "TreeWhenClickNode", "LoadChildren");
                }
            });
            loadChildrenMenu.setToggleGroup(clickGroup);

            RadioMenuItem loadDescendantsMenu = new RadioMenuItem(message("LoadDescendants"), StyleTools.getIconImageView("iconList.png"));
            loadDescendantsMenu.setSelected("LoadDescendants".equals(currentClick));
            loadDescendantsMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setString(baseName + "TreeWhenClickNode", "LoadDescendants");
                }
            });
            loadDescendantsMenu.setToggleGroup(clickGroup);

            clickMenu.getItems().addAll(loadChildrenMenu, loadDescendantsMenu);

            items.add(clickMenu);

        } else {
            menu = new MenuItem(message("Manage"), StyleTools.getIconImageView("iconData.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                openManager();
            });
            items.add(menu);
        }

        items.add(new SeparatorMenuItem());

        if (manageMode) {
            Menu dataMenu = new Menu(message("Data"), StyleTools.getIconImageView("iconData.png"));
            items.add(dataMenu);

            menu = new MenuItem(message("TreeView"), StyleTools.getIconImageView("iconTree.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                treeView();
            });
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                importExamples();
            });
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("Export"), StyleTools.getIconImageView("iconExport.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                exportNode(targetItem);
            });
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("Import"), StyleTools.getIconImageView("iconImport.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                importAction();
            });
            dataMenu.getItems().add(menu);

            Menu viewMenu = new Menu(message("View"), StyleTools.getIconImageView("iconView.png"));
            items.add(viewMenu);

            menu = new MenuItem(message("LoadChildren"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    listChildren(item);
                }
            });
            viewMenu.getItems().add(menu);

            menu = new MenuItem(message("LoadDescendants"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    listDescentants(item);
                }
            });
            viewMenu.getItems().add(menu);

            menu = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPLus.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                unfoldNodes();
            });
            viewMenu.getItems().add(menu);

            menu = new MenuItem(message("Fold"), StyleTools.getIconImageView("iconMinus.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                foldNodes();
            });
            viewMenu.getItems().add(menu);

            Menu modifyMenu = new Menu(message("Modify"), StyleTools.getIconImageView("iconEdit.png"));
            items.add(modifyMenu);

            menu = new MenuItem(message("Add"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                addNode(targetItem);
            });
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                editNode(targetItem);
            });
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Paste"), StyleTools.getIconImageView("iconPaste.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    pasteNode(targetItem);
                }
            });
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNode(targetItem);
            });
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Rename"), StyleTools.getIconImageView("iconRename.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                renameNode(targetItem);
            });
            menu.setDisable(isRoot);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNode(targetItem);
            });
            menu.setDisable(isRoot);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Move"), StyleTools.getIconImageView("iconRef.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveNode(targetItem);
            });
            menu.setDisable(isRoot);
            modifyMenu.getItems().add(menu);

        } else {

            menu = new MenuItem(message("Add"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                addNode(targetItem);
            });
            items.add(menu);

            menu = new MenuItem(message("Examples"), StyleTools.getIconImageView("iconExamples.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                importExamples();
            });
            items.add(menu);

            menu = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPLus.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                unfoldNodes();
            });
            items.add(menu);

            menu = new MenuItem(message("Fold"), StyleTools.getIconImageView("iconMinus.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                foldNodes();
            });
            items.add(menu);
        }

        return items;
    }

    protected void addNode(TreeItem<P> targetItem) {
        if (targetItem == null) {
            popError(message("SelectToHandle"));
            return;
        }
        P targetNode = targetItem.getValue();
        if (targetNode == null) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(targetItem);
        String name = PopTools.askValue(getBaseTitle(), chainName, message("Add"), message("Node") + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.contains(NodeSeparater)) {
            popError(message("NameShouldNotInclude") + " \"" + NodeSeparater + "\"");
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
                private P newNode;

                @Override
                protected boolean handle() {
                    newNode = createNode(targetNode, name);
                    return newNode != null;
                }

                @Override
                protected void whenSucceeded() {
                    TreeItem<P> newItem = new TreeItem<>(newNode);
                    targetItem.getChildren().add(newItem);
                    targetItem.setExpanded(true);
                    nodeAdded(targetNode, newNode);
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    protected void deleteNode(TreeItem<P> targetItem) {
        if (targetItem == null) {
            popError(message("SelectToHandle"));
            return;
        }
        P node = targetItem.getValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        boolean isRoot = isRoot(node);
        if (isRoot) {
            if (!PopTools.askSure(this, getBaseTitle(), message("Delete"), message("SureDeleteAll"))) {
                return;
            }
        } else {
            String chainName = chainName(targetItem);
            if (!PopTools.askSure(this, getBaseTitle(), chainName, message("Delete"))) {
                return;
            }
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                private TreeItem<P> rootItem;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (isRoot) {
                            clearTree(conn, node);
                            P rootNode = root(conn);
                            rootItem = new TreeItem(rootNode);
                        } else {
                            delete(conn, node);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (isRoot) {
                        treeView.setRoot(rootItem);
                        rootItem.setExpanded(true);
//                        itemSelected(rootItem);
//                        nodeDeleted(rootItem.getValue());
                    } else {
                        targetItem.getChildren().clear();
                        if (targetItem.getParent() != null) {
                            targetItem.getParent().getChildren().remove(targetItem);
                        }
//                        itemSelected(treeView.getSelectionModel().getSelectedItem());
//                        nodeDeleted(targetItem.getValue());
                    }

                    popSuccessful();
                }

            };
            start(task);
        }
    }

    protected void renameNode(TreeItem<P> item) {
        if (item == null) {
            popError(message("SelectToHandle"));
            return;
        }
        P nodeValue = item.getValue();
        if (nodeValue == null || isRoot(nodeValue)) {
            popError(message("SelectToHandle"));
            return;
        }
        String chainName = chainName(item);
        String name = PopTools.askValue(getBaseTitle(), chainName, message("RenameNode"), name(nodeValue) + "m");
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.contains(NodeSeparater)) {
            popError(message("NodeNameNotInclude") + " \"" + NodeSeparater + "\"");
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {
                private P updatedNode;

                @Override
                protected boolean handle() {
                    updatedNode = rename(nodeValue, name);
                    return updatedNode != null;
                }

                @Override
                protected void whenSucceeded() {
                    item.setValue(updatedNode);
                    item.getParent().getChildren().set(item.getParent().getChildren().indexOf(item), item); // force item refreshed
                    nodeRenamed(updatedNode);
                    popSuccessful();
                }
            };
            start(task);
        }
    }

    @FXML
    protected void foldNodes() {
        fold(currectSelected());
    }

    @FXML
    protected void unfoldNodes() {
        expandChildren(currectSelected());
    }

    protected void expandChildren(TreeItem<P> item) {
        if (item == null) {
            return;
        }
        P node = item.getValue();
        if (node == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        expandChildren(conn, item);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    treeView.refresh();
                }
            };
            start(task);
        }
    }

    protected void expandChildren(Connection conn, TreeItem<P> item) {
        if (conn == null || item == null) {
            return;
        }
        item.getChildren().clear();
        P node = item.getValue();
        if (node == null) {
            return;
        }
        ignoreNode = getIgnoreNode();
        List<P> children = children(conn, node);
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                P child = children.get(i);
                if (ignoreNode != null && equal(child, ignoreNode)) {
                    continue;
                }
                TreeItem<P> childNode = new TreeItem(child);
                expandChildren(conn, childNode);
                childNode.setExpanded(true);
                item.getChildren().add(childNode);
            }
        }
        item.setExpanded(true);
    }

    protected void loadChildren(TreeItem<P> item) {
        if (item == null) {
            return;
        }
        P node = item.getValue();
        if (node == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        loadChildren(conn, item);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    treeView.refresh();
                }
            };
            start(task);

        }
    }

    protected void loadChildren(Connection conn, TreeItem<P> item) {
        if (conn == null || item == null) {
            return;
        }
        item.getChildren().clear();
        P node = item.getValue();
        if (node == null) {
            return;
        }
        ignoreNode = getIgnoreNode();
        List<P> children = children(conn, node);
        if (children != null) {
            for (P child : children) {
                if (ignoreNode != null && equal(child, ignoreNode)) {
                    continue;
                }
                TreeItem<P> childItem = new TreeItem(child);
                item.getChildren().add(childItem);
                childItem.setExpanded(false);
                if (!childrenEmpty(conn, child)) {
                    childItem.expandedProperty().addListener(
                            (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                if (newVal && !childItem.isLeaf() && !loaded(childItem)) {
                                    loadChildren(childItem);
                                }
                            });
                    TreeItem<P> dummyItem = new TreeItem(dummy());
                    childItem.getChildren().add(dummyItem);
                }
            }
        }
        item.setExpanded(true);
    }

    protected void addNewNode(TreeItem<P> item, P node, boolean select) {
        if (item == null || node == null) {
            return;
        }
        TreeItem<P> child = new TreeItem(node);
        item.getChildren().add(child);
        child.setExpanded(false);
        if (select) {
            select(item);
        }
    }

    protected void updateChild(TreeItem<P> item, P node) {
        if (item == null || node == null) {
            return;
        }
        for (TreeItem<P> child : item.getChildren()) {
            P value = child.getValue();
            if (value != null && equal(node, value)) {
                child.setValue(node);
                return;
            }
        }
        loadChildren(item);
    }

    protected boolean loaded(TreeItem<P> item) {
        if (item == null || item.isLeaf()) {
            return true;
        }
        try {
            TreeItem<P> child = (TreeItem<P>) (item.getChildren().get(0));
            return isDummy(child.getValue());
        } catch (Exception e) {
            return true;
        }
    }

    protected void fold(TreeItem<P> node) {
        if (node == null) {
            return;
        }
        List<TreeItem<P>> children = node.getChildren();
        if (children != null) {
            for (TreeItem<P> child : children) {
                fold(child);
                child.setExpanded(false);
            }
        }
        node.setExpanded(false);
    }

    @FXML
    public void refreshAction() {
        loadTree();
    }

    @FXML
    public void treeView() {
        treeView(currectSelected());
    }

    public void treeView(TreeItem<P> node) {
        if (node == null) {
            return;
        }
        P nodeValue = node.getValue();
        if (nodeValue == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private StringBuilder s;

                @Override
                protected boolean handle() {
                    s = new StringBuilder();
                    // https://www.jb51.net/article/116957.htm
                    s.append(" <script>\n"
                            + "    function nodeClicked(id) {\n"
                            + "      var obj = document.getElementById(id);\n"
                            + "      var objv = obj.style.display;\n"
                            + "      if (objv == 'none') {\n"
                            + "        obj.style.display = 'block';\n"
                            + "      } else {\n"
                            + "        obj.style.display = 'none';\n"
                            + "      }\n"
                            + "    }\n"
                            + "    function showClass(className, show) {\n"
                            + "      var nodes = document.getElementsByClassName(className);  　\n"
                            + "      if ( show) {\n"
                            + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                            + "              nodes[i].style.display = '';\n"
                            + "           }\n"
                            + "       } else {\n"
                            + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                            + "              nodes[i].style.display = 'none';\n"
                            + "           }\n"
                            + "       }\n"
                            + "    }\n"
                            + "  </script>\n\n");
                    s.append("<DIV>\n")
                            .append("<DIV>\n")
                            .append("    <INPUT type=\"checkbox\" checked onclick=\"showClass('TreeNode', this.checked);\">")
                            .append(message("Unfold")).append("</INPUT>\n")
                            .append("    <INPUT type=\"checkbox\" checked onclick=\"showClass('SerialNumber', this.checked);\">")
                            .append(message("SequenceNumber")).append("</INPUT>\n")
                            .append("    <INPUT type=\"checkbox\" checked onclick=\"showClass('NodeTag', this.checked);\">")
                            .append(message("Tags")).append("</INPUT>\n")
                            .append("    <INPUT type=\"checkbox\" checked onclick=\"showClass('nodeValue', this.checked);\">")
                            .append(message("Values")).append("</INPUT>\n")
                            .append("</DIV>\n")
                            .append("<HR>\n");
                    try ( Connection conn = DerbyBase.getConnection()) {
                        treeView(conn, nodeValue, 4, "", s);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    s.append("\n<HR>\n<P style=\"font-size:0.8em\">* ").append(message("HtmlEditableComments")).append("</P>\n");
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    WebAddressController c = WebBrowserController.openHtml(
                            HtmlWriteTools.html(chainName(node), HtmlStyles.styleValue("Default"), s.toString()), true);
                }
            };
            start(task);
        }
    }

    public List<TreeItem<P>> ancestor(TreeItem<P> node) {
        if (node == null) {
            return null;
        }
        List<TreeItem<P>> ancestor = null;
        TreeItem<P> parent = node.getParent();
        if (parent != null) {
            ancestor = ancestor(parent);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public void cloneTree(TreeView<P> sourceTreeView, TreeView<P> targetTreeView, P ignore) {
        if (sourceTreeView == null || targetTreeView == null) {
            return;
        }
        TreeItem<P> sourceRoot = sourceTreeView.getRoot();
        if (sourceRoot == null) {
            return;
        }
        TreeItem<P> targetRoot = new TreeItem(sourceRoot.getValue());
        targetTreeView.setRoot(targetRoot);
        targetRoot.setExpanded(sourceRoot.isExpanded());
        cloneNode(sourceRoot, targetRoot, ignore);
        TreeItem<P> selected = sourceTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            P sourceSelected = selected.getValue();
            if (ignore == null || !equal(ignore, sourceSelected)) {
                select(targetTreeView, sourceSelected);
            }
        }
    }

    public void cloneNode(TreeItem<P> sourceNode, TreeItem<P> targetNode, P ignore) {
        if (sourceNode == null || targetNode == null) {
            return;
        }
        List<TreeItem<P>> sourceChildren = sourceNode.getChildren();
        if (sourceChildren == null) {
            return;
        }
        for (TreeItem<P> sourceChild : sourceChildren) {
            if (ignore != null && equal(sourceChild.getValue(), ignore)) {
                continue;
            }
            TreeItem<P> targetChild = new TreeItem<>(sourceChild.getValue());
            targetChild.setExpanded(sourceChild.isExpanded());
            targetNode.getChildren().add(targetChild);
            cloneNode(sourceChild, targetChild, ignore);
        }
    }

    public TreeItem<P> currectSelected() {
        TreeItem<P> selecteItem = treeView.getSelectionModel().getSelectedItem();
        if (selecteItem == null) {
            selecteItem = treeView.getRoot();
        }
        return selecteItem;
    }

    public void select(P node) {
        select(treeView, node);
    }

    public void select(TreeView<P> treeView, P node) {
        if (treeView == null || node == null) {
            return;
        }
        select(find(treeView.getRoot(), node));
    }

    public void select(TreeItem<P> nodeitem) {
        if (treeView == null || nodeitem == null) {
            return;
        }
        isSettingValues = true;
        treeView.getSelectionModel().select(nodeitem);
        isSettingValues = false;
        treeView.scrollTo(treeView.getRow(nodeitem));
        itemSelected(nodeitem);
    }

    public TreeItem<P> find(P node) {
        if (treeView == null || node == null) {
            return null;
        }
        return find(treeView.getRoot(), node);
    }

    public TreeItem<P> find(TreeItem<P> item, P node) {
        if (item == null || node == null) {
            return null;
        }
        if (equal(node, item.getValue())) {
            return item;
        }
        List<TreeItem<P>> children = item.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<P> child : children) {
            TreeItem<P> find = find(child, node);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    @Override
    public void cleanPane() {
        try {
            ignoreNode = null;

        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
