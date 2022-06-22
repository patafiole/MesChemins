package fr.cjpapps.meschemins;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

public class Archives extends AppCompatActivity {

    TextView nomDernier;
    Button litDernier, triDates, triNoms ;
    ImageButton versIphi, versEmail;
    Uri uriDernier ;
    ModelChemin modelMyWay;
    UnChemin leChemin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archives);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences mesPrefs = MyHelper.getInstance().recupPrefs();
        SharedPreferences.Editor editeur = mesPrefs.edit();

        nomDernier = findViewById(R.id.dernier_trajet);
        litDernier = findViewById(R.id.button5);
        versIphi = findViewById(R.id.ifi_button);
        versEmail = findViewById(R.id.mel_button);
        triDates = findViewById(R.id.button6);
        triNoms = findViewById(R.id.button7);

        modelMyWay = new ViewModelProvider(this).get(ModelChemin.class);

// lecture d dernier trajet enregistré ; après quoi on peut le visualiser dans iphigénie ou l'envoyer en pièce jinte à un email
        litDernier.setOnClickListener(view-> {
            modelMyWay.getMonChemin().observe(this, chemin -> {
                int rowid = chemin.getRowid();
                String dateDuJour = chemin.getDatejour();
                String nomDuChemin = chemin.getNomchemin();
                String nomDuFichier = chemin.getNomfichier();
                uriDernier = chemin.getUrifichier();
                if (BuildConfig.DEBUG) {
                    Log.i("APPCHEMINS", "champs " + rowid + ", " + dateDuJour +", "+ nomDuChemin + ", " + nomDuFichier +", "+ uriDernier);
                }
 // pour vérifier (à enlever par la suite)
                try {
                    String laTrace = Aux.lireChemin(uriDernier);
                    if (!"".equals(laTrace)) {
                        if (BuildConfig.DEBUG) {
                            Log.i("APPCHEMINS", "trace DB " + rowid + ", " + laTrace + ", " + nomDuChemin + ", " + nomDuFichier);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }  // fin de vérif
                nomDernier.setText(nomDuFichier);
                editeur.putString("uri_dernier", uriDernier.toString());
                editeur.apply();
            });
        });

        versEmail.setOnClickListener( view -> {
            uriDernier = Uri.parse(mesPrefs.getString("uri_dernier", ""));
            sendByEmail(uriDernier);
        });

        versIphi.setOnClickListener( view -> {
            uriDernier = Uri.parse(mesPrefs.getString("uri_dernier", ""));
            sendToAppliCarto(uriDernier);
        });

// affichage de la liste des trajets archivés triés par dates
        triDates.setOnClickListener( view -> {
            String titre = "Liste par dates";
            FragmentManager fm = getSupportFragmentManager();
            FragmentListe listeFrag = FragmentListe.newInstance(titre);
            listeFrag.show(fm, "listeDates");
        });

// affichage de la liste des trajets archivés triée par noms
        triNoms.setOnClickListener( view -> {
            String titre = "Liste par nom";
            FragmentManager fm = getSupportFragmentManager();
            FragmentListe listeFrag = FragmentListe.newInstance(titre);
            listeFrag.show(fm, "listeNoms");
        });

    }  // end onCreate

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
            Intent lireAide = new Intent(this, Aide.class);
            startActivity(lireAide);
            return true;
        }
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, Preferences.class);
            startActivity(settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    void sendByEmail(Uri uriMonFichier){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"claude.pastre@free.fr"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "envoi fichier");
        intent.putExtra(Intent.EXTRA_STREAM, uriMonFichier);
        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "intent = "+intent);}
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);}
    }

    void sendToAppliCarto(Uri uriMonFichier) {
        Intent cartoIntent = new Intent(Intent.ACTION_VIEW, uriMonFichier);
        cartoIntent.setPackage("com.iphigenie");
        cartoIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "cartointent = "+cartoIntent);}
        if (cartoIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(cartoIntent);
        } else {
            Toast.makeText(this, "Appli de carte topo non disponible", Toast.LENGTH_LONG).show();
        }
    }

}
