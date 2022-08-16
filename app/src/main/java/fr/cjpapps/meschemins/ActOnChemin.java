package fr.cjpapps.meschemins;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import static fr.cjpapps.meschemins.Aux.ecrireFichier;
import static fr.cjpapps.meschemins.Aux.faitGPXChemin;
import static fr.cjpapps.meschemins.Aux.getPrivateDocStorageDir;
import static fr.cjpapps.meschemins.Aux.isExternalStorageWritable;
import static fr.cjpapps.meschemins.Aux.lireFichier;
import static fr.cjpapps.meschemins.Aux.removeAllAccents;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ActOnChemin extends AppCompatActivity {

    String dateDuJour;
    String dateFichier;
    String nomChemin;
    String nomFichier;
    String track, fullTrack;
    Button okUn;
    EditText leNom;
    TextView fileName;
    Button ouiUn;
    Button ouiDeux;
    Button ouiTrois;
    Button ouiQuatre;
    boolean isFileReady;
    Uri uriMonFichier, uriReadDB;
    private static File mFile = null;
    UnChemin unChemin, cheminLecture;
    ModelChemin modelChemin = null;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    Intent locationIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actonchemin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences mesPrefs =  MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor editeur = mesPrefs.edit();

        dateDuJour = mesPrefs.getString("dateJour", "2000-01-01");
        dateFichier = mesPrefs.getString("dateFichier", "000101");
        isFileReady = mesPrefs.getBoolean("fileReady", false);
        track = mesPrefs.getString("leChemin", "");

        modelChemin = new ViewModelProvider(this).get(ModelChemin.class);

        leNom = findViewById(R.id.editTextTextPersonName);
        okUn = findViewById(R.id.button);
        fileName = findViewById(R.id.textView3);
        ouiDeux = findViewById(R.id.button3);
        ouiTrois = findViewById(R.id.button4);
        ouiQuatre = findViewById(R.id.button5);


// au clic, saisir le nom du chemin, fabriquer le nom du fichier GPX à partir du nom et de la date, fabriquer
// le contenu du fichier à partir de la suite de trkpt, du nom et de la date et enregistrer le fichier
// dans la zone publique de l'appli
        okUn.setOnClickListener(view -> {
// faire nom du fichier à partir de la saisie
            if (TextUtils.isEmpty(leNom.getText())) {
                Toast.makeText(this, "Donner un nom", Toast.LENGTH_SHORT).show();
            }else {
                nomChemin = leNom.getText().toString();
                nomFichier = dateFichier + "-" + removeAllAccents(nomChemin).replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase() + ".gpx";
                fileName.setText(nomFichier);
// créee le chemin du fichier
                File path = getPrivateDocStorageDir(this, "MesChemins");
                mFile = new File(path, nomFichier);
                Log.i("APPCHEMINS", "chemin du fichier = " + mFile);
// fabriquer le contenu du fichier
                fullTrack = faitGPXChemin(nomChemin, track);
// écrire le fichier et calculer son Uri
                if(isExternalStorageWritable()) {
                    ecrireFichier(fullTrack, mFile);
                }else{
                    Toast.makeText(this, "On ne peut pas écrire", Toast.LENGTH_SHORT).show();
                }
                uriMonFichier = MyHelper.getInstance().recupURI(mFile);

                editeur.putString("nomFichier", nomFichier);
                editeur.putString("uriMomFichier", uriMonFichier.toString());
                editeur.apply();
                if (BuildConfig.DEBUG) {
                    Log.i("APPCHEMINS", "Uri = " + uriMonFichier);
                }
                unChemin = new UnChemin(dateDuJour, nomChemin, nomFichier, uriMonFichier);
                modelChemin.insertChemin(unChemin);

// re lecture pour vérifier
/*                try {
                    String lecture = lireFichier(mFile);
                    if (BuildConfig.DEBUG) {
                        Log.i("APPCHEMINS", "fichier fut lu = " + lecture);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }  */
            }
        });  // end ok1 click listener


// bouton envoyer par email
        ouiDeux.setOnClickListener(view-> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"claude.pastre@free.fr"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "envoi fichier");
            intent.putExtra(Intent.EXTRA_STREAM, uriMonFichier);
            intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "intent = "+intent);}
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

// ouvrir dans une appli de cartographie
        ouiTrois.setOnClickListener(view-> {
            Intent cartoIntent = new Intent(Intent.ACTION_VIEW, uriMonFichier);
            cartoIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "cartointent = "+cartoIntent);}
            if (cartoIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(cartoIntent);
            }
        });

/*  En attente d'une carte affichée
        final Observer<UnChemin> cheminObserver = new Observer<UnChemin>() {
            @Override
            public void onChanged(UnChemin chemin) {
                int rowid = chemin.getRowid();
                String nomDuChemin = chemin.getNomchemin();
                String nomDuFichier = chemin.getNomfichier();
                Uri uriDuChemin = chemin.getUrifichier();
                try {
                    String laTrace = lireChemin(uriDuChemin);
                    if (!"".equals(laTrace)){
                        if (BuildConfig.DEBUG){
                            Log.i("APPCHEMINS", "trace DB "+laTrace+", "+nomDuChemin+", "+nomDuFichier);}
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        modelChemin.getMonChemin().observe(this, cheminObserver); */

    }       //end de onCreate()

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.help) {
            Intent lireAide = new Intent(this, Aide.class);
            startActivity(lireAide);
            return true;
        }
        if (id == R.id.archive) {
            Intent archives = new Intent(this, Archives.class);
            startActivity(archives);
            return true;
        }
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, KParameters.class);
            startActivity(settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
