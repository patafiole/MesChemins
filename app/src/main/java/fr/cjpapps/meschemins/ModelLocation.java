package fr.cjpapps.meschemins;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

public class ModelLocation extends AndroidViewModel {

    private final MutableLiveData<Location> positionActuelle = new MutableLiveData<>();
    private final MutableLiveData<HashMap<String, String>> tableauPosition = new MutableLiveData<>();
    private Intent locationIntent;
    private final SharedPreferences mesPrefs;
    private final SharedPreferences.Editor editeur;
    private final Resources resources;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ModelLocation(Application application) {
        super (application);
        mesPrefs =  MyHelper.getInstance().recupPrefs();
        editeur = mesPrefs.edit();
        resources = MyHelper.getInstance().recupResources();
    }

    MutableLiveData<Location> getPositionActuelle() { return positionActuelle; }
    MutableLiveData<HashMap<String, String>> getTableauPosition() { return tableauPosition; }

    void updatePosition() {
        if (BuildConfig.DEBUG){
        Log.i("APPCHEMINS", "model dans updateposition");}
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiver, new IntentFilter(Constantes.ACTION_GETLOCATION));
    }

    void stopUpdatePosition(){
//        repository.stopLocationUpdates();
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onCleared(){
        super.onCleared();
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(broadcastReceiver);
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "location receiver actif");}
/*            String action = intent.getAction();
            if (action.equals("finish_activity")) {
                finish();
            } */
            Location location = intent.getParcelableExtra(Constantes.EXTRA_LOCATION);
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "model loc reçue "+location.toString());}
            positionActuelle.setValue(location);
            ajoutTrackPoint(location);
        }
    };

    void ajoutTrackPoint(Location location) {
        HashMap<String, String> paramsPosition = new HashMap<>();
        String trackPoint;
        String textAlti = "";
        double accuracy = 100.0;
        String precision = "";
        Instant instant = Instant.now(); // Current moment in UTC.
        String timeLocation = instant.toString();  // au format ISO 8601, ex : 2016-03-23T03:09:01.613Z
        String maintenant = LocalTime.now().format(formatter);
        double latitude = location.getLatitude();
        String latitudeStr = String.format(Locale.US, "%.5f", latitude);
        double longitude = location.getLongitude();
        String longitudeStr = String.format(Locale.US, "%.5f", longitude);

        if (!mesPrefs.getBoolean("encours", false)) {
            editeur.putFloat("valgeoide", Aux.getGeoidValue(latitude, longitude));
            editeur.putBoolean("encours", true);
            editeur.commit();
        }

        if (location.hasAltitude()) {
            Log.i("APPCHEMINS", "valgeoide = " + mesPrefs.getFloat("valgeoide", 48.6f));
            double altitude = location.getAltitude() - mesPrefs.getFloat("valgeoide", 48.6f);
            textAlti = String.valueOf((int) altitude); }
        if (location.hasAccuracy()) {
            accuracy = location.getAccuracy();
            precision = String.valueOf((int) accuracy);
            if (BuildConfig.DEBUG) {
                Log.i("APPCHEMINS", "précision = " + precision);
            }
//        }
            if (accuracy <= 30.0) {
                trackPoint = resources.getString(R.string.trkpt, latitudeStr, longitudeStr, textAlti, timeLocation);
                if (BuildConfig.DEBUG) {
                    Log.i("APPCHEMINS", "trackpoint = " + trackPoint);
                }
                String track = mesPrefs.getString("leChemin", "");
                editeur.putString("leChemin", track + trackPoint);
                editeur.apply();
                paramsPosition.put("heure", maintenant);
                paramsPosition.put("lat", latitudeStr);
                paramsPosition.put("lon", longitudeStr);
                paramsPosition.put("altitude", textAlti);
                paramsPosition.put("precision", precision);
                if (BuildConfig.DEBUG) {
                    Log.i("APPCHEMINS", "paramsPoint = " + paramsPosition);
                }
                    tableauPosition.setValue(paramsPosition);
            }
        }
    }
}
