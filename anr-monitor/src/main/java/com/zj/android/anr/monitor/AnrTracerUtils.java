package com.zj.android.anr.monitor;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnrTracerUtils {
    private static final long FOREGROUND_MSG_THRESHOLD = -2000;
    private static final long BACKGROUND_MSG_THRESHOLD = -10000;

    private static final int CHECK_ERROR_STATE_INTERVAL = 500;
    private static final int ANR_DUMP_MAX_TIME = 20000;
    private static final int CHECK_ERROR_STATE_COUNT =
            ANR_DUMP_MAX_TIME / CHECK_ERROR_STATE_INTERVAL;

    @RequiresApi(api = Build.VERSION_CODES.M)
    static boolean isMainThreadBlocked() {
        try {
            MessageQueue mainQueue = Looper.getMainLooper().getQueue();
            Field field = mainQueue.getClass().getDeclaredField("mMessages");
            field.setAccessible(true);
            final Message mMessage = (Message) field.get(mainQueue);
            if (mMessage != null) {
                long when = mMessage.getWhen();
                if (when == 0) {
                    return false;
                }
                long time = when - SystemClock.uptimeMillis();
                long timeThreshold = BACKGROUND_MSG_THRESHOLD;
                boolean currentForeground = isActivityInterestingToUser();
                if (currentForeground) {
                    timeThreshold = FOREGROUND_MSG_THRESHOLD;
                }
                return time < timeThreshold;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    static void checkErrorStateCycle(Application application) {
        int checkErrorStateCount = 0;
        while (checkErrorStateCount < CHECK_ERROR_STATE_COUNT) {
            try {
                checkErrorStateCount++;
                boolean myAnr = checkErrorState(application);
                if (myAnr) {
                    report();
                    break;
                }

                Thread.sleep(CHECK_ERROR_STATE_INTERVAL);
            } catch (Throwable t) {
                break;
            }
        }
    }

    private static boolean checkErrorState(Application application) {
        try {
            ActivityManager am = (ActivityManager) application
                    .getSystemService(Context.ACTIVITY_SERVICE);

            List<ActivityManager.ProcessErrorStateInfo> procs = am.getProcessesInErrorState();
            if (procs == null) {
                return false;
            }

            for (ActivityManager.ProcessErrorStateInfo proc : procs) {

                if (proc.uid != android.os.Process.myUid()
                        && proc.condition == ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING) {
                    return false;
                }

                if (proc.pid != android.os.Process.myPid()) continue;

                if (proc.condition != ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING) {
                    continue;
                }

                return true;
            }
            return false;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    private static boolean isActivityInterestingToUser() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                activities = (HashMap<Object, Object>) activitiesField.get(activityThread);
            } else {
                activities = (ArrayMap<Object, Object>) activitiesField.get(activityThread);
            }
            if (activities.size() < 1) {
                return false;
            }
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");

                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getMainThreadJavaStackTrace() {
        StringBuilder stackTrace = new StringBuilder();
        for (StackTraceElement stackTraceElement : Looper.getMainLooper().getThread().getStackTrace()) {
            stackTrace.append(stackTraceElement.toString()).append("\n");
        }
        return stackTrace.toString();
    }

    static void report() {
        Log.e("AnrTracer", "detect Anr");
        Log.e("AnrTracer", getMainThreadJavaStackTrace());
    }
}
