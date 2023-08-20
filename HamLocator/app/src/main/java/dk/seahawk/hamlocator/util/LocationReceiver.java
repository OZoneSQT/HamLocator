package dk.seahawk.hamlocator.util;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class LocationReceiver extends ResultReceiver {

    private Receiver mReceiver;
    private String TAG = "LocationReceiver";

    public LocationReceiver(Handler handler) {
        super(handler);
        Log.d(TAG, "init LocationReceiver");
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
        Log.d(TAG, "setReceiver()");
    }

    public interface Receiver {
        void onLocationReceived(Location location);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResult()");
        if (mReceiver != null) {
            Log.d(TAG, "mReceiver != null");
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                Log.d(TAG, "resultCode == Activity.RESULT_OK && resultData != null");
                Location location = resultData.getParcelable("location");
                mReceiver.onLocationReceived(location);
            }
        }
    }
}
