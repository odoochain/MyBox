package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileInformation;
import mara.mybox.data.FindReplaceString;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileSortTools;
import mara.mybox.tools.FileSortTools.FileSortMode;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesRenameController extends BaseBatchFileController {

    protected List<String> targetDatas;
    protected int currentAccum, digit, startNumber;
    protected RenameType renameType;

    protected List<File> includeFiles;

    @FXML
    protected VBox renameOptionsBox, numberBox, replaceBox;
    @FXML
    protected FlowPane suffixPane, prefixPane, extensionPane;
    @FXML
    protected CheckBox fillZeroCheck, originalCheck, stringCheck, accumCheck,
            suffixCheck, descentCheck, recountCheck, regexCheck;
    @FXML
    protected TextField oldStringInput, newStringInput, newExtInput,
            prefixInput, suffixInput, stringInput;
    @FXML
    protected ToggleGroup sortGroup, renameGroup;
    @FXML
    protected RadioButton targetReplaceRadio, targetSkipRadio, replaceAllRadio;

    public static enum RenameType {
        ReplaceSubString, AppendSuffix, AddPrefix, AddSequenceNumber, ChangeExtension
    }

    public FilesRenameController() {
        baseTitle = message("FilesRename");
    }

    @Override
    public void initTargetSection() {
        try {
            super.initTargetSection();

            renameGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkRenameType();
                }
            });
            checkRenameType();

            fillZeroCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean("FileRenameFillZero", fillZeroCheck.isSelected());
                }
            });
            fillZeroCheck.setSelected(UserConfig.getBoolean("FileRenameFillZero", true));

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableData)
                            .or(tableController.addFilesButton.disableProperty())
            );
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private void checkRenameType() {
        renameOptionsBox.getChildren().clear();

        RadioButton selected = (RadioButton) renameGroup.getSelectedToggle();
        if (message("ReplaceSubString").equals(selected.getText())) {
            renameType = RenameType.ReplaceSubString;
            renameOptionsBox.getChildren().addAll(replaceBox);

        } else if (message("AppendSuffix").equals(selected.getText())) {
            renameType = RenameType.AppendSuffix;
            renameOptionsBox.getChildren().addAll(suffixPane);

        } else if (message("AddPrefix").equals(selected.getText())) {
            renameType = RenameType.AddPrefix;
            renameOptionsBox.getChildren().addAll(prefixPane);

        } else if (message("AddSequenceNumber").equals(selected.getText())) {
            renameType = RenameType.AddSequenceNumber;
            renameOptionsBox.getChildren().addAll(numberBox);

        } else if (message("ChangeExtension").equals(selected.getText())) {
            renameType = RenameType.ChangeExtension;
            renameOptionsBox.getChildren().addAll(extensionPane);
        }
        refreshStyle(renameOptionsBox);

    }

    @Override
    public boolean makeMoreParameters() {
        switch (renameType) {
            case ReplaceSubString:
                if (oldStringInput.getText().isBlank()) {
                    return false;
                }
                break;
            case AddPrefix:
                if (prefixInput.getText().isBlank()) {
                    return false;
                }
                break;
            case AppendSuffix:
                if (suffixInput.getText().isBlank()) {
                    return false;
                }
                break;
            case AddSequenceNumber:
                if (isPreview) {
                    digit = 1;
                } else {
                    sortFileInformations(tableData);
                    try {
                        digit = Integer.valueOf(digitInput.getText());
                    } catch (Exception e) {
                        if (tableController.totalFilesNumber <= 0) {
                            tableController.countSize();
                        }
                        digit = (tableController.totalFilesNumber + "").length();
                    }
                }
                try {
                    startNumber = Integer.valueOf(acumFromInput.getText());
                } catch (Exception e) {
                    startNumber = 0;
                }
                currentAccum = startNumber;
                break;
            case ChangeExtension:

        }
        return super.makeMoreParameters();

    }

    protected FileSortMode checkSortMode() {
        RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
        boolean desc = descentCheck.isSelected();
        FileSortMode sortMode = FileSortMode.ModifyTimeDesc;
        if (message("OriginalFileName").equals(sort.getText())) {
            if (desc) {
                sortMode = FileSortMode.NameDesc;
            } else {
                sortMode = FileSortMode.NameAsc;
            }
        } else if (message("CreateTime").equals(sort.getText())) {
            if (desc) {
                sortMode = FileSortMode.CreateTimeDesc;
            } else {
                sortMode = FileSortMode.CreateTimeAsc;
            }

        } else if (message("ModifyTime").equals(sort.getText())) {
            if (desc) {
                sortMode = FileSortMode.ModifyTimeDesc;
            } else {
                sortMode = FileSortMode.ModifyTimeAsc;
            }

        } else if (message("Size").equals(sort.getText())) {
            if (desc) {
                sortMode = FileSortMode.SizeDesc;
            } else {
                sortMode = FileSortMode.SizeAsc;
            }

        } else if (message("AddedSequence").equals(sort.getText())) {
            sortMode = null;

        }
        return sortMode;

    }

    protected void sortFileInformations(List<FileInformation> files) {
        FileSortMode sortMode = checkSortMode();
        if (sortMode != null) {
            FileSortTools.sortFileInformations(files, sortMode);
        }
    }

    protected void sortFiles(List<File> files) {
        FileSortMode sortMode = checkSortMode();
        if (sortMode != null) {
            FileSortTools.sortFiles(files, sortMode);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        FileInformation f = tableController.row(currentParameters.currentIndex);
        String newName = renameFile(srcFile);
        if (newName != null) {
            f.setFile(new File(newName));
            return message("Successful");
        } else {
            return message("Skipped");
        }
    }

    protected String renameFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try {
            String newName = makeNewFilename(file);
            if (newName == null || newName.isBlank()) {
                return null;
            }
            File newFile = new File(newName);
            if (file.equals(newFile)) {
                return null;
            }
            if (newFile.isFile() && newFile.exists()) {
                if (targetSkipRadio.isSelected()) {
                    return null;
                }
                FileDeleteTools.delete(newFile);
            }
            if (FileTools.rename(file, newFile)) {
                newName = newFile.getAbsolutePath();
                targetFileGenerated(newFile);
                return newName;
            } else {
                return null;
            }
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            return null;
        }
    }

    protected String makeNewFilename(File file) {
        try {
            if (file == null || !file.exists() || !file.isFile()) {
                return null;
            }
            String filePath = file.getParent() + File.separator;
            String currentName = file.getName();
            String newName = null;
            switch (renameType) {
                case ReplaceSubString:
                    if (regexCheck.isSelected()) {
                        if (replaceAllRadio.isSelected()) {
                            newName = currentName.replaceAll(oldStringInput.getText(), FileNameTools.filter(newStringInput.getText()));
                        } else {
                            newName = currentName.replaceFirst(oldStringInput.getText(), FileNameTools.filter(newStringInput.getText()));
                        }
                    } else {
                        if (replaceAllRadio.isSelected()) {
                            newName = FindReplaceString.replaceAll(currentName, oldStringInput.getText(), FileNameTools.filter(newStringInput.getText()));
                        } else {
                            newName = FindReplaceString.replaceFirst(currentName, oldStringInput.getText(), FileNameTools.filter(newStringInput.getText()));
                        }
                    }
                    break;
                case AddPrefix:
                    newName = FileNameTools.filter(prefixInput.getText()) + currentName;
                    break;
                case AppendSuffix:
                    newName = FileNameTools.append(currentName, FileNameTools.filter(suffixInput.getText()));
                    break;
                case AddSequenceNumber:
                    newName = "";
                    if (originalCheck.isSelected()) {
                        newName += FileNameTools.prefix(currentName);
                    }
                    if (stringCheck.isSelected()) {
                        newName += FileNameTools.filter(stringInput.getText());
                    }
                    String pageNumber = currentAccum + "";
                    if (fillZeroCheck.isSelected()) {
                        pageNumber = StringTools.fillLeftZero(currentAccum, digit);
                    }
                    newName += pageNumber;
                    currentAccum++;
                    newName += "." + FileNameTools.suffix(currentName);
                    break;
                case ChangeExtension:
                    newName = FileNameTools.replaceSuffix(currentName, FileNameTools.filter(newExtInput.getText()));
                    break;
            }
            if (newName == null || newName.isBlank()) {
                return null;
            }
            return filePath + newName;
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            return null;
        }
    }

    @Override
    protected boolean handleDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
            return false;
        }
        try {
            File[] srcFiles = sourcePath.listFiles();
            if (srcFiles == null) {
                return false;
            }
            if (recountCheck.isSelected()) {
                currentAccum = startNumber;
            }
            List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(srcFiles));
            if (renameType == RenameType.AddSequenceNumber) {
                sortFiles(files);
                int bdigit = (files.size() + "").length();
                if (digit < bdigit) {
                    digit = bdigit;
                }
            }
            for (File file : files) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                if (file.isFile()) {
                    dirFilesNumber++;
                    if (!match(file)) {
                        continue;
                    }
                    String newName = renameFile(file);
                    if (newName != null) {
                        dirFilesHandled++;
                    }
                } else if (file.isDirectory() && sourceCheckSubdir) {
                    handleDirectory(file, null);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void viewTarget(File file) {
        openTarget(null);
    }

    @Override
    public void openTarget(ActionEvent event) {
        try {
            if (tableData == null || tableData.isEmpty()) {
                return;
            }
            File f = tableData.get(0).getFile();
            if (f.isDirectory()) {
                browseURI(new File(f.getPath()).toURI());
            } else {
                browseURI(new File(f.getParent()).toURI());
            }
            recordFileOpened(f);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popRegexExample(MouseEvent mouseEvent) {
        PopTools.popRegexExample(this, oldStringInput, mouseEvent);
    }

}
