package dk.seahawk.hamlocator.util;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MyExceptionHandler extends AppCompatActivity {

    public MyExceptionHandler() {
    }

    public void pushExceptionMessage(String message) {
        // System.out.println(message);

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.e("Error: ", message);
    }

}
