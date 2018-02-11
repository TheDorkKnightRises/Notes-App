package thedorkknightrises.notes.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Samriddha Basu on 29-06-2017.
 */

public class NetworkUtils {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
