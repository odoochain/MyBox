package mara.mybox.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileText extends DataFile {

    public DataFileText() {
        type = Type.Text;
    }

    public void setOptions(boolean hasHeader, Charset charset, String delimiter) {
        options = new HashMap<>();
        options.put("hasHeader", hasHeader);
        options.put("charset", charset);
        options.put("delimiter", delimiter);
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
            if (options.containsKey("charset")) {
                charset = (Charset) (options.get("charset"));
            }
            if (options.containsKey("delimiter")) {
                delimiter = (String) (options.get("delimiter"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void checkAttributes() {
        if (charset == null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = guessDelimiter();
        }
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }
        super.checkAttributes();
    }

    public String guessDelimiter() {
        String[] values = {",", " ", "    ", "        ", "\t", "|", "@",
            "#", ";", ":", "*", ".", "%", "$", "_", "&", "-", "=", "!", "\"",
            "'", "<", ">"};
        return guessDelimiter(values);
    }

    public String guessDelimiter(String[] values) {
        if (file == null || values == null) {
            return null;
        }
        if (charset == null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line1 = reader.readLine();
            if (line1 == null) {
                return null;
            }
            int[] count1 = new int[values.length];
            int maxCount1 = 0, maxCountIndex1 = -1;
            for (int i = 0; i < values.length; i++) {
                count1[i] = FindReplaceString.count(line1, values[i]);
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] > maxCount1) {
                    maxCount1 = count1[i];
                    maxCountIndex1 = i;
                }
            }
//            MyBoxLog.console(maxCount1);
            String line2 = reader.readLine();
            if (line2 == null) {
                if (maxCountIndex1 >= 0) {
                    return values[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
            int[] count2 = new int[values.length];
            int maxCount2 = 0, maxCountIndex2 = -1;
            for (int i = 0; i < values.length; i++) {
                count2[i] = FindReplaceString.count(line2, values[i]);
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] == count2[i] && count2[i] > maxCount2) {
                    maxCount2 = count2[i];
                    maxCountIndex2 = i;
                }
            }
//            MyBoxLog.console(maxCount2);
            if (maxCountIndex2 >= 0) {
                return values[maxCountIndex2];
            } else {
                if (maxCountIndex1 >= 0) {
                    return values[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
        } catch (Exception e) {
        }
        hasHeader = false;
        return null;
    }

    @Override
    public List<String> readColumns() {
        List<String> names = null;
        checkAttributes();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            names = readValidLine(reader);
            if (!hasHeader && names != null) {
                int len = names.size();
                names = new ArrayList<>();
                for (int i = 1; i <= len; i++) {
                    names.add(colPrefix() + i);
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return names;
    }

    protected List<String> parseFileLine(String line) {
        return TextTools.parseLine(line, delimiter);
    }

    protected List<String> readValidLine(BufferedReader reader) {
        List<String> names = null;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                names = parseFileLine(line);
                if (names != null && !names.isEmpty()) {
                    break;
                }
            }
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    dataSize = 0;
                    break;
                }
                List<String> row = parseFileLine(line);
                if (row != null && !row.isEmpty()) {
                    dataSize++;
                }
            }
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
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        endRowOfCurrentPage = startRowOfCurrentPage;
        List<List<String>> rows = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            long rowIndex = -1;
            long end = startRowOfCurrentPage + pageSize;
            int columnsNumber = columnsNumber();
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> record = parseFileLine(line);
                if (record == null || record.isEmpty()) {
                    continue;
                }
                if (++rowIndex < startRowOfCurrentPage) {
                    continue;
                }
                if (rowIndex >= end) {
                    break;
                }
                List<String> row = new ArrayList<>();
                for (int i = 0; i < Math.min(record.size(), columnsNumber); i++) {
                    row.add(record.get(i));
                }
                for (int col = row.size(); col < columnsNumber; col++) {
                    row.add(defaultColValue());
                }
                row.add(0, "" + (rowIndex + 1));
                rows.add(row);
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
        if (targetData == null || !(targetData instanceof DataFileText)) {
            return false;
        }
        DataFileText targetTextFile = (DataFileText) targetData;
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = targetTextFile.getFile();
        if (tFile == null) {
            return false;
        }
        targetTextFile.checkAttributes();
        Charset tCharset = targetTextFile.getCharset();
        String tDelimiter = targetTextFile.getDelimiter();
        checkAttributes();
        boolean tHasHeader = targetTextFile.isHasHeader();
        if (file != null) {
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, tCharset, false))) {
                List<String> colsNames = columnNames();
                if (hasHeader) {
                    readValidLine(reader);
                }
                if (tHasHeader && colsNames != null) {
                    TextFileTools.writeLine(writer, colsNames, tDelimiter);
                }
                long rowIndex = -1;
                String line;
                while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                    List<String> row = parseFileLine(line);
                    if (row == null || row.isEmpty()) {
                        continue;
                    }
                    if (++rowIndex < startRowOfCurrentPage || rowIndex >= endRowOfCurrentPage) {
                        TextFileTools.writeLine(writer, fileRow(row), tDelimiter);
                    } else if (rowIndex == startRowOfCurrentPage) {
                        writePageData(writer, tDelimiter);
                    }
                }
                if (rowIndex < 0) {
                    writePageData(writer, tDelimiter);
                }
                writer.flush();
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        } else {
            try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, tCharset, false))) {
                List<String> colsNames = columnNames();
                if (tHasHeader && colsNames != null) {
                    TextFileTools.writeLine(writer, colsNames, tDelimiter);
                }
                writePageData(writer, tDelimiter);
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        return FileTools.rename(tmpFile, tFile, false);
    }

    public boolean writePageData(BufferedWriter writer, String delimiter) {
        try {
            if (writer == null || delimiter == null || !isColumnsValid()) {
                return false;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                TextFileTools.writeLine(writer, tableRow(r), delimiter);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    @Override
    public File tmpFile(List<String> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            File tmpFile = TmpFileTools.getTempFile(".txt");
            String fDelimiter = ",";
            try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8"), false))) {
                if (cols != null && !cols.isEmpty()) {
                    TextFileTools.writeLine(writer, cols, fDelimiter);
                }
                if (data != null) {
                    for (int r = 0; r < data.size(); r++) {
                        if (task != null && task.isCancelled()) {
                            break;
                        }
                        TextFileTools.writeLine(writer, data.get(r), fDelimiter);
                    }
                }
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

}
