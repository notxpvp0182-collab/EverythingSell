package com.ahmed.everythingsell.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class NumberUtil {

    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final ThreadLocal<DecimalFormat> WHOLE_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("#,##0", SYMBOLS));
    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.00", SYMBOLS));

    private NumberUtil() {
    }

    public static String formatAmount(double value) {
        if (!Double.isFinite(value) || value < 0.0D) {
            return "0";
        }
        double rounded = Math.round(value * 100.0D) / 100.0D;
        if (Math.abs(rounded - Math.rint(rounded)) < 0.001D) {
            return WHOLE_FORMAT.get().format(rounded);
        }
        return DECIMAL_FORMAT.get().format(rounded);
    }

    public static String formatCurrency(String currencySymbol, double value) {
        String symbol = (currencySymbol == null) ? "$" : currencySymbol;
        return symbol + formatAmount(value);
    }
}
