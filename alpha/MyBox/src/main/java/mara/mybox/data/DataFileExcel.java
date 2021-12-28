package mara.mybox.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileExcel extends DataFile {

    protected List<String> sheetNames;
    protected boolean currentSheetOnly;

    public DataFileExcel() {
        type = Type.Excel;
    }

    public void setOptions(boolean hasHeader) {
        options = new HashMap<>();
        options.put("hasHeader", hasHeader);
    }

    @Override
    public void applyOptions() {
        try {
            if (options == null) {
                return;
            }
            if (options.containsKey("hasHeader")) {
                hasHeader = (boolean) (options.get("hasHeader"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        if (conn == null || type == null || file == null || sheet == null) {
            return null;
        }
        return tableData2DDefinition.queryFileSheet(conn, type, file, sheet);
    }

    public void initFile(File file, String sheetName) {
        super.initFile(file);
        sheet = sheetName;
        sheetNames = null;
    }

    @Override
    public boolean checkForSave() {
        if (sheet == null) {
            sheet = new Date().getTime() + "";
        }
        if (dataName == null || dataName.isBlank()) {
            if (!isTmpData()) {
                dataName = file.getName();
            } else {
                dataName = DateTools.nowString();
            }
            dataName += " - " + sheet;
        }
        return true;
    }

    @Override
    public long readDataDefinition() {
        try ( Workbook wb = WorkbookFactory.create(file)) {
            int sheetsNumber = wb.getNumberOfSheets();
            sheetNames = new ArrayList<>();
            for (int i = 0; i < sheetsNumber; i++) {
                sheetNames.add(wb.getSheetName(i));
            }
            if (sheet == null && sheetsNumber > 0) {
                sheet = wb.getSheetAt(0).getSheetName();
            }
            wb.close();
        } catch (Exception e) {
        }
        return super.readDataDefinition();
    }

    @Override
    public List<String> readColumns() {
        if (file == null || !file.exists() || file.length() == 0) {
            hasHeader = false;
            return null;
        }
        List<String> names = null;
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            if (sourceSheet == null) {
                return null;
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator == null) {
                return null;
            }
            Row firstRow = null;
            while (iterator.hasNext()) {
                firstRow = iterator.next();
                if (firstRow != null) {
                    break;
                }
            }
            if (firstRow == null) {
                return null;
            }
            List values = new ArrayList<>();
            for (int col = firstRow.getFirstCellNum(); col < firstRow.getLastCellNum(); col++) {
                String v = MicrosoftDocumentTools.cellString(firstRow.getCell(col));
                values.add(v);
            }
            if (hasHeader && StringTools.noDuplicated(values, true)) {
                names = values;
            } else {
                hasHeader = false;
                names = new ArrayList<>();
                for (int i = 1; i <= values.size(); i++) {
                    names.add(colPrefix() + i);
                }
            }
            wb.close();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return names;
    }

    @Override
    public long readTotal() {
        dataSize = 0;
        if (file == null || !file.exists() || file.length() == 0) {
            return 0;
        }
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    if (backgroundTask == null || backgroundTask.isCancelled()) {
                        dataSize = 0;
                        break;
                    }
                    try {
                        if (iterator.next() != null) {
                            dataSize++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
            wb.close();
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return -1;
        }
        if (hasHeader && dataSize > 0) {
            dataSize--;
        }
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        if (file == null || !file.exists() || file.length() == 0) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        List<List<String>> rows = new ArrayList<>();
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                long rowIndex = -1;
                int columnsNumber = columnsNumber();
                long rowsEnd = startRowOfCurrentPage + pageSize;
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        if (++rowIndex < startRowOfCurrentPage) {
                            continue;
                        }
                        if (rowIndex >= rowsEnd) {
                            break;
                        }
                        List<String> row = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            row.add(v);
                            if (row.size() >= columnsNumber) {
                                break;
                            }
                        }
                        for (int col = row.size(); col < columnsNumber; col++) {
                            row.add(null);
                        }
                        row.add(0, "" + (rowIndex + 1));
                        rows.add(row);
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
        endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        return rows;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataFileExcel)) {
            return false;
        }
        DataFileExcel targetExcelFile = (DataFileExcel) targetData;
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = targetExcelFile.getFile();
        if (tFile == null) {
            return false;
        }
        targetExcelFile.checkForLoad();
        boolean targetHasHeader = targetExcelFile.isHasHeader();
        String targetSheetName = targetExcelFile.getSheet();
        checkForLoad();
        if (file != null && file.exists() && file.length() > 0) {
            try ( Workbook sourceBook = WorkbookFactory.create(file)) {
                Sheet sourceSheet;
                if (sheet != null) {
                    sourceSheet = sourceBook.getSheet(sheet);
                } else {
                    sourceSheet = sourceBook.getSheetAt(0);
                    sheet = sourceSheet.getSheetName();
                }
                if (targetSheetName == null) {
                    targetSheetName = sheet;
                }
                Workbook targetBook;
                Sheet targetSheet;
                File tmpDataFile = null;
                int sheetsNumber = sourceBook.getNumberOfSheets();
                if (sheetsNumber == 1
                        || (!file.equals(tFile) && targetExcelFile.isCurrentSheetOnly())) {
                    targetBook = new XSSFWorkbook();
                    targetSheet = targetBook.createSheet(targetSheetName);
                } else {
                    tmpDataFile = TmpFileTools.getTempFile();
                    FileCopyTools.copyFile(file, tmpDataFile);
                    targetBook = WorkbookFactory.create(tmpDataFile);
                    int index = targetBook.getSheetIndex(sheet);
                    targetBook.removeSheetAt(index);
                    targetSheet = targetBook.createSheet(targetSheetName);
                    targetBook.setSheetOrder(targetSheetName, index);
                }
                int targetRowIndex = 0;
                if (targetHasHeader) {
                    targetRowIndex = writeHeader(targetSheet, targetRowIndex);
                }
                Iterator<Row> iterator = sourceSheet.iterator();
                if (iterator != null && iterator.hasNext()) {
                    if (hasHeader) {
                        while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                        }
                    }
                    int sourceRowIndex = -1;
                    while (iterator.hasNext() && task != null && !task.isCancelled()) {
                        Row sourceRow = iterator.next();
                        if (sourceRow == null) {
                            continue;
                        }
                        if (++sourceRowIndex < startRowOfCurrentPage || sourceRowIndex >= endRowOfCurrentPage) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            writeFileRow(sourceRow, targetRow);
                        } else if (sourceRowIndex == startRowOfCurrentPage) {
                            targetRowIndex = writePageData(targetSheet, targetRowIndex);
                        }
                    }
                } else {
                    writePageData(targetSheet, targetRowIndex);
                }
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
                FileDeleteTools.delete(tmpDataFile);
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }

        } else {
            try ( Workbook targetBook = new XSSFWorkbook()) {
                if (targetSheetName == null) {
                    targetSheetName = message("Sheet") + "1";
                }
                Sheet targetSheet = targetBook.createSheet(targetSheetName);
                int targetRowIndex = 0;
                if (targetHasHeader) {
                    targetRowIndex = writeHeader(targetSheet, targetRowIndex);
                }
                writePageData(targetSheet, targetRowIndex);
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        return FileTools.rename(tmpFile, tFile, false);
    }

    protected int writeHeader(Sheet targetSheet, int targetRowIndex) {
        if (!isColumnsValid()) {
            return targetRowIndex;
        }
        int index = targetRowIndex;
        Row targetRow = targetSheet.createRow(index++);
        for (int col = 0; col < columns.size(); col++) {
            Cell targetCell = targetRow.createCell(col, CellType.STRING);
            targetCell.setCellValue(columns.get(col).getName());
        }
        return index;
    }

    protected int writePageData(Sheet targetSheet, int targetRowIndex) {
        int index = targetRowIndex;
        try {
            if (!isColumnsValid()) {
                return index;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return index;
                }
                List<String> values = tableRowWithoutNumber(r);
                Row targetRow = targetSheet.createRow(index++);
                for (int col = 0; col < values.size(); col++) {
                    Cell targetCell = targetRow.createCell(col, CellType.STRING);
                    targetCell.setCellValue(values.get(col));
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
        return index;
    }

    public void writeFileRow(Row sourceRow, Row targetRow) {
        try {
            List<String> row = new ArrayList<>();
            for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                String v = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                row.add(v);
            }
            List<String> fileRow = fileRow(row);
            for (int col = 0; col < fileRow.size(); col++) {
                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                targetCell.setCellValue(fileRow.get(col));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public File tmpFile(List<String> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            File tmpFile = TmpFileTools.excelFile();
            Workbook targetBook = new XSSFWorkbook();
            Sheet targetSheet = targetBook.createSheet(message("Sheet") + "1");
            int targetRowIndex = 0;
            if (cols != null && !cols.isEmpty()) {
                Row targetRow = targetSheet.createRow(targetRowIndex++);
                for (int col = 0; col < cols.size(); col++) {
                    Cell targetCell = targetRow.createCell(col, CellType.STRING);
                    targetCell.setCellValue(cols.get(col));
                }
            }
            if (data != null) {
                for (int r = 0; r < data.size(); r++) {
                    if (task != null && task.isCancelled()) {
                        break;
                    }
                    List<String> values = data.get(r);
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int col = 0; col < values.size(); col++) {
                        Cell targetCell = targetRow.createCell(col, CellType.STRING);
                        targetCell.setCellValue(values.get(col));
                    }
                }
            }
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

    public boolean newSheet(String sheetName) {
        if (file == null || !file.exists() || file.length() == 0) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        if (file.length() > 0) {
            FileCopyTools.copyFile(file, tmpDataFile);
        }
        try ( Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            Sheet targetSheet = targetBook.createSheet(sheetName);
            List<List<String>> data = tmpData(3, 3);
            for (int r = 0; r < data.size(); r++) {
                if (task == null || task.isCancelled()) {
                    break;
                }
                List<String> values = data.get(r);
                Row targetRow = targetSheet.createRow(r);
                for (int col = 0; col < values.size(); col++) {
                    Cell targetCell = targetRow.createCell(col, CellType.STRING);
                    targetCell.setCellValue(values.get(col));
                }
            }
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return false;
            }
            if (FileTools.rename(tmpFile, file)) {
                initFile(file);
                hasHeader = false;
                sheet = sheetName;
                tableData2DDefinition.insertData(this);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    public boolean renameSheet(String newName) {
        if (file == null || !file.exists() || sheet == null) {
            return false;
        }
        String oldName = sheet;
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        try ( Workbook book = WorkbookFactory.create(tmpDataFile)) {
            book.setSheetName(book.getSheetIndex(sheet), newName);
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                book.write(fileOut);
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return false;
            }
            if (FileTools.rename(tmpFile, file)) {
                sheet = newName;
                sheetNames.set(sheetNames.indexOf(oldName), sheet);
                tableData2DDefinition.updateData(this);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    public int deleteSheet(String name) {
        if (file == null || !file.exists()) {
            return -1;
        }
        File tmpFile = TmpFileTools.getTempFile();
        File tmpDataFile = TmpFileTools.getTempFile();
        FileCopyTools.copyFile(file, tmpDataFile);
        int index = -1;
        try ( Workbook targetBook = WorkbookFactory.create(tmpDataFile)) {
            index = targetBook.getSheetIndex(name);
            if (index >= 0) {
                targetBook.removeSheetAt(index);
                try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                    targetBook.write(fileOut);
                }
            }
        } catch (Exception e) {
            error = e.toString();
            return -1;
        }
        try {
            FileDeleteTools.delete(tmpDataFile);
            if (tmpFile == null || !tmpFile.exists()) {
                return -1;
            }
            if (index < 0) {
                return -1;
            }
            if (FileTools.rename(tmpFile, file)) {
                return index;
            } else {
                return -1;
            }
        } catch (Exception e) {
            error = e.toString();
            return -1;
        }
    }

    @Override
    public boolean export(ControlDataConvert convertController, List<Integer> colIndices) {
        if (convertController == null || file == null || !file.exists() || file.length() == 0
                || colIndices == null || colIndices.isEmpty()) {
            return false;
        }
        try ( Workbook sourceBook = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = sourceBook.getSheet(sheet);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    List<String> dataRow = new ArrayList<>();
                    for (int cellIndex = sourceRow.getFirstCellNum(); cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
                        String v = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                        dataRow.add(v);
                    }
                    export(convertController, colIndices, dataRow);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            convertController.updateLogs(e.toString());
            return false;
        }
        task = null;
        return true;
    }

    @Override
    public List<List<String>> allRows(List<Integer> cols) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        List<String> record = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            record.add(v);
                        }
                        if (record.isEmpty()) {
                            continue;
                        }
                        List<String> row = new ArrayList<>();
                        for (int col : cols) {
                            if (col >= 0 && col < record.size()) {
                                row.add(record.get(col));
                            } else {
                                row.add(null);
                            }
                        }
                        if (!row.isEmpty()) {
                            rows.add(row);
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return rows;
    }

    @Override
    public DoubleStatistic[] statisticData(List<Integer> cols) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
        }
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        List<String> record = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            record.add(v);
                        }
                        if (record.isEmpty()) {
                            continue;
                        }
                        for (int c = 0; c < colLen; c++) {
                            sData[c].count++;
                            int col = cols.get(c);
                            if (col < 0 || col >= record.size()) {
                                continue;
                            }
                            double v = doubleValue(record.get(col));
                            sData[c].sum += v;
                            if (v > sData[c].maximum) {
                                sData[c].maximum = v;
                            }
                            if (v < sData[c].minimum) {
                                sData[c].minimum = v;
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        boolean allInvalid = true;
        for (int c = 0; c < colLen; c++) {
            if (sData[c].count != 0) {
                sData[c].mean = sData[c].sum / sData[c].count;
                allInvalid = false;
            } else {
                sData[c].mean = AppValues.InvalidDouble;
                sData[c].variance = AppValues.InvalidDouble;
                sData[c].skewness = AppValues.InvalidDouble;
            }
        }
        if (allInvalid) {
            return sData;
        }
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        List<String> record = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            record.add(v);
                        }
                        if (record.isEmpty()) {
                            continue;
                        }
                        for (int c = 0; c < colLen; c++) {
                            if (sData[c].count == 0) {
                                continue;
                            }
                            int col = cols.get(c);
                            if (col < 0 || col >= record.size()) {
                                continue;
                            }
                            double v = doubleValue(record.get(col));
                            sData[c].variance += Math.pow(v - sData[c].mean, 2);
                            sData[c].skewness += Math.pow(v - sData[c].mean, 3);
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        for (int c = 0; c < colLen; c++) {
            if (sData[c].count == 0) {
                continue;
            }
            sData[c].variance = Math.sqrt(sData[c].variance / sData[c].count);
            sData[c].skewness = Math.cbrt(sData[c].skewness / sData[c].count);
        }

        return sData;
    }

    @Override
    public boolean setValue(List<Integer> cols, String value) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( Workbook sourceBook = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = sourceBook.getSheet(sheet);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Workbook targetBook;
            Sheet targetSheet;
            File tmpDataFile = null;
            int sheetsNumber = sourceBook.getNumberOfSheets();
            if (sheetsNumber == 1) {
                targetBook = new XSSFWorkbook();
                targetSheet = targetBook.createSheet(sheet);
            } else {
                tmpDataFile = TmpFileTools.getTempFile();
                FileCopyTools.copyFile(file, tmpDataFile);
                targetBook = WorkbookFactory.create(tmpDataFile);
                int index = targetBook.getSheetIndex(sheet);
                targetBook.removeSheetAt(index);
                targetSheet = targetBook.createSheet(sheet);
                targetBook.setSheetOrder(sheet, index);
            }
            int targetRowIndex = 0;
            if (hasHeader) {
                targetRowIndex = writeHeader(targetSheet, targetRowIndex);
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                boolean isRandom = "MyBox##random".equals(value);
                boolean isRandomNn = "MyBox##randomNn".equals(value);
                Random random = new Random();
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    Row targetRow = targetSheet.createRow(targetRowIndex++);
                    for (int c = sourceRow.getFirstCellNum(); c < sourceRow.getLastCellNum(); c++) {
                        String v;
                        if (cols.contains(c)) {
                            if (isRandom) {
                                v = random(random, c, false);
                            } else if (isRandomNn) {
                                v = random(random, c, true);
                            } else {
                                v = value;
                            }
                        } else {
                            v = MicrosoftDocumentTools.cellString(sourceRow.getCell(c));
                        }
                        Cell targetCell = targetRow.createCell(c, CellType.STRING);
                        targetCell.setCellValue(v);
                    }
                }
            }
            try ( FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
                targetBook.write(fileOut);
            }
            targetBook.close();
            FileDeleteTools.delete(tmpDataFile);
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
        return tmpFile != null && tmpFile.exists();
    }

    @Override
    public DataFileCSV copy(List<Integer> cols, boolean rowNumber, boolean colName) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), ".csv");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        int tcolsNumber = 0, trowsNumber = 0;
        try ( Workbook wb = WorkbookFactory.create(file);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), targetFormat)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        List<String> dataRow = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            dataRow.add(v);
                        }
                        List<String> row = new ArrayList<>();
                        if (rowNumber) {
                            row.add((trowsNumber + 1) + "");
                        }
                        for (int i : cols) {
                            if (i >= 0 && i < dataRow.size()) {
                                row.add(dataRow.get(i));
                            } else {
                                row.add(null);
                            }
                        }
                        csvPrinter.printRecord(row);
                        trowsNumber++;
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
    }

    @Override
    public DataFileCSV percentage(List<String> names, List<Integer> cols, boolean withValues) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        MyBoxLog.debug(names);
        int colLen = cols.size();
        double[] sum = new double[colLen];
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        List<String> dataRow = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            dataRow.add(v);
                        }
                        for (int c = 0; c < colLen; c++) {
                            int col = cols.get(c);
                            if (col < 0 || col >= dataRow.size()) {
                                continue;
                            }
                            double v = doubleValue(dataRow.get(col));
                            sum[c] += v;
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), ".csv");
        CSVFormat targetFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(',');
        int trowsNumber = 0;
        try ( Workbook wb = WorkbookFactory.create(file);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), targetFormat)) {
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("Summation"));
            for (int c = 0; c < colLen; c++) {
                row.add(DoubleTools.scale(sum[c], scale) + "");
                if (withValues) {
                    row.add("");
                }
            }
            csvPrinter.printRecord(row);
            trowsNumber++;

            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        List<String> dataRow = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            dataRow.add(v);
                        }
                        row = new ArrayList<>();
                        row.add(trowsNumber++ + "");
                        for (int c = 0; c < colLen; c++) {
                            int col = cols.get(c);
                            double v = 0;
                            if (col >= 0 && col < dataRow.size()) {
                                v = doubleValue(dataRow.get(col));
                            }
                            if (withValues) {
                                row.add(DoubleTools.scale(v, scale) + "");
                            }
                            if (sum[c] == 0) {
                                row.add("0");
                            } else {
                                row.add(DoubleTools.percentage(v, sum[c]));
                            }
                        }
                        csvPrinter.printRecord(row);
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        if (csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
    }

    @Override
    public List<List<String>> allRows(List<Integer> cols, boolean rowNumber) {
        if (file == null || !file.exists() || file.length() == 0
                || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sourceSheet;
            if (sheet != null) {
                sourceSheet = wb.getSheet(sheet);
            } else {
                sourceSheet = wb.getSheetAt(0);
                sheet = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                int index = 1;
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        Row fileRow = iterator.next();
                        if (fileRow == null) {
                            continue;
                        }
                        List<String> record = new ArrayList<>();
                        for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                            String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                            record.add(v);
                        }
                        if (record.isEmpty()) {
                            continue;
                        }
                        List<String> row = new ArrayList<>();
                        if (rowNumber) {
                            row.add(index++ + "");
                        }
                        for (int col : cols) {
                            if (col >= 0 && col < record.size()) {
                                row.add(record.get(col));
                            } else {
                                row.add(null);
                            }
                        }
                        rows.add(row);
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return rows;
    }

    public static DataFileExcel toExcel(SingletonTask task, DataFileCSV csvData) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File excelFile = TmpFileTools.excelFile();
        boolean targetHasHeader = false;
        int tcolsNumber = 0, trowsNumber = 0;
        String targetSheetName = message("Sheet") + "1";
        try ( CSVParser parser = CSVParser.parse(csvFile, csvData.getCharset(), csvData.cvsFormat());
                 Workbook targetBook = new XSSFWorkbook()) {
            Sheet targetSheet = targetBook.createSheet(targetSheetName);
            int targetRowIndex = 0;
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                if (csvData.isHasHeader()) {
                    try {
                        List<String> names = parser.getHeaderNames();
                        if (names != null) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < names.size(); col++) {
                                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                                targetCell.setCellValue(names.get(col));
                            }
                            tcolsNumber = names.size();
                            targetHasHeader = true;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
                            Row targetRow = targetSheet.createRow(targetRowIndex++);
                            for (int col = 0; col < record.size(); col++) {
                                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                                targetCell.setCellValue(record.get(col));
                            }
                            trowsNumber++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                        MyBoxLog.error(e);
                    }
                }
                try ( FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                    targetBook.write(fileOut);
                }
                targetBook.close();
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (excelFile != null && excelFile.exists()) {
            DataFileExcel targetData = new DataFileExcel();
            targetData.setFile(excelFile).setSheet(targetSheetName)
                    .setHasHeader(targetHasHeader)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            return targetData;
        } else {
            return null;
        }
    }


    /*
        get/set
     */
    public List<String> getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public boolean isCurrentSheetOnly() {
        return currentSheetOnly;
    }

    public DataFileExcel setCurrentSheetOnly(boolean currentSheetOnly) {
        this.currentSheetOnly = currentSheetOnly;
        return this;
    }

}
