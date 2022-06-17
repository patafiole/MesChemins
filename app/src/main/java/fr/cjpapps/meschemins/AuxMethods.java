package fr.cjpapps.meschemins;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AuxMethods {

    String lireChemin(Uri dbFile) throws IOException {
        String result = "";
        InputStream inputStream = MyHelper.getInstance().resolver().openInputStream(dbFile);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String temp = "";
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

}
