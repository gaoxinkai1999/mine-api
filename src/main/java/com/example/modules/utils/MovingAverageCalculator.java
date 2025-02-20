package com.example.modules.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MovingAverageCalculator {

    /**
     * 计算简单移动平均，前 period-1 天填充 null
     *
     * @param data   原始数据
     * @param period 移动平均周期
     * @return 移动平均值数组（长度与原始数据一致）
     */
    public static Double[] calculateSimpleMovingAverage(double[] data, int period) {
        if (data == null || data.length < period) {
            throw new IllegalArgumentException("数据不足，无法计算移动平均");
        }

        Double[] movingAverages = new Double[data.length];
        double sum = 0.0;

        // 初始化前 period-1 天为 null
        for (int i = 0; i < period - 1; i++) {
            movingAverages[i] = null;
        }

        // 计算第一个窗口的总和
        for (int i = 0; i < period; i++) {
            sum += data[i];
        }
        //如果计算出的移动平均值为0，将移动平均值设为null
        movingAverages[period - 1] = round(sum / period) == 0 ? null : round(sum / period); // 保留两位小数

        // 滑动窗口计算后续平均值
        for (int i = period; i < data.length; i++) {
            sum = sum - data[i - period] + data[i];
            movingAverages[i] = round(sum / period) == 0 ? null : round(sum / period); // 保留两位小数
        }

        return movingAverages;
    }

    /**
     * 四舍五入保留两位小数
     *
     * @param value 原始值
     * @return 四舍五入后的值
     */
    private static double round(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}