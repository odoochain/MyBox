package mara.mybox.controller;

import java.io.File;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import mara.mybox.data.FileEditInformation;
import static mara.mybox.data.FileEditInformation.defaultCharset;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class BaseFileEditorController_File extends BaseFileEditorController_Main {

    @Override
    public void sourceFileChanged(File file) {
        sourceFile = null;
        sourceInformation = null;
        openFile(file);
    }

    public void openFile(File file) {
        if (editType == FileEditInformation.Edit_Type.Bytes) {
            openBytesFile(file);
        } else {
            openTextFile(file);
        }
    }

    private void openTextFile(File file) {
        if (file == null) {
            return;
        }
        initPage(file);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            bottomLabel.setText(message("CheckingEncoding"));
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (sourceInformation == null) {
                        return false;
                    }
                    if (!sourceInformation.isCharsetDetermined()) {
                        sourceInformation.setLineBreak(TextTools.checkLineBreak(sourceFile));
                        sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
                        return sourceInformation.checkCharset();
                    } else {
                        return true;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    bottomLabel.setText("");
                    isSettingValues = true;
                    sourceInformation.setCharsetDetermined(true);
                    if (currentLineBreak != null) {
                        currentLineBreak.setText(sourceInformation.getLineBreak().toString());
                    }
                    if (charsetSelector != null) {
                        charsetSelector.getSelectionModel().select(sourceInformation.getCharset().name());
                        if (sourceInformation.isWithBom()) {
                            bomLabel.setText(message("WithBom"));
                        } else {
                            bomLabel.setText("");
                        }
                    }
                    isSettingValues = false;
                    loadPage();
                }

                @Override
                protected void whenFailed() {
                    bottomLabel.setText("");
                    super.whenFailed();
                }

            };
            start(task);
        }
    }

    private void openBytesFile(File file) {
        if (file == null) {
            return;
        }
        if (lineBreak == FileEditInformation.Line_Break.Value && lineBreakValue == null
                || lineBreak == FileEditInformation.Line_Break.Width && lineBreakWidth <= 0) {
            popError(message("WrongLineBreak"));
            formatPane.setExpanded(true);
            return;
        }
        initPage(file);
        sourceInformation.setLineBreak(lineBreak);
        sourceInformation.setLineBreakValue(lineBreakValue);
        sourceInformation.setLineBreakWidth(lineBreakWidth);
        loadPage();

    }

    protected void loadTotalNumbers() {
        if (sourceInformation == null || sourceFile == null
                || sourceInformation.isTotalNumberRead()) {
            return;
        }
        synchronized (this) {
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }
            backgroundTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    ok = sourceInformation.readTotalNumbers();
                    return ok;
                }

                @Override
                protected void whenSucceeded() {
                    updateNumbers(false);
                }

            };
            start(backgroundTask, false, null);
        }
    }

    protected void initPage(File file) {
        try {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }

            isSettingValues = true;

            fileChanged = new SimpleBooleanProperty(false);
            sourceFile = file;
            if (backupController != null) {
                backupController.loadBackups(sourceFile);
            }

            FileEditInformation existedInfo = sourceInformation;
            sourceInformation = FileEditInformation.newEditInformation(editType, file);
            if (existedInfo != null) {
                sourceInformation.setCharset(existedInfo.getCharset());
                sourceInformation.setWithBom(existedInfo.isWithBom());
                sourceInformation.setLineBreak(existedInfo.getLineBreak());
                sourceInformation.setLineBreakValue(TextTools.lineBreakValue(sourceInformation.getLineBreak()));
                sourceInformation.setCurrentPage(existedInfo.getCurrentPage());
            } else {
                sourceInformation.setCurrentPage(1);
            }
            sourceInformation.setPageSize(UserConfig.getInt(baseName + "PageSize", defaultPageSize));
            sourceInformation.setFindReplace(null);

            filterConditionsString = "";

            mainArea.clear();
            lineArea.clear();
            bottomLabel.setText("");
            fileLabel.setText("");
            selectionLabel.setText("");
            if (charsetSelector != null) {
                charsetSelector.getSelectionModel().select(defaultCharset().name());
                charsetSelector.setDisable(false);
            }
            if (bomLabel != null) {
                bomLabel.setText("");
            }
            if (currentLineBreak != null) {
                switch (System.lineSeparator()) {
                    case "\r\n":
                        crlfRadio.fire();
                        currentLineBreak.setText("CRLF");
                        break;
                    case "\r":
                        crRadio.fire();
                        currentLineBreak.setText("CR");
                        break;
                    default:
                        lfRadio.fire();
                        currentLineBreak.setText("LF");
                        break;
                }
            }
            if (filterConditionsLabel != null) {
                filterConditionsLabel.setText("");
            }
            recoverButton.setDisable(file == null);
            clearPairArea();

            isSettingValues = false;

            mainArea.requestFocus();

            if (findReplaceController != null) {
                findReplaceController.lastFileRange = null;
                findReplaceController.lastStringRange = null;
                findReplaceController.findReplace = null;
                setControlsStyle();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            isSettingValues = false;
        }
    }

    protected void loadPage() {
        if (sourceInformation == null || sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            bottomLabel.setText(message("ReadingFile"));
            task = new SingletonTask<Void>() {

                private String text;

                @Override
                protected boolean handle() {
                    text = sourceInformation.readPage();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    bottomLabel.setText("");
                    if (text != null) {
                        isSettingValues = true;
                        mainArea.setText(text);
                        isSettingValues = false;
                        formatMainArea();
                        updateInterface(false);
                        updateCursor();
                        if (!sourceInformation.isTotalNumberRead()) {
                            loadTotalNumbers();
                        }

                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    taskCanceled();
                }

            };
            start(task);
        }
    }

    public void setPageSize() {
        try {
            if (isSettingValues || !checkBeforeNextAction()) {
                return;
            }
            int v = Integer.valueOf(pageSizeSelector.getValue().replaceAll(",", ""));
            int available = (int) (SystemTools.freeBytes() / 4);
            if (v > available) {
                popError(message("MayOutOfMemory"));
                v = available;
            } else if (v <= 0) {
                pageSizeSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                popError(message("InvalidParameters"));
                return;
            }
            pageSizeSelector.getEditor().setStyle(null);
            UserConfig.setInt(baseName + "PageSize", v);
            sourceInformation.setPageSize(v);
            sourceInformation.setCurrentPage(1);
            if (sourceInformation.getLineBreak() == FileEditInformation.Line_Break.Width) {
                sourceInformation.setTotalNumberRead(false);
                openFile(sourceFile);
            } else {
                loadPage();
            }
        } catch (Exception e) {
            pageSizeSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            popError(message("InvalidParameters"));
        }
    }

    protected boolean checkCurrentPage() {
        if (isSettingValues || pageSelector == null || !checkBeforeNextAction()) {
            return false;
        }
        String value = pageSelector.getEditor().getText();
        try {
            int v = Integer.valueOf(value);
            if (v > 0 && v <= sourceInformation.getPagesNumber()) {
                sourceInformation.setCurrentPage(v);
                if (findReplaceController != null) {
                    findReplaceController.lastFileRange = null;
                }
                pageSelector.getEditor().setStyle(null);
                loadPage();
                return true;
            } else {
                pageSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                return false;
            }
        } catch (Exception e) {
            pageSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            return false;
        }
    }

    @FXML
    public void goPage() {
        checkCurrentPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (sourceInformation.getObjectsNumber() <= sourceInformation.getCurrentPageObjectEnd()) {
            pageNextButton.setDisable(true);
        } else {
            sourceInformation.setCurrentPage(sourceInformation.getCurrentPage() + 1);
            if (findReplaceController != null) {
                findReplaceController.lastFileRange = null;
            }
            loadPage();
        }
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (sourceInformation.getCurrentPage() <= 1) {
            pagePreviousButton.setDisable(true);
        } else {
            sourceInformation.setCurrentPage(sourceInformation.getCurrentPage() - 1);
            if (findReplaceController != null) {
                findReplaceController.lastFileRange = null;
            }
            loadPage();
        }
    }

    @FXML
    @Override
    public void pageFirstAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        sourceInformation.setCurrentPage(1);
        if (findReplaceController != null) {
            findReplaceController.lastFileRange = null;
        }
        loadPage();
    }

    @FXML
    @Override
    public void pageLastAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        sourceInformation.setCurrentPage(sourceInformation.getPagesNumber());
        if (findReplaceController != null) {
            findReplaceController.lastFileRange = null;
        }
        loadPage();
    }

    public void loadContents(String contents) {
        createAction();
        mainArea.setText(contents);
        updateInterface(true);
    }

}