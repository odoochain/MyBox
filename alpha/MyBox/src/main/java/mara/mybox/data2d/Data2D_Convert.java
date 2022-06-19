package mara.mybox.data2d;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mara.mybox.data2d.scan.Data2DReader;
import mara.mybox.data2d.scan.Data2DReader.Operation;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Convert extends Data2D_Edit {

    /*
        to/from database table
     */
    public DataTable createTable(SingletonTask task, Connection conn, String targetName, boolean dropExisted) {
        return createTable(task, conn, columns, targetName, dropExisted);
    }

    public DataTable createTable(SingletonTask task, Connection conn, List<Integer> cols, boolean includeRowNumber) {
        if (conn == null || !isValid() || cols == null || cols.isEmpty()) {
            return null;
        }
        List<Data2DColumn> targetColumns = new ArrayList<>();
        if (includeRowNumber) {
            targetColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
        }
        for (int i = 0; i < columns.size(); i++) {
            if (cols.contains(i)) {
                Data2DColumn column = columns.get(i).cloneAll();
                column.setD2cid(-1).setD2id(-1);
                targetColumns.add(column);
            }
        }
        return createTable(task, conn, targetColumns, tmpTableName(), true);
    }

    public long writeTable(SingletonTask task, Connection conn, DataTable dataTable, List<Integer> cols, boolean includeRowNumber) {
        if (conn == null || dataTable == null || cols == null || cols.isEmpty()) {
            return -1;
        }
        Data2DReader reader = Data2DReader.create(this)
                .setConn(conn).setDataTable(dataTable)
                .setCols(cols).setIncludeRowNumber(includeRowNumber)
                .setReaderTask(task).start(Operation.WriteTable);
        if (reader != null && !reader.isFailed()) {
            return reader.getCount();
        } else {
            return -1;
        }
    }

    public long writeTable(SingletonTask task, Connection conn, DataTable dataTable) {
        if (conn == null || dataTable == null || columns == null || columns.isEmpty()) {
            return -1;
        }
        List<Integer> cols = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            cols.add(i);
        }
        return writeTable(task, conn, dataTable, cols, false);
    }

    public DataTable toTable(SingletonTask task, String targetName, boolean dropExisted) {
        try ( Connection conn = DerbyBase.getConnection()) {
            DataTable dataTable = createTable(task, conn, targetName, dropExisted);
            writeTable(task, conn, dataTable);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataTable toTable(SingletonTask task, List<Integer> cols, boolean includeRowNumber) {
        try ( Connection conn = DerbyBase.getConnection()) {
            DataTable dataTable = createTable(task, conn, cols, includeRowNumber);
            writeTable(task, conn, dataTable, cols, includeRowNumber);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String tmpTableName() {
        return "MyBoxTmp__" + new Date().getTime();
    }

    public static DataTable createTable(SingletonTask task, Connection conn,
            List<Data2DColumn> inColumns, String name, boolean dropExisted) {
        try {
            if (conn == null || inColumns == null || inColumns.isEmpty()) {
                return null;
            }
            if (name == null || name.isBlank()) {
                name = tmpTableName();
            }
            DataTable dataTable = new DataTable();
            TableData2D tableData2D = dataTable.getTableData2D();
            String tableName = DerbyBase.fixedIdentifier(name);

            if (tableData2D.exist(conn, tableName)) {
                if (!dropExisted) {
                    return null;
                }
                dataTable.getTableData2DDefinition().deleteUserTable(conn, tableName);
                conn.commit();
            }

            tableData2D.setTableName(tableName);
            String idname = tableName.replace("\"", "") + "_id";
            Data2DColumn idcolumn = new Data2DColumn(idname, ColumnDefinition.ColumnType.Long);
            idcolumn.setAuto(true).setIsPrimaryKey(true).setNotNull(true).setEditable(false);
            List<Data2DColumn> tableColumns = new ArrayList<>();
            tableColumns.add(idcolumn);
            tableData2D.addColumn(idcolumn);
            Map<String, String> columnsMap = new HashMap<>();
            for (Data2DColumn inColumn : inColumns) {
                Data2DColumn dataColumn = new Data2DColumn();
                dataColumn.cloneFrom(inColumn);
                dataColumn.setD2id(-1);
                dataColumn.setD2cid(-1);
                String columeName = DerbyBase.fixedIdentifier(inColumn.getColumnName());
                if (columeName.equalsIgnoreCase(idname)) {
                    columeName += "m";
                }
                dataColumn.setColumnName(columeName);
                tableColumns.add(dataColumn);
                tableData2D.addColumn(dataColumn);
                columnsMap.put(inColumn.getColumnName(), dataColumn.getColumnName());
            }
            dataTable.setColumnsMap(columnsMap);
            if (conn.createStatement().executeUpdate(tableData2D.createTableStatement()) < 0) {
                return null;
            }
            conn.commit();
            dataTable.recordTable(conn, tableName, tableColumns);
            return dataTable;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataFileCSV toCSV(SingletonTask task, DataTable dataTable, File file, boolean save) {
        if (task == null || dataTable == null || !dataTable.isValid() || file == null) {
            return null;
        }
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size(), trowsNumber = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(tableData2D.queryAllStatement());
                 ResultSet results = statement.executeQuery();
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file, Charset.forName("UTF-8")), CsvTools.csvFormat(',', true))) {
            csvPrinter.printRecord(dataTable.columnNames());
            while (results.next() && task != null && !task.isCancelled()) {
                try {
                    List<String> row = new ArrayList<>();
                    for (int col = 0; col < tcolsNumber; col++) {
                        Data2DColumn column = dataColumns.get(col);
                        Object v = results.getObject(column.getColumnName());
                        row.add(column.toString(v));
                    }
                    csvPrinter.printRecord(row);
                    trowsNumber++;
                } catch (Exception e) {  // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
        if (file != null && file.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(dataTable.getColumns())
                    .setFile(file).setCharset(Charset.forName("UTF-8")).setDelimiter(",")
                    .setHasHeader(true).setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            if (save) {
                targetData.saveAttributes();
            }
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileCSV toCSV(SingletonTask task, DataTable dataTable) {
        File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.shortName(), ".csv");
        return toCSV(task, dataTable, csvFile, true);
    }

    public static DataFileText toText(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File txtFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.shortName(), ".txt");
        DataFileCSV csvData = toCSV(task, dataTable, txtFile, false);
        if (csvData != null && txtFile != null && txtFile.exists()) {
            DataFileText targetData = new DataFileText();
            targetData.setColumns(csvData.getColumns())
                    .setFile(txtFile).setCharset(Charset.forName("UTF-8")).setDelimiter(",")
                    .setHasHeader(true).setColsNumber(csvData.getColsNumber()).setRowsNumber(csvData.getRowsNumber());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileExcel toExcel(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File excelFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.shortName(), ".xlsx");
        String targetSheetName = message("Sheet") + "1";
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size(), trowsNumber = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(tableData2D.queryAllStatement());
                 ResultSet results = statement.executeQuery();
                 Workbook targetBook = new XSSFWorkbook()) {
            Sheet targetSheet = targetBook.createSheet(targetSheetName);
            Row targetRow = targetSheet.createRow(0);
            for (int col = 0; col < tcolsNumber; col++) {
                Cell targetCell = targetRow.createCell(col, CellType.STRING);
                targetCell.setCellValue(dataColumns.get(col).getColumnName());
            }
            while (results.next() && task != null && !task.isCancelled()) {
                try {
                    targetRow = targetSheet.createRow(++trowsNumber);
                    for (int col = 0; col < tcolsNumber; col++) {
                        Data2DColumn column = dataColumns.get(col);
                        Object v = results.getObject(column.getColumnName());
                        Cell targetCell = targetRow.createCell(col, CellType.STRING);
                        targetCell.setCellValue(column.toString(v));
                    }
                } catch (Exception e) {  // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
            try ( FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                targetBook.write(fileOut);
            }
            targetBook.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
        if (excelFile != null && excelFile.exists()) {
            DataFileExcel targetData = new DataFileExcel();
            targetData.setColumns(dataTable.getColumns())
                    .setFile(excelFile).setSheet(targetSheetName)
                    .setHasHeader(true).setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataMatrix toMatrix(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        TableData2D tableData2D = dataTable.getTableData2D();
        List<Data2DColumn> dataColumns = dataTable.getColumns();
        int tcolsNumber = dataColumns.size(), trowsNumber = 0;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement statement = conn.prepareStatement(tableData2D.queryAllStatement());
                 ResultSet results = statement.executeQuery()) {
            while (results.next() && task != null && !task.isCancelled()) {
                try {
                    List<String> row = new ArrayList<>();
                    for (int col = 0; col < tcolsNumber; col++) {
                        Data2DColumn column = dataColumns.get(col);
                        Object v = results.getObject(column.getColumnName());
                        row.add(column.toString(v));
                    }
                    rows.add(row);
                    trowsNumber++;
                } catch (Exception e) {  // skip  bad lines
                    MyBoxLog.error(e);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
        DataMatrix matrix = new DataMatrix();
        if (DataMatrix.save(task, matrix, dataColumns, rows)) {
            return matrix;
        } else {
            return null;
        }
    }

    public static DataClipboard toClip(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File clipFile = DataClipboard.newFile();
        DataFileCSV csvData = toCSV(task, dataTable, clipFile, false);
        if (csvData != null && clipFile != null && clipFile.exists()) {
            return DataClipboard.create(task, csvData.getColumns(), clipFile, csvData.getRowsNumber(), csvData.getColsNumber());
        } else {
            return null;
        }
    }

    public static String toString(SingletonTask task, DataTable dataTable) {
        if (task == null || dataTable == null || !dataTable.isValid()) {
            return null;
        }
        File txtFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), dataTable.shortName(), ".txt");
        DataFileCSV csvData = toCSV(task, dataTable, txtFile, false);
        if (csvData != null && txtFile != null && txtFile.exists()) {
            return TextFileTools.readTexts(txtFile);
        } else {
            return null;
        }
    }

    /*  
        to/from CSV
     */
    public static DataFileCSV save(SingletonTask task, ResultSet results, boolean showRowNumber) {
        try {
            if (results == null) {
                return null;
            }
            File csvFile = TmpFileTools.csvFile();
            long count = 0;
            int colsSize;
            List<Data2DColumn> db2Columns = new ArrayList<>();
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                if (showRowNumber) {
                    names.add(message("SourceRowNumber"));
                }
                ResultSetMetaData meta = results.getMetaData();

                for (int col = 1; col <= meta.getColumnCount(); col++) {
                    String name = meta.getColumnName(col);
                    names.add(name);
                    Data2DColumn dc = new Data2DColumn(name,
                            ColumnDefinition.sqlColumnType(meta.getColumnType(col)),
                            meta.isNullable(col) == ResultSetMetaData.columnNoNulls);
                    db2Columns.add(dc);
                }
                csvPrinter.printRecord(names);
                colsSize = names.size();
                List<String> fileRow = new ArrayList<>();
                while (results.next() && task != null && !task.isCancelled()) {
                    count++;
                    if (showRowNumber) {
                        fileRow.add(count + "");
                    }
                    for (Data2DColumn column : db2Columns) {
                        Object v = results.getObject(column.getColumnName());
                        fileRow.add(v == null ? "" : column.toString(v));
                    }
                    csvPrinter.printRecord(fileRow);
                    fileRow.clear();
                }
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return null;
            }
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(colsSize).setRowsNumber(count);
            if (showRowNumber) {
                db2Columns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            targetData.setColumns(db2Columns);
            return targetData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static DataFileExcel toExcel(SingletonTask task, DataFileCSV csvData) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File excelFile = new File(FileNameTools.replaceSuffix(csvFile.getAbsolutePath(), "xlsx"));
        boolean targetHasHeader = false;
        int tcolsNumber = 0, trowsNumber = 0;
        String targetSheetName = message("Sheet") + "1";
        File validFile = FileTools.removeBOM(csvFile);
        try ( CSVParser parser = CSVParser.parse(validFile, csvData.getCharset(), csvData.cvsFormat());
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
            targetData.setColumns(csvData.getColumns())
                    .setFile(excelFile).setSheet(targetSheetName)
                    .setHasHeader(targetHasHeader)
                    .setColsNumber(tcolsNumber).setRowsNumber(trowsNumber);
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataFileText toText(DataFileCSV csvData) {
        if (csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        File txtFile = new File(FileNameTools.replaceSuffix(csvFile.getAbsolutePath(), "txt"));
        if (FileCopyTools.copyFile(csvFile, txtFile)) {
            DataFileText targetData = new DataFileText();
            targetData.cloneAll(csvData);
            targetData.setType(Type.Texts).setFile(txtFile);
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public static DataMatrix toMatrix(SingletonTask task, DataFileCSV csvData) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        List<List<String>> data = csvData.allRows(false);
        List<Data2DColumn> cols = csvData.getColumns();
        if (cols == null || cols.isEmpty()) {
            try ( Connection conn = DerbyBase.getConnection()) {
                csvData.readColumns(conn);
                cols = csvData.getColumns();
                if (cols == null || cols.isEmpty()) {
                    return null;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
        }
        DataMatrix matrix = new DataMatrix();
        if (DataMatrix.save(task, matrix, cols, data)) {
            return matrix;
        } else {
            return null;
        }
    }

    public static DataClipboard toClip(SingletonTask task, DataFileCSV csvData) {
        if (task == null || csvData == null) {
            return null;
        }
        File csvFile = csvData.getFile();
        if (csvFile == null || !csvFile.exists() || csvFile.length() == 0) {
            return null;
        }
        List<Data2DColumn> cols = csvData.getColumns();
        if (cols == null || cols.isEmpty()) {
            try ( Connection conn = DerbyBase.getConnection()) {
                csvData.readColumns(conn);
                cols = csvData.getColumns();
                if (cols == null || cols.isEmpty()) {
                    return null;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
        }
        File dFile = DataClipboard.newFile();
        if (FileCopyTools.copyFile(csvFile, dFile, true, true)) {
            return DataClipboard.create(task, cols, dFile, csvData.getRowsNumber(), cols.size());
        } else {
            MyBoxLog.error("Failed");
            return null;
        }
    }

}
