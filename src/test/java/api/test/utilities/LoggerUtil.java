package api.test.utilities;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerUtil {
    static private Logger logger = LogManager.getLogger(LoggerUtil.class.getName());

    public static void info(String format, Object... args) {
        logger.printf(Level.INFO, format, args);

    }

    public static void error(String format, Object... args) {
        logger.printf(Level.ERROR, format, args);
    }
}
