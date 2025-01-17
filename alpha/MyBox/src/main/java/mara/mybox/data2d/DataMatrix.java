package mara.mybox.data2d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.data.SetValue;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DCell;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DCell;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.NumberTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class DataMatrix extends Data2D {

    protected TableData2DCell tableData2DCell;

    public DataMatrix() {
        type = Type.Matrix;
        tableData2DCell = new TableData2DCell();
    }

    public int type() {
        return type(Type.Matrix);
    }

    public void cloneAll(DataMatrix d) {
        try {
            if (d == null) {
                return;
            }
            super.cloneAll(d);
            tableData2DCell = d.tableData2DCell;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean checkForLoad() {
        hasHeader = false;
        return true;
    }

    @Override
    public boolean checkForSave() {
        if (dataName == null || dataName.isBlank()) {
            dataName = rowsNumber + "x" + colsNumber;
        }
        return true;
    }

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryID(conn, d2did);
    }

    @Override
    public void applyOptions() {
    }

    @Override
    public long readTotal() {
        return dataSize;
    }

    @Override
    public List<String> readColumnNames() {
        checkForLoad();
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= colsNumber; i++) {
            names.add(colPrefix() + i);
        }
        return names;
    }

    @Override
    public List<List<String>> readPageData(Connection conn) {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        List<List<String>> rows = new ArrayList<>();
        if (d2did >= 0 && rowsNumber > 0 && colsNumber > 0) {
            double[][] matrix = new double[(int) rowsNumber][(int) colsNumber];
            try ( PreparedStatement query = conn.prepareStatement(TableData2DCell.QueryData)) {
                query.setLong(1, d2did);
                ResultSet results = query.executeQuery();
                while (results.next()) {
                    Data2DCell cell = tableData2DCell.readData(results);
                    if (cell.getCol() < colsNumber && cell.getRow() < rowsNumber) {
                        matrix[(int) cell.getRow()][(int) cell.getCol()] = toDouble(cell.getValue());
                    }
                }
                rows = toTableData(matrix);
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.console(e);
            }
        }
        rowsNumber = rows.size();
        dataSize = rowsNumber;
        endRowOfCurrentPage = startRowOfCurrentPage + rowsNumber;
        readPageStyles(conn);
        return rows;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !targetData.isMatrix()) {
            return false;
        }
        return save(null, (DataMatrix) targetData, columns, tableRows(false));
    }

    @Override
    public boolean export(ControlDataConvert convertController, List<Integer> colIndices) {
        return false;
    }

    public boolean isSquare() {
        return isValid() && tableColsNumber() == tableRowsNumber();
    }

    public String toString(double d) {
        if (DoubleTools.invalidDouble(d)) {
            return Double.NaN + "";
        } else {
            return NumberTools.format(d, scale);
        }
    }

    public static double toDouble(String d) {
        try {
            return Double.valueOf(d.replaceAll(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public double[][] toMatrix() {
        rowsNumber = tableRowsNumber();
        colsNumber = tableColsNumber();
        if (rowsNumber <= 0 || colsNumber <= 0) {
            return null;
        }
        double[][] data = new double[(int) rowsNumber][(int) colsNumber];
        for (int r = 0; r < rowsNumber; r++) {
            List<String> row = tableRow(r, false, false);
            for (int c = 0; c < row.size(); c++) {
                data[r][c] = toDouble(row.get(c));
            }
        }
        return data;
    }

    public List<List<String>> toTableData(double[][] data) {
        if (data == null) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        for (int r = 0; r < data.length; r++) {
            List<String> row = new ArrayList<>();
            row.add(("" + (r + 1)));
            for (int c = 0; c < data[r].length; c++) {
                row.add(toString(data[r][c]));
            }
            rows.add(row);
        }
        return rows;
    }

    @Override
    public long setValue(List<Integer> cols, SetValue value, boolean errorContinue) {
        return -1;
    }

    @Override
    public long deleteRows(boolean errorContinue) {
        return -1;
    }

    @Override
    public long clearData() {
        long count = -1;
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement clear = conn.prepareStatement(TableData2DCell.ClearData)) {
            clear.setLong(1, d2did);
            count = clear.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public static boolean save(SingletonTask task, DataMatrix matrix,
            List<Data2DColumn> cols, List<List<String>> rows) {
        if (matrix == null || cols == null || rows == null) {
            return false;
        }
        TableData2DCell tableData2DCell = matrix.tableData2DCell;
        try ( Connection conn = DerbyBase.getConnection()) {
            matrix.setColsNumber(cols.size());
            matrix.setRowsNumber(rows.size());
            Data2D.saveAttributes(conn, matrix, cols);
            long did = matrix.getD2did();
            if (did < 0) {
                return false;
            }
            try ( PreparedStatement clear = conn.prepareStatement(TableData2DCell.ClearData)) {
                clear.setLong(1, did);
                clear.executeUpdate();
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.debug(e);
            }
            conn.commit();
            conn.setAutoCommit(false);
            for (int r = 0; r < rows.size(); r++) {
                List<String> row = rows.get(r);
                for (int c = 0; c < row.size(); c++) {
                    double d = toDouble(row.get(c));
                    if (d == 0 || DoubleTools.invalidDouble(d)) {
                        continue;
                    }
                    Data2DCell cell = Data2DCell.create().setD2did(did)
                            .setRow(r).setCol(c).setValue(d + "");
                    tableData2DCell.insertData(conn, cell);
                }
            }
            conn.commit();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

}
