package mara.mybox.data2d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.data2d.scan.Data2DReader;
import mara.mybox.data2d.scan.Data2DReader.Operation;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ColumnFilter;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Edit extends Data2D_Data {

    public abstract Data2DDefinition queryDefinition(Connection conn);

    public abstract void applyOptions();

    public abstract List<String> readColumnNames();

    public abstract boolean savePageData(Data2D targetData);

    public abstract boolean setValue(List<Integer> cols, String value, boolean errorContinue);

    public abstract boolean delete(boolean errorContinue);

    public abstract long clearData();

    /*
        read
     */
    public boolean checkForLoad() {
        return true;
    }

    public long readDataDefinition(Connection conn) {
        if (isTmpData()) {
            checkForLoad();
            return -1;
        }
        try {
            Data2DDefinition definition = queryDefinition(conn);
            if (definition != null) {
                cloneAll(definition);
            }
            applyOptions();
            checkForLoad();
            if (definition == null) {
                definition = tableData2DDefinition.insertData(conn, this);
                conn.commit();
                d2did = definition.getD2did();
            } else {
                tableData2DDefinition.updateData(conn, this);
                conn.commit();
                d2did = definition.getD2did();
                savedColumns = tableData2DColumn.read(conn, d2did);
            }
            options = null;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.debug(e);
            return -1;
        }
        return d2did;
    }

    public boolean readColumns(Connection conn) {
        try {
            columns = null;
            List<String> colNames = readColumnNames();
            if (colNames == null || colNames.isEmpty()) {
                hasHeader = false;
                columns = savedColumns;
            } else {
                List<String> validNames = new ArrayList<>();
                columns = new ArrayList<>();
                for (int i = 0; i < colNames.size(); i++) {
                    String name = colNames.get(i);
                    Data2DColumn column;
                    if (savedColumns != null && i < savedColumns.size()) {
                        column = savedColumns.get(i);
                        if (!hasHeader) {
                            name = column.getColumnName();
                        }
                    } else {
                        column = new Data2DColumn(name, defaultColumnType());
                    }
                    String vname = (name == null || name.isBlank()) ? message("Column") + (i + 1) : name;
                    while (validNames.contains(vname)) {
                        vname += "m";
                    }
                    validNames.add(vname);
                    column.setColumnName(vname);
                    columns.add(column);
                }
            }
            if (columns != null && !columns.isEmpty()) {
                Random random = new Random();
                for (int i = 0; i < columns.size(); i++) {
                    Data2DColumn column = columns.get(i);
                    column.setD2id(d2did);
                    column.setIndex(i);
                    if (column.getColor() == null) {
                        column.setColor(FxColorTools.randomColor(random));
                    }
                    if (!isTable()) {
                        column.setAuto(false);
                        column.setIsPrimaryKey(false);
                    }
                    if (isMatrix()) {
                        column.setType(ColumnDefinition.ColumnType.Double);
                    }
                }
                colsNumber = columns.size();
                if (d2did >= 0) {
                    tableData2DColumn.save(conn, d2did, columns);
                    tableData2DDefinition.updateData(conn, this);
                }
            } else {
                colsNumber = 0;
                if (d2did >= 0) {
                    tableData2DColumn.clear(conn, d2did);
                    tableData2DDefinition.updateData(conn, this);
                    tableData2DStyle.clear(conn, d2did);
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.debug(e);
            return false;
        }
    }

    public long readTotal() {
        dataSize = 0;
        Data2DReader reader = Data2DReader.create(this)
                .setReaderTask(backgroundTask).start(Operation.ReadTotal);
        if (reader != null) {
            dataSize = reader.getRowIndex();
        }
        rowsNumber = dataSize;
        try ( Connection conn = DerbyBase.getConnection()) {
            tableData2DDefinition.updateData(conn, this);
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
        return dataSize;
    }

    public List<List<String>> readPageData(Connection conn) {
        if (!isColumnsValid()) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        Data2DReader reader = Data2DReader.create(this)
                .setConn(conn).setReaderTask(task)
                .start(Operation.ReadPage);
        if (reader == null) {
            startRowOfCurrentPage = endRowOfCurrentPage = 0;
            return null;
        }
        List<List<String>> rows = reader.getRows();
        if (rows != null) {
            endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        }
        readPageStyles(conn, rows);
        return rows;
    }

    public void readPageStyles(Connection conn, List<List<String>> rows) {
        styles = new ArrayList<>();
        if (d2did < 0 || startRowOfCurrentPage >= endRowOfCurrentPage) {
            return;
        }
        try ( PreparedStatement statement = conn.prepareStatement(TableData2DStyle.QueryStyles)) {
            statement.setLong(1, d2did);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Data2DStyle style = tableData2DStyle.readData(results);
                    if (style != null) {
                        styles.add(style);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
        countAbnormalLines();
    }

    public void countSize() {
        try {
            rowsNumber = dataSize + (tableRowsNumber() - (endRowOfCurrentPage - startRowOfCurrentPage));
            colsNumber = tableColsNumber();
            if (colsNumber <= 0) {
                hasHeader = false;
            }
        } catch (Exception e) {
        }
    }

    public void countAbnormalLines() {
        resetStatistic();
        if (styles == null || styles.isEmpty()) {
            return;
        }
        try {
            List<String> colNames = new ArrayList<>();
            DescriptiveStatistic calculation = new DescriptiveStatistic()
                    .setStatisticObject(DescriptiveStatistic.StatisticObject.Columns);
            for (Data2DStyle style : styles) {
                String scolumns = style.getColumns();
                if (scolumns != null && !scolumns.isBlank()) {
                    String[] ns = scolumns.split(Data2DStyle.ColumnSeparator);
                    for (String s : ns) {
                        if (!colNames.contains(s)) {
                            colNames.add(s);
                        }
                    }
                } else {
                    colNames = this.columnNames();
                }
                ColumnFilter columnFilter = style.getColumnFilter();
                if (columnFilter == null) {
                    continue;
                }
                checkStatistic(calculation, columnFilter.getEqualValue());
                checkStatistic(calculation, columnFilter.getLargerValue());
                checkStatistic(calculation, columnFilter.getLargerValue());
            }
            if (colNames.isEmpty() || !calculation.needStored()) {
                return;
            }
            List<Integer> colIndices = new ArrayList<>();
            for (String name : colNames) {
                colIndices.add(colOrder(name));
            }
            DataTable tmpTable = ((Data2D) this).toTmpTable(task, colIndices, false, true);
            if (tmpTable == null) {
                return;
            }
            tmpTable.setTask(task);
            List<Integer> tmpColIndices = tmpTable.columnIndices().subList(1, tmpTable.columnsNumber());
            DoubleStatistic[] statisticData = tmpTable.statisticByColumnsForStored(tmpColIndices, calculation);
            if (statisticData == null) {
                return;
            }
            for (int i = 0; i < colNames.size(); i++) {
                Data2DColumn column = columnByName(colNames.get(i));
                column.setDoubleStatistic(statisticData[i]);
            }
            tmpTable.drop();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
        }
    }

    public void checkStatistic(DescriptiveStatistic calculation, String name) {
        if (calculation == null || name == null || name.isBlank()) {
            return;
        }
        if (ColumnFilter.Q1.equals(name)) {
            calculation.setLowerQuartile(true);
        } else if (ColumnFilter.Q3.equals(name)) {
            calculation.setUpperQuartile(true);
        } else if (ColumnFilter.E1.equals(name)) {
            calculation.setLowerExtremeOutlierLine(true);
        } else if (ColumnFilter.E2.equals(name)) {
            calculation.setLowerMildOutlierLine(true);
        } else if (ColumnFilter.E3.equals(name)) {
            calculation.setUpperMildOutlierLine(true);
        } else if (ColumnFilter.E4.equals(name)) {
            calculation.setUpperExtremeOutlierLine(true);
        } else if (ColumnFilter.Mode.equals(name)) {
            calculation.setMode(true);
        } else if (ColumnFilter.Median.equals(name)) {
            calculation.setMedian(true);
        }
    }

    /*
        write
     */
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            if (file != null && !isTmpData()) {
                dataName = file.getName();
            } else {
                dataName = DateTools.nowString();
            }
        }
        return true;
    }

    public boolean saveAttributes() {
        try ( Connection conn = DerbyBase.getConnection()) {
            return saveAttributes(conn, (Data2D) this, columns);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveAttributes(Data2D source, Data2D target) {
        try ( Connection conn = DerbyBase.getConnection()) {
            target.cloneAttributes(source);
            if (!saveAttributes(conn, target, source.getColumns())) {
                return false;
            }
            return target.getTableData2DStyle().copyStyles(conn, source.getD2did(), target.getD2did()) >= 0;
        } catch (Exception e) {
            if (source.getTask() != null) {
                source.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveAttributes(Data2D d, List<Data2DColumn> cols) {
        if (d == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return saveAttributes(conn, d, cols);
        } catch (Exception e) {
            if (d.getTask() != null) {
                d.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean saveAttributes(Connection conn, Data2D d, List<Data2DColumn> inColumns) {
        if (d == null) {
            return false;
        }
        try {
            if (!d.checkForSave() || !d.checkForLoad()) {
                return false;
            }
            Data2DDefinition def;
            long did = d.getD2did();
            d.setModifyTime(new Date());
            d.setColsNumber(inColumns == null ? 0 : inColumns.size());
            TableData2DDefinition tableData2DDefinition = d.getTableData2DDefinition();
            if (did >= 0) {
                def = tableData2DDefinition.updateData(conn, d);
            } else {
                def = d.queryDefinition(conn);
                if (def == null) {
                    def = tableData2DDefinition.insertData(conn, d);
                } else {
                    d.setD2did(def.getD2did());
                    def = tableData2DDefinition.updateData(conn, d);
                }
            }
            conn.commit();
            did = def.getD2did();
            if (did < 0) {
                return false;
            }
            d.cloneAll(def);
            if (inColumns != null && !inColumns.isEmpty()) {
                try {
                    List<Data2DColumn> savedColumns = new ArrayList<>();
                    for (int i = 0; i < inColumns.size(); i++) {
                        Data2DColumn column = inColumns.get(i).cloneAll();
                        if (column.getD2id() != did) {
                            column.setD2cid(-1);
                        }
                        column.setD2id(did);
                        column.setIndex(i);
                        if (!d.isTable()) {
                            column.setIsPrimaryKey(false);
                            column.setAuto(false);
                        }
                        if (d.isMatrix()) {
                            column.setType(ColumnDefinition.ColumnType.Double);
                        }
                        savedColumns.add(column);
                    }
                    d.getTableData2DColumn().save(conn, did, savedColumns);
                    d.setColumns(savedColumns);
                } catch (Exception e) {
                    if (d.getTask() != null) {
                        d.getTask().setError(e.toString());
                    }
                    MyBoxLog.error(e);
                }
            } else {
                d.getTableData2DColumn().clear(d);
                d.setColumns(null);
            }
            return true;
        } catch (Exception e) {
            if (d.getTask() != null) {
                d.getTask().setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

}
