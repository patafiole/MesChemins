package fr.cjpapps.meschemins;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;

public class Aux {

    static final private String GPXA ="<?xml version=\"1.0\"?><gpx><trk><name>";
    static final private String GPXB ="</name><trkseg>";
    static final private String GPXC ="</trkseg></trk></gpx>";
    static final String GPXD = "</trkseg><trkseg>";

    static String faitGPXChemin(String name, String track){
        String cheminGPX = GPXA+name+GPXB+track+GPXC;
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "2 chemin.gpx = "+cheminGPX);}
        return cheminGPX;
    }

    public static String removeAllAccents(@NonNull String s) {
        String result = Normalizer.normalize(s, Normalizer.Form.NFD);
        result = result.replaceAll("[^\\p{ASCII}]", "");
        return result;
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

    /* Checks if external storage is available for read and write */
    static public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    static void ecrireFichier(String fullTrack, File file){
        try{
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(fullTrack.getBytes());
            stream.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "fichier écrit");}
    }

    static String lireFichier(File file) throws IOException {
        FileInputStream input;
        int value;
        StringBuilder lu = new StringBuilder();
        try {
            if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                input = new FileInputStream(file);
                while ((value =input.read()) != -1) lu.append((char)value);
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lu.toString();
    }

    static String lireChemin(Uri dbFile) throws IOException {
        String result;
        InputStream inputStream = MyHelper.getInstance().resolver().openInputStream(dbFile);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String temp;
        StringBuilder stringBuilder = new StringBuilder();
        while((temp = bufferedReader.readLine()) != null)
        {
            stringBuilder.append(temp);
            stringBuilder.append("\n");
        }
        inputStream.close();
        result = stringBuilder.toString();
        return result;
    }

    static float getGeoidValue(double lat, double lon) {
//grille entre latitudes 41 et 53    , longitudes -6 à 10 . Valeur moyenne sur le grille 48.6 m ; voir Constantes
      /*  if (Double.compare(lat, 41.0d) < 0 || Double.compare(lat, 53.0d) > 0 || Double.compare(lon, -6.0d) < 0 ||
                Double.compare(lon, 10.0d) > 0) { return 48.6f;}  */
            int i = (int) ((lat - 41.0) / 4.0);
            float fractI = (float) (((lat - 41.0) % 4) / 4.0);
            float complI = (float) (1.0 - fractI);
            int j = (int) ((lon + 6.0) / 4.0);
            float fractJ = (float) (((lon + 6.0) % 4) / 4.0);
            float complJ = (float) (1.0 - fractJ);
            return (complI * complJ * Constantes.geoide[i][j] + fractI * complJ * Constantes.geoide[i + 1][j] +
                    fractJ * complI * Constantes.geoide[i][j + 1] + fractI * fractJ * Constantes.geoide[i + 1][j + 1]);
    }

}
