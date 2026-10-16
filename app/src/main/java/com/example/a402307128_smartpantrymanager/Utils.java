package com.example.a402307128_smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Small helper methods shared by several screens:
 * quantity formatting, expiry-date maths and the (SharedPreferences) settings.
 */
public final class Utils {

    /** Ingredients expiring within this many days are highlighted. */
    public static final int EXPIRING_DAYS = 3;

    private static final String PREFS_NAME = "smart_pantry_prefs";
    private static final String KEY_EXPIRING_ALERTS = "expiring_alerts";

    private Utils() { }   // utility class, never instantiated

    /** 2.0 -> "2", 2.5 -> "2.5", 0.333 -> "0.33". */
    public static String formatQuantity(double quantity) {
        return BigDecimal.valueOf(quantity)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    /**
     * Whole days from today until the given "yyyy-MM-dd" date.
     * Negative = already expired. Returns Long.MAX_VALUE for a blank/invalid date.
     */
    public static long daysUntil(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return Long.MAX_VALUE;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date expiry = format.parse(isoDate.trim());

            Calendar today = Calendar.getInstance();   // today at midnight
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            double days = (expiry.getTime() - today.getTimeInMillis()) / 86400000.0;
            return Math.round(days);                   // round() absorbs daylight-saving shifts
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    // ---- Settings (stored with SharedPreferences so they survive app restarts) ----

    public static boolean isExpiringAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRING_ALERTS, true);
    }

    public static void setExpiringAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRING_ALERTS, enabled).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
