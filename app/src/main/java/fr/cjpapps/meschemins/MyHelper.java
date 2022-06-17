package fr.cjpapps.meschemins;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static fr.cjpapps.meschemins.AuxGPX.getPrivateDocStorageDir;
import static fr.cjpapps.meschemins.MainActivity.PREF_FILE;

class MyHelper {

    /* voir https://www.fwd.cloud/commit/post/android-context-on-demand/
     * MyHelper permet de récupérer le contexte de l'application même depuis une méthode statique de manière
     * paraît-il pas caca. Mais j'arrivais pas à m'en servir parce qu'il fallait passer un contexte auquel
     * je n'avais précisément pas accès depuis une méthode statique. Sauvé par idée de
     * http://brainwashinc.com/2017/08/25/androidjava-sharedpreferences-anywhere-app/
     * rajouter un constructeur d'instance par défaut qu'on peut appeler sans context. Il servira alors à accéder à
     * l'instance qu'on aura préalablement créée à partir d'un endroit où on a accès à ApplicationContext (ici dans
     * le constructeur du ViewModel).
     */

    private static MyHelper instance;
    private final Context mContext;

    private MyHelper(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    // création instance avec paramètre de contexte
    static MyHelper getInstance(@NonNull Context context) {
        synchronized(MyHelper.class) {
            if (instance == null) {
                instance = new MyHelper(context);
            }
            return instance;
        }
    }

    // instance sans paramètre donne accès à l'instance préalablement créée
    static MyHelper getInstance() {
        return instance;
    }

    // méthode pour accéder aux SharedPréférences
    SharedPreferences recupPrefs(){
        return mContext.getSharedPreferences("mesInfos", MODE_PRIVATE);
    }

    // méthodepour accéder aux ressources
    Resources recupResources() {
        return mContext.getResources();
    }

    // méthode pour accéder au private doc storage dir
    File recupStorageDir() { return getPrivateDocStorageDir(mContext, "MesChemins");}

    // méthode pour créer l'Uri avec le FileProvider
    Uri recupURI(File fichier) {
        return FileProvider.getUriForFile(mContext,"fr.cjpapps.meschemins.fileprovider", fichier);
    }

    // fabrication d'un gestionnaire de connexion
    ConnectivityManager conMan() {
        return (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
    }

    // un contentresolver
    ContentResolver resolver() {
        return (ContentResolver) mContext.getContentResolver();
    }

    // et un packagemanager
    PackageManager packManager() {
        return (PackageManager) mContext.getPackageManager();
    }

}
