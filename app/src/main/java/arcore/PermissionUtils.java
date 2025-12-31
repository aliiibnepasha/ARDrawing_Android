package arcore;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    public static boolean hasCameraPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(
                activity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(Activity activity, int code) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.CAMERA},
                code);
    }
}