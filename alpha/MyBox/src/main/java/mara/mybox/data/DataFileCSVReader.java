package mara.mybox.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.StringTools;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileCSVReader extends DataFileReader {

    protected DataFileCSV readerCSV;
    protected Iterator<CSVRecord> iterator;
    CSVParser csvParser;

    public DataFileCSVReader(DataFileCSV data) {
        this.readerCSV = data;
        init(data);
    }

    @Override
    public void scanFile() {
        readerCSV.checkForLoad();
        try ( CSVParser parser = CSVParser.parse(readerFile, readerCSV.getCharset(), readerCSV.cvsFormat())) {
            csvParser = parser;
            iterator = parser.iterator();
            handleFile();
            csvParser = null;
            parser.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
            failed = true;
        }
    }

    @Override
    public void readColumns() {
        if (csvParser == null) {
            return;
        }
        if (readerHasHeader) {
            try {
                List<String> values = csvParser.getHeaderNames();
                if (StringTools.noDuplicated(values, true)) {
                    names = new ArrayList<>();
                    names.addAll(values);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (readerTask != null) {
                    readerTask.setError(e.toString());
                }
            }
        }
        if (names == null) {
            readerHasHeader = false;
            while (iterator.hasNext() && !readerStopped()) {
                readRecord();
                if (record == null || record.isEmpty()) {
                    continue;
                }
                handleHeader();
                return;
            }
        }
    }

    @Override
    public void readTotal() {
        if (iterator == null) {
            return;
        }
        rowIndex = 0;
        while (iterator.hasNext()) {
            if (readerStopped()) {
                rowIndex = 0;
                return;
            }
            ++rowIndex;
            iterator.next();
        }
    }

    @Override
    public void readPage() {
        if (iterator == null) {
            return;
        }
        rowIndex = -1;
        while (iterator.hasNext() && !readerStopped()) {
            if (++rowIndex < rowsStart) {
                iterator.next();
                continue;
            }
            if (rowIndex >= rowsEnd) {
                readerStopped = true;
                break;
            }
            readRecord();
            if (record == null || record.isEmpty()) {
                continue;
            }
            handlePageRow();
        }
    }

    @Override
    public void readRecords() {
        if (iterator == null) {
            return;
        }
        rowIndex = 0;
        while (iterator.hasNext() && !readerStopped()) {
            readRecord();
            if (record == null || record.isEmpty()) {
                continue;
            }
            handleRecord();
            ++rowIndex;
        }
    }

    public void readRecord() {
        try {
            record = null;
            if (readerStopped() || iterator == null) {
                return;
            }
            CSVRecord csvRecord = iterator.next();
            if (csvRecord == null) {
                return;
            }
            record = new ArrayList<>();
            for (String v : csvRecord) {
                record.add(v);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (readerTask != null) {
                readerTask.setError(e.toString());
            }
        }
    }

}
