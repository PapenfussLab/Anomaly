package org.petermac.nic.api;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Nic on 15/08/2018.
 */
public class ErrorDetails
{
    private Date timestamp;
    private String message;
    private String details;
    private Exception ex;

    public ErrorDetails(String message, String details)
    {
        super();
        this.timestamp = new Date();
        this.message = message;
        this.details = details;
    }

    public ErrorDetails(Exception ex, String description)
    {
        this(ex.toString(), description);
        this.ex = ex;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public String getMessage()
    {
        return message;
    }

    public String getDetails()
    {
        return details;
    }

//        public String getStackTraceString()
//        {
//            if (ex != null)
//            {
//                final StringWriter stringWriter = new StringWriter();
//                final PrintWriter printWriter = new PrintWriter(stringWriter);
//                ex.printStackTrace(printWriter);
//                printWriter.close();
//                return stringWriter.toString();
//            }
//            return "";
//        }

    public List<String> getStackTrace()
    {
        if (ex != null)
            return Stream.of(ex.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList());
        return null;
    }
}
