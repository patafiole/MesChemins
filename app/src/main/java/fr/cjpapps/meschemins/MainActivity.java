package fr.cjpapps.meschemins;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import fr.cjpapps.meschemins.databinding.ActivityMainBinding;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// @RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity {

/* Pour continuer à saisir la posqition GPS lorsqu'on met le téléphone en poche il faut déjouer les piège
du système (Doze) et ceux des fabricants (économie d'énergie.
* Exemple Samsung A3 :
* -- paramètres > applis > menu >> accès spécial > optimiser batterie == veiller àà ce que MesChemins
*  ne soit pas cochée pour être optimisable
* -- paramètres > maintenance > batterie > contrôle énergie == veiller à ce que MesChemins ne puisse pas
*  être interdites de batteries si elle sont en arrière plan
* */
    ModelLocation model;
    TextView alti = null;
    TextView heure = null;
    TextView coordonn = null;
    TextView accuracy = null;
    Button start = null;
    Button pause = null;
    Button resume = null;
    Button save = null;
    Button cancel = null;
    SharedPreferences mesPrefs;
    final static String PREF_FILE = "mesInfos";
    Intent locationIntent;
    Boolean backgroundLocationGranted = false;
    Boolean foregroundLocationGranted = false;

//    private AppBarConfiguration appBarConfiguration;
//    private ActivityMainBinding binding;

/* TODO     - détection perte GPS ? pourquoi faire ?
            - position pendant veille pas réglé : OK 8 sur A3 ; à vérifier pour plus récents
            - tracé sur carte
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar); */
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        alti = findViewById(R.id.alti);
        heure = findViewById(R.id.heure);
        coordonn = findViewById(R.id.coordonnees);
        accuracy = findViewById(R.id.accuracy);
        start = findViewById(R.id.bouton_start);
        pause = findViewById(R.id.bouton_pause);
        resume = findViewById(R.id.bouton_resume);
        save = findViewById(R.id.bouton_save);
        cancel = findViewById(R.id.bouton_cancel);

// au passage, création de l'instance de MyHelper qui va stocker le contexte de l'application. Cela permettra de
// récupérer le context et donc en particulier les préférences de n'importe où en récupérant l'instance sans avoir
//   à passer de contexte
        mesPrefs =  MyHelper.getInstance(getApplicationContext()).recupPrefs();
        SharedPreferences.Editor editeur = mesPrefs.edit();

        checkGPSEnabled(); // ceci informe l'utilisateur si ce n'est pas le cas

        model = new ViewModelProvider(this).get(ModelLocation.class);
        locationIntent = new Intent(this, LocationTracker.class);

        LocalDate dateToday = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        editeur.putString("dateJour", dateToday.format(formatter));
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyMMdd");
        editeur.putString("dateFichier", dateToday.format(formatter2));
        editeur.putBoolean("fileReady", false);
        editeur.commit();

