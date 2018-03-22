package org.iqiyi.datareact.utils;

import android.util.Log;

/**
 * Created by liangxu on 2018/1/24.
 */

public class DataReactLog {
    private static final String TAG = "DataReactLog";

    public static boolean DEBUG = false;

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    public static void v(Object... args) {
        if (DEBUG) {
            Log.v(TAG, getLogMessage(args));
        }
    }

    public static void d(Object... args) {
        if (DEBUG) {
            Log.d(TAG, getLogMessage(args));
        }
    }

    public static void e(Object... args) {
        if (DEBUG) {
            Log.e(TAG, getLogMessage(args));
        }
    }

    private static String getLogMessage(Object... msg) {
        if (msg != null && msg.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (Object s : msg) {
                if (msg != null && s != null) sb.append(s.toString());
            }
            return sb.toString();
        }
        return "";
    }

}
