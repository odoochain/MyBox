package mara.mybox.tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2019-5-28 15:31:28
 * @License Apache License Version 2.0
 */
public class FloatTools {

    public static boolean invalidFloat(float value) {
        return Float.isNaN(value) || value == AppValues.InvalidFloat;
    }

    public static String percentage(float data, float total) {
        try {
            String format = "#,###.##";
            DecimalFormat df = new DecimalFormat(format);
            return df.format(scale(data * 100 / total, 2));
        } catch (Exception e) {
            return data + "";
        }
    }

    public static String format(float data, int scale) {
        try {
            String format = "#,###";
            if (scale > 0) {
                format += "." + "#".repeat(scale);
            }
            DecimalFormat df = new DecimalFormat(format);
            return df.format(scale(data, scale));
        } catch (Exception e) {
            return data + "";
        }
    }

    public static float scale(float fvalue, int scale) {
        return (float) DoubleTools.scale(fvalue, scale);
    }

    public static float roundFloat2(float fvalue) {
        return scale(fvalue, 2);
    }

    public static float roundFloat5(float fvalue) {
        return scale(fvalue, 5);
    }

    public static double[] toDouble(float[] f) {
        if (f == null) {
            return null;
        }
        double[] d = new double[f.length];
        for (int i = 0; i < f.length; ++i) {
            d[i] = f[i];
        }
        return d;
    }

    public static double[][] toDouble(float[][] f) {
        if (f == null) {
            return null;
        }
        double[][] d = new double[f.length][f[0].length];
        for (int i = 0; i < f.length; ++i) {
            for (int j = 0; j < f[i].length; ++j) {
                d[i][j] = f[i][j];
            }
        }
        return d;
    }

    public static float random(Random r, int max, boolean nonNegative) {
        if (r == null) {
            r = new Random();
        }
        int sign = nonNegative ? 1 : r.nextInt(2);
        float f = r.nextFloat(max);
        return sign == 1 ? f : -f;
    }

    public static float[] sortArray(float[] numbers) {
        List<Float> list = new ArrayList<>();
        for (float i : numbers) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Float>() {
            @Override
            public int compare(Float p1, Float p2) {
                if (p1 > p2) {
                    return 1;
                } else if (p1 < p2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        float[] sorted = new float[numbers.length];
        for (int i = 0; i < list.size(); ++i) {
            sorted[i] = list.get(i);
        }
        return sorted;
    }

}
