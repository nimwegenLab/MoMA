package com.jug.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeProvider {
    public static String getDateTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd-HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        return dtf.format(localDateTime);
    }
}
