package dk.seahawk.hamlocator.util;

import android.app.Activity;
import android.util.Log;

public class ActivityHolder {

    private static String TAG = "ActivityHolder";

    private static ActivityHolder instance;
    private Activity activity;

    private ActivityHolder() {
        Log.d(TAG, "init ActivityHolder");
    }

    public static ActivityHolder getInstance() {
        Log.d(TAG, "getInstance()");
        Log.d(TAG, "instance == null: " + (instance == null) );
        if (instance == null) {
            instance = new ActivityHolder();
        }
        Log.d(TAG, "instance == null: " + (instance == null) );
        return instance;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        Log.d(TAG, "setActivity()");
    }

    public Activity getActivity() {
        Log.d(TAG, "getActivity()");
        return activity;
    }

}