// observateur de la position GPS
        model.getTableauPosition().observe(this, tableau -> {
            heure.setText(tableau.get("heure"));
            alti.setText(getString(R.string.altitude,tableau.getOrDefault("altitude", "")));
            coordonn.setText(getString(R.string.lat_lon, tableau.get("lat"), tableau.get("lon")));
            accuracy.setText(getString(R.string.precision,tableau.getOrDefault("precision","")));
        });

        start.setOnClickListener(view -> {
            editeur.putString("leChemin", "");
            editeur.commit();
            if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.Q) {
                checkLocPermissionCodeQ();
            }else{
                checkLocPermission();
            }
        });

        pause.setOnClickListener(view -> model.stopUpdatePosition());

        resume.setOnClickListener(view -> {
            if (Variables.requestingLocationUpdates) {
                model.updatePosition();
            }
        });

        save.setOnClickListener(view -> {
            model.stopUpdatePosition();
            Intent finChemin = new Intent(this, ActOnChemin.class);
            startActivity(finChemin);
        });

        cancel.setOnClickListener(view -> {
            model.stopUpdatePosition();
            heure.setText(getString(R.string.real_time));
            alti.setText(getString(R.string.alti));
            coordonn.setText(getString(R.string.loc));
            accuracy.setText(getString(R.string.accuracy));
            editeur.putString("leChemin", "");
            editeur.apply();
        });
    }   // end onCreate


    /*  mix de https://developer.android.com/training/location/change-location-settings  et de
     * https://stackoverflow.com/questions/32423157/android-check-if-location-services-enabled-using-fused-location-provider
     * sauf que on simplifie : dans le cas de onFailure, au lieu d'envoyer l'utilisateur changer les paramètres on se contente
     * de lui signaler que son GPS n'est pas activé. A lui d'y aller s'il ouhaite avoir une position*/
    private void checkGPSEnabled() {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(request).build();
        LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsRequest)
                .addOnSuccessListener(this, locationSettingsResponse -> {
                    if (BuildConfig.DEBUG){
                        Log.i("APPCHEMINS", "GPS success");}
//                      Variables.isGPSAvailable = true;
                })
                .addOnFailureListener(this, e -> {
                    if (BuildConfig.DEBUG) {
                        Log.i("APPCHEMINS", "GPS failure");
                    }
//                  Variables.isGPSAvailable = false;
                    String message = "Attention, le GPS n'est pas activé.";
                    DialogAlertes infoGPS = DialogAlertes.newInstance(message);
                    infoGPS.show(getSupportFragmentManager(), "infoGPS");
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.help) {
            Intent lireAide = new Intent(MainActivity.this, Aide.class);
            startActivity(lireAide);
            return true;
        }
        if (id == R.id.archive) {
            Intent archives = new Intent(MainActivity.this, Archives.class);
            startActivity(archives);
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        model.stopUpdatePosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
/*        if (Variables.requestingLocationUpdates) {
            model.updatePosition();
        }  */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()  && !isChangingConfigurations()) {
            stopService(locationIntent);
        }
    }

    void checkLocPermission(){
// vérification de la permission de localisation, sauf pour Android 10 (Q)
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "on a déja la permission");}
            startService(locationIntent);
            model.updatePosition();
            Variables.requestingLocationUpdates = true;
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "demande permissions");}
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    void checkLocPermissionCodeQ(){
/* vérification de la permission de localisation pour SDK=29, Android 10. Pour 10 il faut demander
   explicitement la permission d'accès en background, cela peut se faire en même temps que les autres.
   Pour les versions suivantes il est interdit de faire la demande en même temps sous peine de n'avoir
   aucune permission. Il faut la demander après en faisant passer le client par les paramètres du téléphone*/
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "on a déja la permission");}
            startService(locationIntent);
            model.updatePosition();
            Variables.requestingLocationUpdates = true;
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "demande permissions");}
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            });
        }
    }

    // lanceur pour demander la permission de localisation
    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.get(
                                Manifest.permission.ACCESS_FINE_LOCATION);
                        Boolean coarseLocationGranted = result.get(
                                Manifest.permission.ACCESS_COARSE_LOCATION);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            if (BuildConfig.DEBUG){
                                Log.i("APPCHEMINS", "on obtient la permission");}
                     // pour Android > 10 on peut maintenant demander la permission de background
                     // on reviendra ici par la flèche de retour arrière du téléphone
                            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q) {
                                getBackgroundLocationPermission();
                            }
                            startService(locationIntent);
                            model.updatePosition();
                            Variables.requestingLocationUpdates = true;
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                            Toast.makeText(this, "Pas de localisation possible", Toast.LENGTH_LONG).show();
                        } else {
                            // No location access granted.
                            Toast.makeText(this, "Pas de localisation possible", Toast.LENGTH_LONG).show();
                        }
                    }
            );

    void getBackgroundLocationPermission() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Backround Location");
        alertDialog.setMessage("Aller dans Paramètres pour permettre GPS en permanence ?");
        alertDialog.setPositiveButton("Paramètres", (dialog, which) -> {
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "on demande background permission");}
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        alertDialog.show();    }
}