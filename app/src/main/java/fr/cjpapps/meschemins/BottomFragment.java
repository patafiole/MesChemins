package fr.cjpapps.meschemins;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;

import io.reactivex.rxjava3.annotations.Nullable;

public class BottomFragment extends BottomSheetDialogFragment {

    Button start = null;
    Button pause = null;
    Button resume = null;
    Button save = null;
    Button cancel = null;
    SharedPreferences mesPrefs;
    ModelLocation model;

    public static BottomFragment getInstance() {
        return new BottomFragment();
    }

@Nullable
@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mesPrefs =  MyHelper.getInstance().recupPrefs();
        model = new ViewModelProvider(requireActivity()).get(ModelLocation.class);
        View view =inflater.inflate(R.layout.modal_bottom_sheet, container, false);
        start = view.findViewById(R.id.bouton_start);
        pause = view.findViewById(R.id.bouton_pause);
        resume = view.findViewById(R.id.bouton_resume);
        save = view.findViewById(R.id.bouton_save);
        cancel = view.findViewById(R.id.bouton_cancel);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        SharedPreferences.Editor editeur = mesPrefs.edit();

        start.setOnClickListener(view1 -> {
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "bouton start");}
            editeur.putString("leChemin", "");
            editeur.commit();
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "4 updates request "+Variables.requestingLocationUpdates);}
            if(Variables.requestingLocationUpdates) {
                model.updatePosition();
            }else{
                Toast.makeText(requireActivity(), "Pas de localisation possible", Toast.LENGTH_LONG).show();
            }
            BottomFragment.this.dismiss();
        });

        pause.setOnClickListener(view1 -> {
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "bouton pause");}
            model.stopUpdatePosition();
            BottomFragment.this.dismiss();
        });

        resume.setOnClickListener(view1 -> {
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "bouton resume");}
            if (Variables.requestingLocationUpdates) {
                String track = mesPrefs.getString("leChemin", "");
                editeur.putString("leChemin", track + Aux.GPXD);
                editeur.apply();
                model.updatePosition();
            }else{
                Toast.makeText(requireActivity(), "Pas de localisation possible", Toast.LENGTH_LONG).show();
            }
            BottomFragment.this.dismiss();
        });

        save.setOnClickListener(view1 -> {
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "bouton save");}
            model.stopUpdatePosition();
            Intent finChemin = new Intent(requireActivity(), ActOnChemin.class);
            startActivity(finChemin);
            BottomFragment.this.dismiss();
        });

        cancel.setOnClickListener(view1 -> {
            if (BuildConfig.DEBUG){
                Log.i("APPCHEMINS", "bouton cancel");}
            model.stopUpdatePosition();
            HashMap<String, String> paramsPosition = new HashMap<>();
            paramsPosition.put("heure", getString(R.string.real_time));
            paramsPosition.put("lat", "");
            paramsPosition.put("lon", "");
            paramsPosition.put("altitude", getString(R.string.alti));
            paramsPosition.put("precision", getString(R.string.accuracy));
            model.getTableauPosition().setValue(paramsPosition);
            editeur.putString("leChemin", "");
            editeur.apply();
            BottomFragment.this.dismiss();
        });
    }

}
