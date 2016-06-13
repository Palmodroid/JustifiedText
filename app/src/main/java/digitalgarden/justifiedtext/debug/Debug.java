package digitalgarden.justifiedtext.debug;

import android.content.Context;

import digitalgarden.justifiedtext.scribe.Scribe;


/**
 * Collection of message-limit constants for Scribe and
 * Scribe initialisation.
 */
public class Debug
    {
    // Constants for PRIMARY configuration
    public static final String LOG_TAG = "SCRIBE_JUST";

    /**
     * Scribe primary config initialisation.
     * @param context context containing package information
     */
    public static void initScribe( Context context )
        {
        Scribe.setConfig()
                .enableSysLog( LOG_TAG )                // Primary log-tag : BEST
                .init( context );                       // Primary file name : package name

        Scribe.checkLogFileLength(); // Primary log will log several runs
        Scribe.logUncaughtExceptions(); // Primary log will store uncaught exceptions

        Scribe.title("Justified text has started...");
        }

    }
