package fr.cjpapps.meschemins;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AuxGPX {

    static final private String LATLON_RV = "latlon.gpx";
    static final private String LOCATION = "location";
    final private String CONTENU = "<?xml version=\"1.0\" ?><gpx><trkpt lat=\"44.9801\" lon=\"6.5030\"></trkpt></gpx>";
    static final private String GPX1 ="<?xml version=\"1.0\" ?><gpx><trk><trkseg><trkpt lat=\"";
    static final private String GPX2 ="\" lon=\"";
    static final private String GPX3 ="\"></trkpt><trkpt lat=\"";
    static final private String GPX4 ="\"></trkpt></trkseg></trk></gpx>";
    static final private String GPXA ="<?xml version=\"1.0\"?><gpx><trk><name>";
    static final private String GPXB ="</name><trkseg>";
    static final private String GPXC ="</trkseg></trk></gpx>";

    private static File mFile = null;

    static boolean faitFichierGPX() {
        if(isExternalStorageWritable()){
            File fichiers = MyHelper.getInstance().recupStorageDir();
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "0  chemin OK "+fichiers.toString());}
            mFile = new File(fichiers, LATLON_RV);
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "1  fichier créé ");}
            return true;
        }else{ return false;}
    }

    static Uri faitURI(String lat, String lon, String laP, String LoP) {
// fabrique le texte du fichier GPX
        String texteGPX = faitGPXTexte(lat,lon);
// écrit le fichier GPX dans le stockage de Documents
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {

                FileOutputStream output = new FileOutputStream(mFile);
                output.write(texteGPX.getBytes());
                output.close();
            }
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "3 fichier GPX a été écrit ");}
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
// fabrique l'URI du fichier
        return MyHelper.getInstance().recupURI(mFile);
    }

    private static String faitGPXTexte (String lat, String lon){
//        String latBis = String.valueOf(Double.parseDouble(lat)+0.0003);
//  servait à créer une trace à deux points très proches
        String texteFichier = GPX1+lat+GPX2+lon+GPX4;
 /*       if (BuildConfig.DEBUG){
//        texteFichier = "<?xml version=\"1.0\" ?><gpx><trk><trkseg><trkpt lat=\"48.44596\" lon=\"2.63768\"></trkpt><trkpt lat=\"48.447261\" lon=\"2.640046\"></trkpt></trkseg></trk></gpx>";
            Log.i("APPCHEMINS", "2 fichier GPX "+texteFichier);} */
        return texteFichier;
    }

    static String faitGPXChemin(String name, String track){
        String cheminGPX = GPXA+name+GPXB+track+GPXC;
        if (BuildConfig.DEBUG){
//        texteFichier = "<?xml version=\"1.0\" ?><gpx><trk><trkseg><trkpt lat=\"48.44596\" lon=\"2.63768\"></trkpt><trkpt lat=\"48.447261\" lon=\"2.640046\"></trkpt></trkseg></trk></gpx>";
            Log.i("APPCHEMINS", "2 chemin.gpx = "+cheminGPX);}
        return cheminGPX;
    }
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    static File getPrivateDocStorageDir(Context context, String position) {
        // Get the directory for the app's private documents directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), position);
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "chemin fichier GPX "+file.getPath());}
        if (!file.mkdir()) {
            Log.e("APPCHEMINS", "Directory pas created");
        }
        return file;
    }


}