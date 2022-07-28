package com.project.auth.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.util.Locale;
import org.apache.logging.log4j.util.Strings;

public final class NumberUtils {

    private NumberUtils() {
    }

    public static BigDecimal numberFormat(BigDecimal bigDecimal) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(4);
        return new BigDecimal(numberFormat.format(bigDecimal).replace(",",
                Strings.EMPTY), MathContext.UNLIMITED);
    }

    public static String numberFormatTwoDecimal(BigDecimal bigDecimal) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(bigDecimal).replace(",", Strings.EMPTY).replace(".", ",");
    }

    public static String generateReferenceNumber(Long referenceNumber) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMinimumIntegerDigits(10);
        return numberFormat.format(referenceNumber).replace(",", Strings.EMPTY);
    }

}
