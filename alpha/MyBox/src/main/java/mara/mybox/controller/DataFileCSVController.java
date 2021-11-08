package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFileCSV;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

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

            setDataType(Data2D.Type.DataFileCSV);
            dataFileCSV = (DataFileCSV) dataController.data2D;
            dataFileCSV.setFileController(this);
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
        dataFileCSV.setCharset(csvReadController.charset);
        dataFileCSV.setSourceCsvDelimiter(csvReadController.delimiter);
        dataFileCSV.setAutoDetermineSourceCharset(csvReadController.autoDetermine);
        dataFileCSV.setHasHeader(csvReadController.withNamesCheck.isSelected());
    }

    public void setFile(File file, Charset charset, boolean withName, char delimiter) {
        sourceFile = file;
        csvReadController.withNamesCheck.setSelected(withName);
        csvReadController.setDelimiter(delimiter);
        csvReadController.setCharset(charset);
        dataFileCSV.setUserSavedDataDefinition(false);
        dataFileCSV.setCharset(charset);
        dataFileCSV.setSourceCsvDelimiter(delimiter);
        dataFileCSV.setAutoDetermineSourceCharset(false);
        dataFileCSV.setHasHeader(withName);
        loadFile();
    }

    @Override
    protected void updateInfoLabel() {
        String info = "";
        if (sourceFile != null) {
            info = message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + message("Charset") + ": " + dataFileCSV.getCharset() + "\n"
                    + message("Delimiter") + ": " + TextTools.delimiterMessage(dataFileCSV.getSourceCsvDelimiter() + "") + "\n"
                    + message("FirstLineAsNames") + ": " + (dataFileCSV.isHasHeader() ? message("Yes") : message("No")) + "\n";
        }
        if (!dataFileCSV.isMutiplePages()) {
            info += message("RowsNumber") + ":" + dataFileCSV.tableRowsNumber() + "\n";
        } else {
            info += message("LinesNumberInFile") + ":" + dataFileCSV.getDataSize() + "\n";
        }
        info += message("ColumnsNumber") + ": " + dataFileCSV.columnsNumber() + "\n"
                + message("CurrentPage") + ": " + StringTools.format(dataFileCSV.getCurrentPage() + 1)
                + " / " + StringTools.format(dataFileCSV.getPagesNumber()) + "\n";
        if (dataFileCSV.isMutiplePages() && dataFileCSV.hasData()) {
            info += message("RowsRangeInPage")
                    + ": " + StringTools.format(dataFileCSV.getStartRowOfCurrentPage() + 1) + " - "
                    + StringTools.format(dataFileCSV.getStartRowOfCurrentPage() + dataFileCSV.tableRowsNumber())
                    + " ( " + StringTools.format(dataFileCSV.tableRowsNumber()) + " )\n";
        }
        info += message("PageModifyTime") + ": " + DateTools.nowString();
        fileInfoLabel.setText(info);
    }

    @Override
    public String savePageData() {
        return savePageData(dataFileCSV.getFile(), dataFileCSV.getCharset(), dataFileCSV.getSourceCsvFormat(), dataFileCSV.isHasHeader());
    }

    @Override
    public String savePageDataAs(File file) {
        CSVFormat targetCsvFormat = CSVFormat.DEFAULT
                .withDelimiter(csvWriteController.delimiter)
                .withIgnoreEmptyLines().withTrim().withNullString("");
        boolean hasHeader = csvWriteController.withNamesCheck.isSelected();
        if (hasHeader) {
            targetCsvFormat = targetCsvFormat.withFirstRecordAsHeader();
        }
        return savePageData(file, csvWriteController.charset, targetCsvFormat, hasHeader);
    }

    public String savePageData(File tfile, Charset charset, CSVFormat csvFormat, boolean withName) {
        File tmpFile = TmpFileTools.getTempFile();
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        if (sourceFile != null) {
            try ( CSVParser parser = CSVParser.parse(sourceFile, dataFileCSV.getCharset(), dataFileCSV.getSourceCsvFormat());
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), csvFormat)) {
                if (withName) {
                    csvPrinter.printRecord(dataFileCSV.columnNames());
                }
                long index = -1;
                for (CSVRecord record : parser) {
                    if (++index < dataFileCSV.getStartRowOfCurrentPage() || index >= dataFileCSV.getEndRowOfCurrentPage()) {
                        csvPrinter.printRecord(record);
                    } else if (index == dataFileCSV.getStartRowOfCurrentPage()) {
                        writePageData(csvPrinter);
                    }
                }
                if (index < 0) {
                    writePageData(csvPrinter);
                }
            } catch (Exception e) {
                MyBoxLog.console(e);
                return e.toString();
            }
        } else {
            try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), csvFormat)) {
                if (withName) {
                    csvPrinter.printRecord(dataFileCSV.columnNames());
                }
                writePageData(csvPrinter);
            } catch (Exception e) {
                MyBoxLog.console(e);
                return e.toString();
            }
        }
        if (FileTools.rename(tmpFile, tfile, false)) {
//            saveDefinition(tfile.getAbsolutePath(), dataType, charset, csvFormat.getDelimiter() + "", withName, columns);
            return null;
        } else {
            return "Failed";
        }
    }

    protected void writePageData(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null) {
                return;
            }
            for (int r = 0; r < dataFileCSV.columnsNumber(); r++) {
                csvPrinter.printRecord(dataFileCSV.rowList(r));
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @Override
    public void open(File file) {
        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
        controller.sourceFileChanged(file);
    }

    public void loadData(List<StringTable> tables) {
        if (tables == null || tables.isEmpty()) {
            return;
        }
//        if (tables.size() == 1) {
//            StringTable table = tables.get(0);
//            String[][] data = TextTools.toArray(table.getData());
//            if (data == null || data.length == 0) {
//                return;
//            }
//            List<ColumnDefinition> dataColumns = null;
//            List<String> names = table.getNames();
//            if (names != null && !names.isEmpty()) {
//                dataColumns = new ArrayList<>();
//                for (String name : names) {
//                    dataColumns.add(new ColumnDefinition(name, ColumnDefinition.ColumnType.String));
//                }
//            }
//            loadData(data, dataColumns);
//            return;
//        }
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
