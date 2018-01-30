import java.io.IOException;
import java.util.logging.FileHandler; 
import java.util.logging.ConsoleHandler; 
//import java.util.logging.Level; 
import java.util.logging.Logger; 
import java.util.logging.LogRecord; 
import java.util.logging.Formatter; 
import java.util.logging.Handler; 
//import java.util.logging.SimpleFormatter; 

public class TheLogger {

	private static FileHandler s_fileHandler;
//	private static SimpleFormatter s_formatter;
	private static Formatter s_formatter;
	
	public static void setup() throws IOException {
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		
		Logger rootLogger = Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		if(handlers[0] instanceof ConsoleHandler) {
			rootLogger.removeHandler(handlers[0]);
		}
		
		s_fileHandler = new FileHandler("logs\\log.txt");
		// s_formatter = new SimpleFormatter();
		s_formatter = new LogEntryFormatter();
		s_fileHandler.setFormatter(s_formatter);
		logger.addHandler(s_fileHandler);
	}
}

class LogEntryFormatter extends Formatter {
	
	public String format(LogRecord r) {
		String s = "";
		s += r.getLevel() + " : " + r.getMessage() + "\r\n";				
		return s;
	}
}
