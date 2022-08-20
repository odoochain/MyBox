package mara.mybox.data2d.reader;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DStatistic extends Data2DOperator {

    protected Type type;
    protected DoubleStatistic[] statisticData;
    protected DoubleStatistic statisticAll;
    protected DescriptiveStatistic statisticCalculation;
    protected double[] colValues;
    protected List<Skewness> skewnessList;
    protected Skewness skewnessAll;
    protected boolean sumAbs;

    public static enum Type {
        ColumnsPass1, ColumnsPass2, Rows, AllPass1, AllPass2
    }

    public static Data2DStatistic create(Data2D_Edit data) {
        Data2DStatistic op = new Data2DStatistic();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        if (cols == null || cols.isEmpty() || type == null) {
            return false;
        }
        switch (type) {
            case ColumnsPass1:
                if (statisticData == null || statisticCalculation == null) {
                    return false;
                }
                colValues = new double[colsLen];
                if (statisticCalculation.isSkewness()) {
                    skewnessList = new ArrayList<>();
                    for (int i = 0; i < cols.size(); i++) {
                        skewnessList.add(new Skewness());
                    }
                }
                break;
            case ColumnsPass2:
                if (statisticData == null) {
                    return false;
                }
                for (int i = 0; i < cols.size(); i++) {
                    statisticData[i].dTmp = 0;
                }
                break;
            case AllPass1:
                if (statisticAll == null || statisticCalculation == null) {
                    return false;
                }
                if (statisticCalculation.isSkewness()) {
                    skewnessAll = new Skewness();
                }
                break;
            case AllPass2:
                if (statisticAll == null) {
                    return false;
                }
                statisticAll.dTmp = 0;
                break;
            case Rows:
                if (csvPrinter == null || statisticCalculation == null) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void handleRow() {
        switch (type) {
            case ColumnsPass1:
                handleStatisticColumnsPass1();
                break;
            case ColumnsPass2:
                handleStatisticColumnsPass2();
                break;
            case AllPass1:
                handleStatisticAllPass1();
                break;
            case AllPass2:
                handleStatisticAllPass2();
                break;
            case Rows:
                handleStatisticRows();
                break;
        }
    }

    public void handleStatisticColumnsPass1() {
        try {
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    continue;
                }
                double v = statisticData[c].toDouble(sourceRow.get(i));
                if (DoubleTools.invalidDouble(v)) {
                    statisticData[c].invalidCount++;
                    continue;
                } else {
                    statisticData[c].count++;
                }
                if (sumAbs) {
                    statisticData[c].sum += Math.abs(v);
                } else {
                    statisticData[c].sum += v;
                }
                if (statisticCalculation.isMaximum() && v > statisticData[c].maximum) {
                    statisticData[c].maximum = v;
                }
                if (statisticCalculation.isMinimum() && v < statisticData[c].minimum) {
                    statisticData[c].minimum = v;
                }
                if (statisticCalculation.isGeometricMean()) {
                    statisticData[c].geometricMean = statisticData[c].geometricMean * v;
                }
                if (statisticCalculation.isSumSquares()) {
                    statisticData[c].sumSquares += v * v;
                }
                if (statisticCalculation.isSkewness()) {
                    skewnessList.get(c).increment(v);
                }
            }
        } catch (Exception e) {

        }
    }

    public void handleStatisticColumnsPass2() {
        try {
            for (int c = 0; c < colsLen; c++) {
                if (statisticData[c].count == 0) {
                    continue;
                }
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    continue;
                }
                double v = statisticData[c].toDouble(sourceRow.get(i));
                if (!DoubleTools.invalidDouble(v)) {
                    v = v - statisticData[c].mean;
                    statisticData[c].dTmp += v * v;
                }
            }
        } catch (Exception e) {
        }
    }

    public void handleStatisticAllPass1() {
        try {
            for (int c = 0; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    continue;
                }
                double v = statisticAll.toDouble(sourceRow.get(i));
                if (DoubleTools.invalidDouble(v)) {
                    statisticAll.invalidCount++;
                    continue;
                } else {
                    statisticAll.count++;
                }
                statisticAll.sum += v;
                if (statisticCalculation.isMaximum() && v > statisticAll.maximum) {
                    statisticAll.maximum = v;
                }
                if (statisticCalculation.isMinimum() && v < statisticAll.minimum) {
                    statisticAll.minimum = v;
                }
                if (statisticCalculation.isGeometricMean()) {
                    statisticAll.geometricMean = statisticAll.geometricMean * v;
                }
                if (statisticCalculation.isSumSquares()) {
                    statisticAll.sumSquares += v * v;
                }
                if (statisticCalculation.isSkewness()) {
                    skewnessAll.increment(v);
                }
            }
        } catch (Exception e) {

        }
    }

    public void handleStatisticAllPass2() {
        try {
            for (int c = 0; c < colsLen; c++) {
                if (statisticAll.count == 0) {
                    continue;
                }
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    continue;
                }
                double v = statisticAll.toDouble(sourceRow.get(i));
                if (!DoubleTools.invalidDouble(v)) {
                    v = v - statisticAll.mean;
                    statisticAll.dTmp += v * v;
                }
            }
        } catch (Exception e) {
        }
    }

    public void handleStatisticRows() {
        try {
            List<String> row = new ArrayList<>();
            int startIndex;
            if (statisticCalculation.getCategoryName() == null) {
                row.add(message("Row") + " " + rowIndex);
                startIndex = 0;
            } else {
                row.add(sourceRow.get(cols.get(0)));
                startIndex = 1;
            }
            String[] values = new String[colsLen - startIndex];
            for (int c = startIndex; c < colsLen; c++) {
                int i = cols.get(c);
                if (i < 0 || i >= sourceRow.size()) {
                    continue;
                }
                values[c - startIndex] = sourceRow.get(i);
            }
            DoubleStatistic statistic = new DoubleStatistic(values, statisticCalculation);
            row.addAll(statistic.toStringList());
            csvPrinter.printRecord(row);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean end() {
        try {
            switch (type) {
                case ColumnsPass1:
                    for (int c = 0; c < colsLen; c++) {
                        if (statisticData[c].count > 0) {
                            if (!DoubleTools.invalidDouble(statisticData[c].sum)) {
                                statisticData[c].mean = statisticData[c].sum / statisticData[c].count;
                            } else {
                                statisticData[c].mean = Double.NaN;
                            }
                            if (statisticCalculation.isGeometricMean()) {
                                if (!DoubleTools.invalidDouble(statisticData[c].geometricMean)) {
                                    statisticData[c].geometricMean = Math.pow(statisticData[c].geometricMean, 1d / statisticData[c].count);
                                } else {
                                    statisticData[c].geometricMean = Double.NaN;
                                }
                            }
                        } else {
                            statisticData[c].mean = Double.NaN;
                            statisticData[c].geometricMean = Double.NaN;
                        }
                        if (statisticCalculation.isSkewness()) {
                            statisticData[c].skewness = skewnessList.get(c).getResult();
                        }
                    }
                    break;
                case ColumnsPass2:
                    for (int c = 0; c < colsLen; c++) {
                        if (statisticData[c].count > 0 && !DoubleTools.invalidDouble(statisticData[c].dTmp)) {
                            statisticData[c].populationVariance = statisticData[c].dTmp / statisticData[c].count;
                            statisticData[c].sampleVariance = statisticData[c].dTmp / (statisticData[c].count - 1);
                            if (statisticCalculation.isPopulationStandardDeviation()) {
                                statisticData[c].populationStandardDeviation = Math.sqrt(statisticData[c].populationVariance);
                            }
                            if (statisticCalculation.isSampleStandardDeviation()) {
                                statisticData[c].sampleStandardDeviation = Math.sqrt(statisticData[c].sampleVariance);
                            }
                        } else {
                            statisticData[c].populationVariance = Double.NaN;
                            statisticData[c].sampleVariance = Double.NaN;
                            statisticData[c].populationStandardDeviation = Double.NaN;
                            statisticData[c].sampleStandardDeviation = Double.NaN;
                        }
                    }
                    break;
                case AllPass1:
                    if (statisticAll.count > 0) {
                        if (!DoubleTools.invalidDouble(statisticAll.sum)) {
                            statisticAll.mean = statisticAll.sum / statisticAll.count;
                        } else {
                            statisticAll.mean = Double.NaN;
                        }
                        if (statisticCalculation.isGeometricMean()) {
                            if (!DoubleTools.invalidDouble(statisticAll.geometricMean)) {
                                statisticAll.geometricMean = Math.pow(statisticAll.geometricMean, 1d / statisticAll.count);
                            } else {
                                statisticAll.geometricMean = Double.NaN;
                            }
                        }
                    } else {
                        statisticAll.mean = Double.NaN;
                        statisticAll.geometricMean = Double.NaN;
                    }
                    if (statisticCalculation.isSkewness()) {
                        statisticAll.skewness = skewnessAll.getResult();
                    }
                    break;
                case AllPass2:
                    if (statisticAll.count > 0 && !DoubleTools.invalidDouble(statisticAll.dTmp)) {
                        statisticAll.populationVariance = statisticAll.dTmp / statisticAll.count;
                        statisticAll.sampleVariance = statisticAll.dTmp / (statisticAll.count - 1);
                        if (statisticCalculation.isPopulationStandardDeviation()) {
                            statisticAll.populationStandardDeviation = Math.sqrt(statisticAll.populationVariance);
                        }
                        if (statisticCalculation.isSampleStandardDeviation()) {
                            statisticAll.sampleStandardDeviation = Math.sqrt(statisticAll.sampleVariance);
                        }
                    } else {
                        statisticAll.populationVariance = Double.NaN;
                        statisticAll.sampleVariance = Double.NaN;
                        statisticAll.populationStandardDeviation = Double.NaN;
                        statisticAll.sampleStandardDeviation = Double.NaN;
                    }
                    break;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    /*
        set
     */
    public Data2DStatistic setType(Type type) {
        this.type = type;
        return this;
    }

    public Data2DStatistic setStatisticData(DoubleStatistic[] statisticData) {
        this.statisticData = statisticData;
        return this;
    }

    public Data2DStatistic setStatisticAll(DoubleStatistic statisticAll) {
        this.statisticAll = statisticAll;
        return this;
    }

    public Data2DStatistic setStatisticCalculation(DescriptiveStatistic statisticCalculation) {
        this.statisticCalculation = statisticCalculation;
        return this;
    }

    public Data2DStatistic setSumAbs(boolean sumAbs) {
        this.sumAbs = sumAbs;
        return this;
    }

}
