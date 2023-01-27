package mara.mybox.calculation;

import java.util.HashMap;
import java.util.Map;
import mara.mybox.tools.ShortTools;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-10-4
 * @License Apache License Version 2.0
 */
public class ShortStatistic {

    private String name;
    private int count;
    private short minimum, maximum, mode, median;
    private double sum, mean, variance, skewness;

    public ShortStatistic() {
    }

    public ShortStatistic(short[] values) {
        if (values == null || values.length == 0) {
            return;
        }
        count = values.length;
        sum = 0;
        minimum = Short.MAX_VALUE;
        maximum = Short.MIN_VALUE;
        for (int i = 0; i < count; ++i) {
            short v = values[i];
            sum += v;
            if (v > maximum) {
                maximum = v;
            }
            if (v < minimum) {
                minimum = v;
            }
        }
        mean = sum / values.length;
        variance = 0;
        skewness = 0;
        mode = mode(values);
        median = median(values);
        for (int i = 0; i < values.length; ++i) {
            short v = values[i];
            variance += Math.pow(v - mean, 2);
            skewness += Math.pow(v - mean, 3);
        }
        variance = Math.sqrt(variance / count);
        skewness = Math.cbrt(skewness / count);
    }


    /*
        static methods
     */
    public static double sum(short[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
        }
        double sum = 0;
        for (int i = 0; i < values.length; ++i) {
            sum += values[i];
        }
        return sum;
    }

    public static short maximum(short[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
        }
        short max = Short.MIN_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public static short minimum(short[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
        }
        short min = Short.MAX_VALUE;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public static double mean(short[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
        }
        return sum(values) / values.length;
    }

    public static short mode(short[] values) {

        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
        }
        short mode = 0;
        Map<Short, Integer> number = new HashMap<>();
        for (short value : values) {
            if (number.containsKey(value)) {
                number.put(value, number.get(value) + 1);
            } else {
                number.put(value, 1);
            }
        }
        short num = 0;
        for (short value : number.keySet()) {
            if (num < number.get(value)) {
                mode = value;
            }
        }
        return mode;
    }

    public static short median(short[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
        }
        short[] sorted = ShortTools.sortArray(values);
        int len = sorted.length;
        if (len % 2 == 0) {
            return (short) ((sorted[len / 2] + sorted[len / 2 + 1]) / 2);
        } else {
            return sorted[len / 2];
        }
    }

    public static double variance(short[] values) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
        }
        double mean = mean(values);
        return variance(values, mean);
    }

    public static double variance(short[] values, double mean) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
        }
        double variance = 0;
        for (int i = 0; i < values.length; ++i) {
            variance += Math.pow(values[i] - mean, 2);
        }
        variance = Math.sqrt(variance / values.length);
        return variance;
    }

    public static double skewness(short[] values, double mean) {
        if (values == null || values.length == 0) {
            return AppValues.InvalidShort;
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

    public void setSum(short sum) {
        this.sum = sum;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(short mean) {
        this.mean = mean;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(short variance) {
        this.variance = variance;
    }

    public double getSkewness() {
        return skewness;
    }

    public void setSkewness(short skewness) {
        this.skewness = skewness;
    }

    public short getMinimum() {
        return minimum;
    }

    public void setMinimum(short minimum) {
        this.minimum = minimum;
    }

    public short getMaximum() {
        return maximum;
    }

    public void setMaximum(short maximum) {
        this.maximum = maximum;
    }

    public short getMode() {
        return mode;
    }

    public void setMode(short mode) {
        this.mode = mode;
    }

    public short getMedian() {
        return median;
    }

    public void setMedian(short median) {
        this.median = median;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
