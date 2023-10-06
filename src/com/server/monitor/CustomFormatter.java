package com.server.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;

public class CustomFormatter extends Formatter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("[").append(dateFormat.format(new Date(record.getMillis()))).append("] ");
//        logMessage.append("[").append(record.getLevel()).append("] ");
//        logMessage.append("[").append(record.getSourceClassName()).append("] ");
        logMessage.append(record.getMessage()).append(System.lineSeparator());
        return logMessage.toString();
    }
}
