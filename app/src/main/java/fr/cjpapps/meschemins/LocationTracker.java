package fr.cjpapps.meschemins;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/* A partir de FaizanMubasher LocationTracker.java
* https://gist.github.com/FaizanMubasher/095824dd720b24be88f6526128a7ca1b*/

public class LocationTracker extends Service {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000; // Every 5 Seconds
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private PowerManager.WakeLock cpuWakeLock;

    public static final String CHANNEL_ID = "my_channel_id";
    public static final CharSequence CHANNEL_NAME = "MesChemins";
    private LocationRequest mLocationRequest;

    private Location mLocation; //current location
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

// variable "position" inutile dans la mesure où pour fonctionner pendant la veille on
// utilise un broadcast pour envoyer la position
    private final MutableLiveData<Location> position = new MutableLiveData<>();
    MutableLiveData<Location> getPosition() { return position; }


    public LocationTracker() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public void onCreate() {
        super.onCreate();
// maintenir le CPU en éveil
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MesChemins:gps_service");
        cpuWakeLock.acquire();  // en principe il fadrait un timeout pour être "bon citoyen"
        createNotificationChannel();  // nécessaire pour fonctionner avec appli en veille
        handleLocation();
//        Log.i("APPCHEMINS", "LocationTracker service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("APPCHEMINS", "Start service in foreground");
        super.onStartCommand(intent, flags, startId);
//        String NOTIFICATION_CHANNEL_ID = "fr.cjpapps.meschemins";

//        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("MesChemins is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
//                .setContentIntent(pendingIntent)
                .build();
        startForeground(300, notification);
        return START_STICKY;
    }

    private void handleLocation(){
        Log.i("APPCHEMINS", "Start location tracking");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e("APPCHEMINS", "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    private void createNotificationChannel(){
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.BLUE);
                notificationChannel.setSound(null, null);
                notificationChannel.setShowBadge(false);
                notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLocation = task.getResult();
                            position.setValue(task.getResult());
                            // Notify anyone listening for broadcasts about the new location.
                            Intent intent = new Intent(Constantes.ACTION_GETLOCATION);
                            intent.putExtra(Constantes.EXTRA_LOCATION, task.getResult());
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        } else {
                            Log.w("APPCHEMINS", "Failed to get location.");
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e("APPCHEMINS", "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
//        Log.i("APPCHEMINS", "New location: " + location);
        mLocation = location;
        position.setValue(location);  // pas utile actuellement
        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(Constantes.ACTION_GETLOCATION);
        intent.putExtra(Constantes.EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (cpuWakeLock.isHeld())
            cpuWakeLock.release();
    }
}
