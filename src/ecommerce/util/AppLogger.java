package ecommerce.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {}

    public enum Niveau { INFO, WARN, ERROR }

    public static void info(String message) {
        log(Niveau.INFO, message);
    }

    public static void warn(String message) {
        log(Niveau.WARN, message);
    }

    public static void error(String message) {
        log(Niveau.ERROR, message);
    }

    public static void error(String message, Throwable t) {
        log(Niveau.ERROR, message + " | " + t.getClass().getSimpleName() + ": " + t.getMessage());
    }

    private static void log(Niveau niveau, String message) {
        String ligne = String.format("[%s] [%s] %s",
                LocalDateTime.now().format(FMT), niveau, message);
        System.out.println(ligne);
            java.io.File dir = new java.io.File("logs");
            if (!dir.exists()) dir.mkdirs();

    }
}