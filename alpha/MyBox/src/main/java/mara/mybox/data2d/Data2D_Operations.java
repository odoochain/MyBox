package mara.mybox.data2d;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.calculation.Normalization;
import mara.mybox.calculation.SimpleLinearRegression;
import mara.mybox.controller.ControlDataConvert;
import mara.mybox.data2d.reader.Data2DCopy;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.data2d.reader.Data2DFrequency;
import mara.mybox.data2d.reader.Data2DNormalize;
import mara.mybox.data2d.reader.Data2DOperator;
import mara.mybox.data2d.reader.Data2DPrecentage;
import mara.mybox.data2d.reader.Data2DReadColumns;
import mara.mybox.data2d.reader.Data2DReadRows;
import mara.mybox.data2d.reader.Data2DRowExpression;
import mara.mybox.data2d.reader.Data2DSimpleLinearRegression;
import mara.mybox.data2d.reader.Data2DStatistic;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.Frequency;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public abstract class Data2D_Operations extends Data2D_Convert {

    public static enum ObjectType {
        Columns, Rows, All
    }

    public boolean export(ControlDataConvert convertController, List<Integer> cols) {
        if (convertController == null || cols == null || cols.isEmpty()) {
            return false;
        }
        Data2DOperator reader = Data2DExport.create(this)
                .setConvertController(convertController)
                .setCols(cols).setTask(task).start();
        return reader != null && !reader.failed();
    }

    public List<List<String>> allRows(List<Integer> cols, boolean rowNumber) {
        Data2DReadColumns reader = Data2DReadColumns.create(this);
        reader.setIncludeRowNumber(rowNumber)
                .setCols(cols).setTask(task).start();
        return reader.failed() ? null : reader.getRows();
    }

    public List<List<String>> allRows(boolean rowNumber) {
        Data2DReadRows reader = Data2DReadRows.create(this);
        reader.setIncludeRowNumber(rowNumber).setTask(task).start();
        return reader.failed() ? null : reader.getRows();
    }

    public DoubleStatistic[] statisticByColumnsForCurrentPage(List<Integer> cols, DescriptiveStatistic selections) {
        try {
            if (cols == null || cols.isEmpty() || selections == null) {
                return null;
            }
            int colLen = cols.size();
            DoubleStatistic[] sData = new DoubleStatistic[colLen];
            List<List<String>> tableData = tableData();
            int rNumber = tableData.size();
            for (int c = 0; c < colLen; c++) {
                int colIndex = cols.get(c);
                String[] colData = new String[rNumber];
                for (int r = 0; r < rNumber; r++) {
                    colData[r] = tableData.get(r).get(colIndex + 1);
                }
                DoubleStatistic colStatistic = new DoubleStatistic();
                colStatistic.invalidAs = selections.invalidAs;
                colStatistic.calculate(colData, selections);
                columns.get(colIndex).setStatistic(colStatistic);
                sData[c] = colStatistic;
            }
            return sData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // No percentile nor mode
    public DoubleStatistic[] statisticByColumnsWithoutStored(List<Integer> cols, DescriptiveStatistic selections) {
        try {
            if (cols == null || cols.isEmpty() || selections == null) {
                return null;
            }
            int colLen = cols.size();
            DoubleStatistic[] sData = new DoubleStatistic[colLen];
            for (int c = 0; c < colLen; c++) {
                Data2DColumn column = columns.get(cols.get(c));
                DoubleStatistic colStatistic = column.getStatistic();
                if (colStatistic == null) {
                    colStatistic = new DoubleStatistic();
                    column.setStatistic(colStatistic);
                }
                colStatistic.invalidAs = selections.invalidAs;
                sData[c] = colStatistic;
            }
            Data2DOperator reader = Data2DStatistic.create(this)
                    .setStatisticData(sData)
                    .setStatisticCalculation(selections)
                    .setType(Data2DStatistic.Type.ColumnsPass1)
                    .setCols(cols).setTask(task).start();
            if (reader == null || reader.failed()) {
                return null;
            }
            if (selections.isPopulationStandardDeviation() || selections.isPopulationVariance()
                    || selections.isSampleStandardDeviation() || selections.isSampleVariance()) {
                reader = Data2DStatistic.create(this)
                        .setStatisticData(sData)
                        .setStatisticCalculation(selections)
                        .setType(Data2DStatistic.Type.ColumnsPass2)
                        .setCols(cols).setTask(task).start();
                if (reader == null || reader.failed()) {
                    return null;
                }
            }
            return sData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    // percentile or mode
    public DoubleStatistic[] statisticByColumnsForStored(List<Integer> cols, DescriptiveStatistic selections) {
        try {
            if (cols == null || cols.isEmpty() || selections == null) {
                return null;
            }
            DataTable tmpTable = ((Data2D) this).toTmpTable(task, cols, false, true);
            if (tmpTable == null) {
                return null;
            }
            tmpTable.setTask(task);
            List<Integer> tmpColIndices = tmpTable.columnIndices().subList(1, tmpTable.columnsNumber());
            DoubleStatistic[] statisticData = tmpTable.statisticByColumnsForStored(tmpColIndices, selections);
            if (statisticData == null) {
                return null;
            }
            for (int i = 0; i < cols.size(); i++) {
                Data2DColumn column = this.column(cols.get(i));
                column.setStatistic(statisticData[i]);
            }
            tmpTable.drop();
            return statisticData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public DataFileCSV statisticByRows(String dname, List<String> names, List<Integer> cols, DescriptiveStatistic selections) {
        if (names == null || names.isEmpty() || cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "statistic", ".csv");
        Data2DOperator reader = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);
            reader = Data2DStatistic.create(this)
                    .setStatisticCalculation(selections)
                    .setType(Data2DStatistic.Type.Rows)
                    .setCsvPrinter(csvPrinter)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    // No percentile nor mode
    public DoubleStatistic statisticByAllWithoutStored(List<Integer> cols, DescriptiveStatistic selections) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = selections.invalidAs;
        Data2DOperator reader = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass1)
                .setCols(cols).setTask(task).start();
        if (reader == null || reader.failed()) {
            return null;
        }
        if (selections.isPopulationStandardDeviation() || selections.isPopulationVariance()
                || selections.isSampleStandardDeviation() || selections.isSampleVariance()) {
            reader = Data2DStatistic.create(this)
                    .setStatisticAll(sData)
                    .setStatisticCalculation(selections)
                    .setType(Data2DStatistic.Type.AllPass2)
                    .setCols(cols).setTask(task).start();
            if (reader == null || reader.failed()) {
                return null;
            }
        }
        return sData;
    }

    public DataFileCSV copy(String dname, List<Integer> cols, boolean includeRowNumber, boolean includeColName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "copy", ".csv");
        Data2DOperator reader;
        List<Data2DColumn> targetColumns = new ArrayList<>();
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (includeRowNumber) {
                names.add(message("RowNumber"));
                targetColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                    targetColumns.add(columns.get(i).cloneAll().setD2cid(-1).setD2id(-1));
                }
            }
            if (includeColName) {
                csvPrinter.printRecord(names);
            }
            reader = Data2DCopy.create(this)
                    .setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(includeRowNumber)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && !reader.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(includeColName)
                    .setColsNumber(targetColumns.size())
                    .setRowsNumber(reader.getRowIndex());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV rowExpression(String dname, String script, String name, boolean errorContinue,
            List<Integer> cols, boolean includeRowNumber, boolean includeColName) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "RowExpression", ".csv");
        Data2DOperator reader;
        List<Data2DColumn> targetColumns = new ArrayList<>();
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (includeRowNumber) {
                names.add(message("RowNumber"));
                targetColumns.add(new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                    targetColumns.add(columns.get(i).cloneAll().setD2cid(-1).setD2id(-1));
                }
            }
            names.add(name);
            targetColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.String));
            if (includeColName) {
                csvPrinter.printRecord(names);
            }
            reader = Data2DRowExpression.create(this)
                    .setScript(script).setName(name)
                    .setCsvPrinter(csvPrinter)
                    .setIncludeRowNumber(includeRowNumber)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (reader != null && !reader.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setColumns(targetColumns)
                    .setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(includeColName)
                    .setColsNumber(targetColumns.size())
                    .setRowsNumber(reader.getRowIndex());
            targetData.saveAttributes();
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentageColumns(String dname, List<String> names, List<Integer> cols,
            int scale, boolean withValues, String toNegative, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DPrecentage reader = Data2DPrecentage.create(this)
                .setType(Data2DPrecentage.Type.ColumnsPass1)
                .setToNegative(toNegative);
        reader.setInvalidAs(invalidAs).setScale(scale)
                .setCols(cols).setTask(task).start();
        if (reader.failed()) {
            return null;
        }
        double[] colsSum = reader.getColValues();
        File csvFile = tmpFile(dname, "percentage", ".csv");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("Column") + "-" + message("Summation"));
            for (int c = 0; c < cols.size(); c++) {
                row.add(DoubleTools.scale(colsSum[c], scale) + "");
                if (withValues) {
                    row.add("100");
                }
            }
            csvPrinter.printRecord(row);
            reader = Data2DPrecentage.create(this)
                    .setType(Data2DPrecentage.Type.ColumnsPass2)
                    .setToNegative(toNegative)
                    .setColValues(colsSum);
            reader.setInvalidAs(invalidAs).setScale(scale).setCsvPrinter(csvPrinter)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (!reader.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex() + 1);
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentageAll(String dname, List<String> names, List<Integer> cols,
            int scale, boolean withValues, String toNegative, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        Data2DPrecentage reader = Data2DPrecentage.create(this)
                .setType(Data2DPrecentage.Type.AllPass1)
                .setToNegative(toNegative);
        reader.setInvalidAs(invalidAs).setScale(scale)
                .setCols(cols).setTask(task).start();
        if (reader.failed()) {
            return null;
        }
        File csvFile = tmpFile(dname, "percentage", ".csv");
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            row.add(message("All") + "-" + message("Summation"));
            double sum = reader.gettValue();
            row.add(DoubleTools.format(sum, scale));
            if (withValues) {
                row.add("100");
            }
            for (int c : cols) {
                row.add(null);
                if (withValues) {
                    row.add(null);
                }
            }
            csvPrinter.printRecord(row);
            reader = Data2DPrecentage.create(this)
                    .setType(Data2DPrecentage.Type.AllPass2)
                    .setWithValues(withValues).setToNegative(toNegative)
                    .settValue(sum);
            reader.setInvalidAs(invalidAs).setScale(scale).setCsvPrinter(csvPrinter)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (!reader.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex() + 1);
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV percentageRows(String dname, List<String> names, List<Integer> cols,
            int scale, boolean withValues, String toNegative, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "percentage", ".csv");
        Data2DOperator reader;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            csvPrinter.printRecord(names);

            reader = Data2DPrecentage.create(this)
                    .setType(Data2DPrecentage.Type.Rows)
                    .setWithValues(withValues)
                    .setToNegative(toNegative);
            reader.setInvalidAs(invalidAs).setScale(scale).setCsvPrinter(csvPrinter)
                    .setCols(cols).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (!reader.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(names.size()).setRowsNumber(reader.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV frequency(String dname, Frequency frequency, String colName, int col, int scale) {
        if (frequency == null || colName == null || col < 0) {
            return null;
        }
        File csvFile = tmpFile(dname, "frequency", ".csv");
        Data2DFrequency operator;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> row = new ArrayList<>();
            row.add(colName);
            row.add(colName + "_" + message("Count"));
            row.add(colName + "_" + message("CountPercentage"));
            csvPrinter.printRecord(row);

            operator = Data2DFrequency.create(this)
                    .setFrequency(frequency).setColIndex(col);
            operator.setCsvPrinter(csvPrinter).setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (!operator.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(3).setRowsNumber(operator.getCount());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeMinMaxColumns(String dname, List<Integer> cols, double from, double to,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
            sData[c].invalidAs = invalidAs;
        }
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .setSum(true).setMaximum(true).setMinimum(true);
        Data2DOperator operator = Data2DStatistic.create(this)
                .setStatisticData(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.ColumnsPass1)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null || operator.failed()) {
            return null;
        }
        for (int c = 0; c < colLen; c++) {
            double d = sData[c].maximum - sData[c].minimum;
            sData[c].dTmp = (to - from) / (d == 0 ? AppValues.TinyDouble : d);
        }
        File csvFile = tmpFile(dname, "normalizeMinMax", ".csv");
        int tcolsNumber = 0;
        operator = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.MinMaxColumns)
                    .setStatisticData(sData).setFrom(from)
                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setScale(scale).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(operator.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeSumColumns(String dname, List<Integer> cols,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
            sData[c].invalidAs = invalidAs;
        }
        DescriptiveStatistic selections = DescriptiveStatistic.all(false);
        Data2DOperator operator = Data2DStatistic.create(this)
                .setType(Data2DStatistic.Type.ColumnsPass1)
                .setStatisticData(sData)
                .setStatisticCalculation(selections)
                .setSumAbs(true).setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        double[] colValues = new double[colLen];
        for (int c = 0; c < colLen; c++) {
            if (sData[c].sum == 0) {
                colValues[c] = 1d / AppValues.TinyDouble;
            } else {
                colValues[c] = 1d / sData[c].sum;
            }
        }
        File csvFile = tmpFile(dname, "normalizeSum", ".csv");
        int tcolsNumber = 0;
        operator = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.SumColumns)
                    .setColValues(colValues)
                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(operator.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeZscoreColumns(String dname, List<Integer> cols,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int colLen = cols.size();
        int tcolsNumber = 0;
        DoubleStatistic[] sData = new DoubleStatistic[colLen];
        for (int c = 0; c < colLen; c++) {
            sData[c] = new DoubleStatistic();
            sData[c].invalidAs = invalidAs;
        }
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .setPopulationStandardDeviation(true);
        Data2DOperator operator = Data2DStatistic.create(this)
                .setStatisticData(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.ColumnsPass1)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        operator = Data2DStatistic.create(this)
                .setStatisticData(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.ColumnsPass2)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        File csvFile = tmpFile(dname, "normalizeZscore", ".csv");
        operator = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.ZscoreColumns)
                    .setStatisticData(sData)
                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(operator.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeMinMaxAll(String dname, List<Integer> cols, double from, double to,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .setSum(true).setMaximum(true).setMinimum(true);
        Data2DOperator operator = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass1)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        double d = sData.maximum - sData.minimum;
        sData.dTmp = (to - from) / (d == 0 ? AppValues.TinyDouble : d);
        File csvFile = tmpFile(dname, "normalizeMinMax", ".csv");
        int tcolsNumber = 0;
        operator = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.MinMaxAll)
                    .setStatisticAll(sData).setFrom(from)
                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(operator.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeSumAll(String dname, List<Integer> cols,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false);
        Data2DOperator operator = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass1)
                .setSumAbs(true).setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        double k;
        if (sData.sum == 0) {
            k = 1d / AppValues.TinyDouble;
        } else {
            k = 1d / sData.sum;
        }
        File csvFile = tmpFile(dname, "normalizeSum", ".csv");
        int tcolsNumber = 0;
        operator = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.SumAll)
                    .settValue(k)
                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(operator.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeZscoreAll(String dname, List<Integer> cols,
            boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        int tcolsNumber = 0;
        DoubleStatistic sData = new DoubleStatistic();
        sData.invalidAs = invalidAs;
        DescriptiveStatistic selections = DescriptiveStatistic.all(false)
                .setPopulationStandardDeviation(true);
        Data2DOperator operator = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass1)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        operator = Data2DStatistic.create(this)
                .setStatisticAll(sData)
                .setStatisticCalculation(selections)
                .setType(Data2DStatistic.Type.AllPass2)
                .setInvalidAs(invalidAs)
                .setCols(cols).setScale(scale).setTask(task).start();
        if (operator == null) {
            return null;
        }
        File csvFile = tmpFile(dname, "normalizeZscore", ".csv");
        operator = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.ZscoreAll)
                    .setStatisticAll(sData)
                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setScale(scale).setTask(task).start();

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(operator.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV normalizeRows(String dname, Normalization.Algorithm a, List<Integer> cols,
            double from, double to, boolean rowNumber, boolean colName, int scale, InvalidAs invalidAs) {
        if (cols == null || cols.isEmpty()) {
            return null;
        }
        File csvFile = tmpFile(dname, "normalizeSum", ".csv");
        int tcolsNumber = 0;
        Data2DOperator operator = null;
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
            List<String> names = new ArrayList<>();
            if (rowNumber) {
                names.add(message("RowNumber"));
            }
            for (int i = 0; i < columns.size(); i++) {
                if (cols.contains(i)) {
                    names.add(columns.get(i).getColumnName());
                }
            }
            if (colName) {
                csvPrinter.printRecord(names);
            }
            tcolsNumber = names.size();

            operator = Data2DNormalize.create(this)
                    .setType(Data2DNormalize.Type.Rows)
                    .setA(a).setFrom(from).setTo(to)
                    .setCsvPrinter(csvPrinter)
                    .setInvalidAs(invalidAs).setIncludeRowNumber(rowNumber)
                    .setCols(cols).setScale(scale).setTask(task).start();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
        if (operator != null && !operator.failed() && csvFile != null && csvFile.exists()) {
            DataFileCSV targetData = new DataFileCSV();
            targetData.setFile(csvFile).setDataName(dname)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(colName)
                    .setColsNumber(tcolsNumber).setRowsNumber(operator.getRowIndex());
            return targetData;
        } else {
            return null;
        }
    }

    public DataFileCSV simpleLinearRegression(String dname, List<Integer> cols,
            SimpleLinearRegression simpleRegression, boolean writeFile) {
        if (cols == null || cols.isEmpty() || simpleRegression == null) {
            return null;
        }
        if (writeFile) {
            File csvFile = tmpFile(dname, "simpleLinearRegression", ".csv");
            int tcolsNumber = 0;
            Data2DOperator operator = null;
            try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(csvFile)) {
                List<String> names = new ArrayList<>();
                List<Data2DColumn> resultColumns = simpleRegression.getColumns();
                for (Data2DColumn c : resultColumns) {
                    names.add(c.getColumnName());
                }
                csvPrinter.printRecord(names);
                tcolsNumber = names.size();

                operator = Data2DSimpleLinearRegression.create(this)
                        .setSimpleRegression(simpleRegression)
                        .setCsvPrinter(csvPrinter)
                        .setCols(cols).setScale(scale).setTask(task).start();

            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
                return null;
            }
            if (operator != null && !operator.failed() && csvFile != null && csvFile.exists()) {
                DataFileCSV targetData = new DataFileCSV();
                targetData.setFile(csvFile).setDataName(dname)
                        .setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",").setHasHeader(true)
                        .setColsNumber(tcolsNumber).setRowsNumber(operator.getRowIndex());
                return targetData;
            } else {
                return null;
            }
        } else {
            Data2DSimpleLinearRegression.create(this)
                    .setSimpleRegression(simpleRegression)
                    .setCols(cols).setScale(scale).setTask(task).start();
            return null;
        }
    }

}
