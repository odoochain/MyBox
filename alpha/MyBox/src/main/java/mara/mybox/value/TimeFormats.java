package mara.mybox.value;

import java.util.TimeZone;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class TimeFormats {

    public static final TimeZone zoneUTC = TimeZone.getTimeZone("GMT+0"); // UTC
    public static final TimeZone zoneZhCN = TimeZone.getTimeZone("GMT+8"); // Beijing zone, UTC+0800

    public static final String Datetime = "yyyy-MM-dd HH:mm:ss";
    public static final String Date = "yyyy-MM-dd";
    public static final String Month = "yyyy-MM";
    public static final String Year = "yyyy";
    public static final String Time = "HH:mm:ss";
    public static final String TimeMs = "HH:mm:ss.SSS";
    public static final String DatetimeMs = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String Datetime2 = "yyyy-MM-dd_HH-mm-ss-SSS";
    public static final String Datetime3 = "yyyyMMddHHmmssSSS";
    public static final String Datetime4 = "yyyy-MM-dd_HH-mm-ss";
    public static final String DatetimeFormat5 = "yyyy.MM.dd HH:mm:ss";

    public static final String EraDatetimeEn = "y-M-d H:m:s G";
    public static final String EraDatetimeMsEn = "y-M-d H:m:s.S G";
    public static final String EraDateEn = "y-M-d G";
    public static final String EraMonthEn = "y-M G";
    public static final String EraYearEn = "y G";

    public static final String EraDatetimeZh = "Gy-M-d H:m:s";
    public static final String EraDatetimeMsZh = "Gy-M-d H:m:s.S";
    public static final String EraDateZh = "Gy-M-d";
    public static final String EraMonthZh = "Gy-M";
    public static final String EraYearZh = "Gy";

    public static final String EraDatetime = "y-M-d H:m:s";
    public static final String EraDatetimeMs = "y-M-d H:m:s.S";
    public static final String EraDate = "y-M-d";
    public static final String EraMonth = "y-M";
    public static final String EraYear = "y";
    public static final String EraTime = "H:m:s";
    public static final String EraTimeMs = "H:m:s.S";

    public static final String DatetimeE = "MM/dd/yyyy HH:mm:ss";
    public static final String DateE = "MM/dd/yyyy";
    public static final String MonthE = "MM/yyyy";
    public static final String DatetimeMsE = "MM/dd/yyyy HH:mm:ss.SSS";
}
