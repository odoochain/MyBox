package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFile;
import mara.mybox.data.DataFileCSV;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-24
 * @License Apache License Version 2.0
 */
public class DataFileCSVController extends BaseData2DFileController {

    protected DataFileCSV dataFileCSV;

    @FXML
    protected ControlCsvOptions csvReadController, csvWriteController;
    @FXML
    protected VBox mainBox;

    public DataFileCSVController() {
        baseTitle = message("EditCSV");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            setDataType(Data2D.Type.CSV);
            dataFileCSV = (DataFileCSV) dataController.data2D;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            csvReadController.setControls(baseName + "Read");
            csvWriteController.setControls(baseName + "Write");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void pickOptions() {
        dataFileCSV.setDelimiter(csvReadController.delimiter + "");
        if (csvReadController.autoDetermine) {
            dataFileCSV.setCharset(TextFileTools.charset(dataFileCSV.getFile()));
        } else {
            dataFileCSV.setCharset(csvReadController.charset);
        }
        dataFileCSV.setHasHeader(csvReadController.withNamesCheck.isSelected());
    }

    public void setFile(File file, Charset charset, boolean withName, char delimiter) {
        if (file == null || !checkBeforeNextAction()) {
            return;
        }
        sourceFile = file;
        csvReadController.withNamesCheck.setSelected(withName);
        csvReadController.setDelimiter(delimiter);
        csvReadController.setCharset(charset);
        dataFileCSV.setUserSavedDataDefinition(false);
        dataFileCSV.setCharset(charset);
        dataFileCSV.setDelimiter(delimiter + "");
        dataFileCSV.setHasHeader(withName);
        dataController.loadDefinition();
    }

    @Override
    public DataFile makeTargetDataFile(File file) {
        DataFileCSV targetCSVFile = (DataFileCSV) dataFileCSV.cloneAll();
        targetCSVFile.setFile(file);
        targetCSVFile.setD2did(-1);
        targetCSVFile.setCharset(csvWriteController.charset);
        targetCSVFile.setDelimiter(csvWriteController.delimiter + "");
        targetCSVFile.setHasHeader(csvWriteController.withNamesCheck.isSelected());
        return targetCSVFile;
    }

    public void loadData(List<StringTable> tables) {
        if (tables == null || tables.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private File tmpPath;
                private LinkedHashMap<File, Boolean> files;
                private int count;

                @Override
                protected boolean handle() {
                    tmpPath = TmpFileTools.getTempDirectory();
                    files = CsvTools.save(tmpPath, "tmp", tables);
                    count = files != null ? files.size() : 0;
                    return count > 0;
                }

                @Override
                protected void whenSucceeded() {
                    Iterator<File> iterator = files.keySet().iterator();
                    File csvFile = iterator.next();
                    setFile(csvFile, Charset.forName("UTF-8"), files.get(csvFile), ',');
                    if (count > 1) {
                        browseURI(tmpPath.toURI());
                        String info = MessageFormat.format(message("GeneratedFilesResult"),
                                count, "\"" + tmpPath + "\"");
                        int num = 1;
                        info += "\n    " + csvFile.getName();
                        while (iterator.hasNext()) {
                            info += "\n    " + iterator.next().getName();
                            if (++num > 10) {
                                info += "\n    ......";
                                break;
                            }
                        }
                        info += "\n\n" + message("NoticeTmpFiles");
                        alertInformation(info);
                    }
                }

            };
            start(task);
        }
    }

    /*
        static
     */
    public static DataFileCSVController open(File file, Charset charset, boolean withNames, char delimiter) {
        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
        controller.setFile(file, charset, withNames, delimiter);
        return controller;
    }

}
