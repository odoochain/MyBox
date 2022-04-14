package mara.mybox.data;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleArrayTools;
import mara.mybox.value.AppValues;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;

/**
 * @Author Mara
 * @CreateDate 2021-10-4
 * @License Apache License Version 2.0
 */
public class DoubleStatistic {

    public String name;
    public int count;
    public double sum, mean, geometricMean, minimum, maximum, sumSquares,
            populationVariance, sampleVariance, populationStandardDeviation, sampleStandardDeviation,
            skewness, mode, median, vTmp;

    public DoubleStatistic() {
        init();
    }

    public final void init() {
        count = 0;
        sum = 0;
        mean = 0;
        maximum = -Double.MAX_VALUE;
        minimum = Double.MAX_VALUE;
        geometricMean = 1;
        sumSquares = 0;
        populationVariance = 0;
        sampleVariance = 0;
        populationStandardDeviation = 0;
        sampleStandardDeviation = 0;
        skewness = 0;
        mode = 0;
        median = 0;
        vTmp = 0;
    }

    public DoubleStatistic(double[] values) {
        calculate(values, StatisticSelection.all(true));
    }

    public DoubleStatistic(double[] values, StatisticSelection selections) {
        calculate(values, selections);
    }

    public final void calculate(double[] values, StatisticSelection selections) {
        try {
            init();
            if (values == null || values.length == 0 || selections == null) {
                return;
            }
            sum = 0;
            count = values.length;
            for (int i = 0; i < count; ++i) {
                double v = values[i];
                sum += v;
                if (selections.isMaximum() && v > maximum) {
                    maximum = v;
                }
                if (selections.isMinimum() && v < minimum) {
                    minimum = v;
                }
                if (selections.isGeometricMean()) {
                    geometricMean = geometricMean * v;
                }
                if (selections.isSumSquares()) {
                    sumSquares += v * v;
                }
            }
            mean = sum / count;
            if (selections.isGeometricMean()) {
                geometricMean = Math.pow(geometricMean, 1d / count);
            }
            if (selections.isPopulationStandardDeviation() || selections.isPopulationVariance()
                    || selections.isSampleStandardDeviation() || selections.isSampleVariance()) {
                vTmp = 0;
                for (int i = 0; i < count; ++i) {
                    double p = values[i] - mean;
                    double p2 = p * p;
                    vTmp += p2;
                }
                populationVariance = vTmp / count;
                sampleVariance = vTmp / (count - 1);
                if (selections.populationStandardDeviation) {
                    populationStandardDeviation = Math.sqrt(populationVariance);
                }
                if (selections.isSampleStandardDeviation()) {
                    sampleStandardDeviation = Math.sqrt(sampleVariance);
                }
            }
            if (selections.isSkewness()) {
                skewness = new Skewness().evaluate(values);
            }
            if (selections.isMode()) {
                mode = mode(values);
            }
            if (selections.isMedian()) {
                median = median(values);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static double sum(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double sum = 0;
        for (int i = 0; i < values.length; ++i) {
            sum += values[i];
        }
        return sum;
    }

    public static double maximum(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public static double minimum(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public static double mean(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        return sum(values) / values.length;
    }

    public static double mode(double[] values) {
        try {
            if (values == null || values.length == 0) {
                return AppValues.InvalidDouble;
            }
            double mode = 0;
            Map<Double, Integer> number = new HashMap<>();
            for (double value : values) {
                if (number.containsKey(value)) {
                    number.put(value, number.get(value) + 1);
                } else {
                    number.put(value, 1);
                }
            }
            double num = 0;
            for (double value : number.keySet()) {
                if (num < number.get(value)) {
                    mode = value;
                }
            }
            return mode;
        } catch (Exception e) {
            return AppValues.InvalidDouble;
        }
    }

    public static double median(double[] values) {
        try {
            if (values == null || values.length == 0) {
                return AppValues.InvalidDouble;
            }
            double[] sorted = DoubleArrayTools.sortArray(values);
            int len = sorted.length;
            if (len == 2) {
                return (sorted[0] + sorted[1]) / 2;
            } else if (len % 2 == 0) {
                return (sorted[len / 2] + sorted[len / 2 + 1]) / 2;
            } else {
                return sorted[len / 2];
            }
        } catch (Exception e) {
            return AppValues.InvalidDouble;
        }
    }

    public static double variance(double[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double mean = mean(values);
        return variance(values, mean);
    }

    public static double variance(double[] values, double mean) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double variance = 0;
        for (int i = 0; i < values.length; ++i) {
            variance += Math.pow(values[i] - mean, 2);
        }
        variance = Math.sqrt(variance / values.length);
        return variance;
    }

    public static double skewness(double[] values, double mean) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidDouble;
        }
        double skewness = 0;
        for (int i = 0; i < values.length; ++i) {
            skewness += Math.pow(values[i] - mean, 3);
        }
        skewness = Math.cbrt(skewness / values.length);
        return skewness;
    }

    /*
        get/set
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getPopulationVariance() {
        return populationVariance;
    }

    public void setPopulationVariance(double populationVariance) {
        this.populationVariance = populationVariance;
    }

    public double getSkewness() {
        return skewness;
    }

    public void setSkewness(double skewness) {
        this.skewness = skewness;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public double getMode() {
        return mode;
    }

    public void setMode(double mode) {
        this.mode = mode;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getGeometricMean() {
        return geometricMean;
    }

    public void setGeometricMean(double geometricMean) {
        this.geometricMean = geometricMean;
    }

    public double getSumSquares() {
        return sumSquares;
    }

    public void setSumSquares(double sumSquares) {
        this.sumSquares = sumSquares;
    }

    public double getSampleVariance() {
        return sampleVariance;
    }

    public void setSampleVariance(double sampleVariance) {
        this.sampleVariance = sampleVariance;
    }

    public double getPopulationStandardDeviation() {
        return populationStandardDeviation;
    }

    public void setPopulationStandardDeviation(double populationStandardDeviation) {
        this.populationStandardDeviation = populationStandardDeviation;
    }

    public double getSampleStandardDeviation() {
        return sampleStandardDeviation;
    }

    public void setSampleStandardDeviation(double sampleStandardDeviation) {
        this.sampleStandardDeviation = sampleStandardDeviation;
    }

}
