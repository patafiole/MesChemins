package fr.cjpapps.meschemins;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

public class Archives extends AppCompatActivity {

/* en cas de recherche, dès le traitement de l'intent (ligne 58) handleIntent puis doMySearch lancent le fragment
*  avec la query (sans passer par l'instanciation du viewModel). Cette query sera utilisée pour activer la DAO
*  à travers model puis repository sans stockage intermédiaire de la liveData (c'est Room qui s'en occupe à
*  condition que les méthodes de la DAO renvoient des LiveData. Un observateur peut alors être n'importe où).
*  Le constructeur du Model puis celui du REpository sont exécutés une fois lors de l'ouverture de Archives,
*  mais pas lorsque Archives est relancé par l'intent de la query de recherche (mode signle top) ni par le
*  constructeur du fragment qui récupère l'instance crée par l'activité Archives.
*  Au total, pas besoin de passer la query au viewModel, donc pas besoin de factory */

    TextView nomDernier;
    Button litDernier, triDates, triNoms ;
    ImageButton versIphi, versEmail;
    Uri uriDernier ;
    ModelChemin modelMyWay;
    String searchQuery = "";
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

        handleIntent(getIntent());

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
            String titreCourt ="dates";
            String query = "";
            FragmentManager fm = getSupportFragmentManager();
            FragmentListe listeFrag = FragmentListe.newInstance(titre, titreCourt, query);
            listeFrag.show(fm, "listeDates");
        });

// affichage de la liste des trajets archivés triée par noms
        triNoms.setOnClickListener( view -> {
            String titre = "Liste par noms";
            String titreCourt = "noms";
            String query = "";
            FragmentManager fm = getSupportFragmentManager();
            FragmentListe listeFrag = FragmentListe.newInstance(titre, titreCourt, query);
            listeFrag.show(fm, "listeNoms");
        });

    }  // end onCreate

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        String titre = "résultat recherche";
        String titreCourt = "search";
        FragmentManager fm = getSupportFragmentManager();
        FragmentListe listeFrag = FragmentListe.newInstance(titre, titreCourt, query);
        listeFrag.show(fm, "listeSearch");
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_archives, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_bar).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // If false, do not iconify the widget; expand it by default
        searchView.setSubmitButtonEnabled(true);
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
            Intent settings = new Intent(this, KParameters.class);
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
