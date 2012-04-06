package lamothe.android.backup;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
	
    public static String ThrowableToString(Throwable throwable)
    {
    	StringWriter sw = new StringWriter();
    	throwable.printStackTrace(new PrintWriter(sw));
    	return sw.toString();
    }

}
