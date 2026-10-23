package com.gathin.tvhome;

import android.content.res.Resources;
import java.util.Calendar;

/**
 * Vietnamese/East Asian Lunar Calendar based on astronomical algorithm by Hồ Ngọc Đức.
 * Pure arithmetic without external libraries, deterministic and lightweight.
 */
final class Lunar {

    private Lunar() {
    }

    private static int jdFromDate(int dd, int mm, int yy) {
        int a = (14 - mm) / 12;
        int y = yy + 4800 - a;
        int m = mm + 12 * a - 3;
        int jd = dd + ((153 * m + 2) / 5) + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
        if (jd < 2299161) {
            jd = dd + ((153 * m + 2) / 5) + 365 * y + y / 4 - 32083;
        }
        return jd;
    }

    private static int getNewMoonDay(int k, double timeZone) {
        double t = k / 1236.85;
        double t2 = t * t;
        double t3 = t2 * t;
        double dr = Math.PI / 180;
        double jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3;
        jd1 += 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * dr);
        double m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3;
        double mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3;
        double f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3;
        double c1 = (0.1734 - 0.000393 * t) * Math.sin(m * dr) + 0.0021 * Math.sin(2 * m * dr);
        c1 -= 0.4068 * Math.sin(mpr * dr) + 0.0161 * Math.sin(2 * mpr * dr);
        c1 -= 0.0004 * Math.sin(3 * mpr * dr);
        c1 += 0.0104 * Math.sin(2 * f * dr) - 0.0051 * Math.sin((m + mpr) * dr);
        c1 -= 0.0074 * Math.sin((m - mpr) * dr) + 0.0004 * Math.sin((2 * f + m) * dr);
        c1 -= 0.0004 * Math.sin((2 * f - m) * dr) - 0.0006 * Math.sin((2 * f + mpr) * dr);
        c1 += 0.0010 * Math.sin((2 * f - mpr) * dr) + 0.0005 * Math.sin((m + 2 * mpr) * dr);
        return (int) (jd1 + c1 + 0.5 + timeZone / 24.0);
    }

    private static int getSunLongitude(int jdn, double timeZone) {
        double t = (jdn - 2451545.5 - timeZone / 24.0) / 36525.0;
        double t2 = t * t;
        double dr = Math.PI / 180;
        double l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2;
        l0 = l0 % 360.0;
        double m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * (t2 * t);
        double c = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(m * dr);
        c += (0.019993 - 0.000101 * t) * Math.sin(2 * m * dr) + 0.000290 * Math.sin(3 * m * dr);
        double theta = (l0 + c) % 360.0;
        if (theta < 0) theta += 360.0;
        return (int) (theta / 30.0);
    }

    private static int getLeapMonthOffset(int a11, double timeZone) {
        int k = (int) ((a11 - 2415021.076998695) / 29.530588853);
        int last = 0;
        for (int i = 1; i <= 14; i++) {
            int nm = getNewMoonDay(k + i, timeZone);
            int sunLong = getSunLongitude(nm, timeZone);
            if (sunLong == last) return i - 1;
            last = sunLong;
        }
        return 0;
    }

    /**
     * Converts solar date to lunar date.
     * Returns int[]{day, month, year, leap (1 if leap month else 0)}.
     */
    public static int[] toLunar(int dd, int mm, int yy, double timeZone) {
        int dayNumber = jdFromDate(dd, mm, yy);
        int k = (int) ((dayNumber - 2415021.076998695) / 29.530588853);
        int monthStart = getNewMoonDay(k + 1, timeZone);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone);
        } else {
            k++;
        }

        int a11 = getNewMoonDay((int) ((jdFromDate(31, 12, mm >= 11 ? yy : yy - 1) - 2415021.076998695) / 29.530588853), timeZone);
        while (getSunLongitude(a11, timeZone) >= 9) {
            int kTemp = (int) ((a11 - 2415021.076998695) / 29.530588853) - 1;
            a11 = getNewMoonDay(kTemp, timeZone);
        }

        int a11Next = getNewMoonDay((int) ((jdFromDate(31, 12, mm >= 11 ? yy + 1 : yy) - 2415021.076998695) / 29.530588853), timeZone);
        while (getSunLongitude(a11Next, timeZone) >= 9) {
            int kTemp = (int) ((a11Next - 2415021.076998695) / 29.530588853) - 1;
            a11Next = getNewMoonDay(kTemp, timeZone);
        }

        int lunarDay = dayNumber - monthStart + 1;
        int diff = (int) ((monthStart - a11) / 29.0);
        int lunarLeap = 0;
        int lunarMonth = diff + 11;

        if (a11Next - a11 > 365) {
            int leapMonthDiff = getLeapMonthOffset(a11, timeZone);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    lunarLeap = 1;
                }
            }
        }
        if (lunarMonth > 12) lunarMonth = lunarMonth - 12;
        if (lunarMonth > 12) lunarMonth = lunarMonth - 12;

        int lunarYear = mm >= 11 && lunarMonth < 3 ? yy + 1 : (mm < 3 && lunarMonth >= 11 ? yy - 1 : yy);
        return new int[]{lunarDay, lunarMonth, lunarYear, lunarLeap};
    }

    public static String today(Resources res, int utcOffset) {
        Calendar cal = Calendar.getInstance();
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        int mm = cal.get(Calendar.MONTH) + 1;
        int yy = cal.get(Calendar.YEAR);
        int[] l = toLunar(dd, mm, yy, utcOffset);
        String suffix = l[3] == 1 ? res.getString(R.string.lunar_leap_suffix) : res.getString(R.string.lunar_suffix);
        return res.getString(R.string.lunar_date_format, l[0], l[1], suffix);
    }
}
