package com.swsnowball.dragonreborn.util;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {
    public static double round(double value, int places) {
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}