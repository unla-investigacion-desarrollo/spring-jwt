package com.project.auth.constants;

import java.time.format.DateTimeFormatter;

public final class ConfigurationConstants {

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd");

    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    public static final int POSTGRESQL_LIMIT = 1650;

    private ConfigurationConstants() {
    }
}
