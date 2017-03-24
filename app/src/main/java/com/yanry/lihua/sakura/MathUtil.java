package com.yanry.lihua.sakura;

/**
 * Created by rongyu.yan on 3/21/2017.
 */

public class MathUtil {

    public static int bezierEvaluate(float t, int startValue, int endValue, int...medianValues) {
        int n = medianValues.length + 1;
        int value = (int) (startValue * Math.pow(1 - t, n) + endValue * Math.pow(t, n));
        for (int i = 1; i < n; i++) {
            value += getPascalTriangleCoefficient(n, i) * medianValues[i - 1] * Math.pow(1 - t, n - i) * Math.pow(t, i);
        }
        return value;
    }

    public static int getPascalTriangleCoefficient(int power, int index) {
        if (power >= 0 && index >= 0 && index < power) {
            if (index == 0 || index == power - 1) {
                return 1;
            }
            return getPascalTriangleCoefficient(power - 1, index - 1) + getPascalTriangleCoefficient(power - 1, index);
        }
        throw new IllegalArgumentException(String.format("power:%s, index:%s", power, index));
    }
}
