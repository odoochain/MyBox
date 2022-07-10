package mara.mybox.fxml;

import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2022-7-7
 * @License Apache License Version 2.0
 */
public class RowFilter extends ExpressionCalculator {

    public long passedNumber, maxPassed;
    public boolean reversed, passed;

    public RowFilter() {
        init();
    }

    private void init() {
        passedNumber = 0;
        maxPassed = -1;
        reversed = false;
        passed = false;
    }

    public static RowFilter create() {
        return new RowFilter();
    }

    @Override
    public void stopService() {
        super.stopService();
        passedNumber = 0;
        passed = false;
    }

    public boolean needFilter() {
        return (script != null && !script.isBlank())
                || maxPassed > 0;
    }

    public boolean reachMaxPassed() {
        return maxPassed > 0 && passedNumber > maxPassed;
    }

    public boolean readResult(boolean calculateFailed) {
        passed = calculateFailed ? false : "true".equals(expressionResult);
        passed = reversed ? !passed : passed;
        if (passed) {
            passedNumber++;
        }
        return passed;
    }

    public boolean filterTableRow(List<String> tableRow, long tableRowIndex) {
        if (!needFilter()) {
            passed = true;
            passedNumber++;
            return true;
        }
        return readResult(!calculateTableRowExpression(script, tableRow, tableRowIndex));
    }

    public boolean filterDataRow(List<String> dataRow, long dataRowIndex) {
        try {
            error = null;
            if (dataRow == null) {
                passed = false;
                return false;
            }
            if (!needFilter()) {
                passed = true;
                passedNumber++;
                return true;
            }
            if (task == null || task.isQuit()) {
                return readResult(!calculateExpression(dataRowExpression(script, dataRow, dataRowIndex)));
            }
            synchronized (expressionLock) {
                expression = dataRowExpression(script, dataRow, dataRowIndex);
                expressionLock.notify();
                expressionLock.wait();
                readResult(false);
                expressionResult = null;
            }
        } catch (Exception e) {
            handleError(e);
        }
        return passed;
    }


    /*
        get/set
     */
    public long getPassedNumber() {
        return passedNumber;
    }

    public RowFilter setPassedNumber(long passedNumber) {
        this.passedNumber = passedNumber;
        return this;
    }

    public long getMaxPassed() {
        return maxPassed;
    }

    public RowFilter setMaxPassed(long maxPassed) {
        this.maxPassed = maxPassed;
        return this;
    }

    public boolean isReversed() {
        return reversed;
    }

    public RowFilter setReversed(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    public boolean isPassed() {
        return passed;
    }

    public RowFilter setPassed(boolean passed) {
        this.passed = passed;
        return this;
    }

}
