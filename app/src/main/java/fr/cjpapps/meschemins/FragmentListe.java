package fr.cjpapps.meschemins;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import static fr.cjpapps.meschemins.Aux.getPrivateDocStorageDir;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentListe#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentListe extends DialogFragment {

    /* Pour afficher la liste des chemins archivés */

    private static final String ARG_PARAM1 = "titre";
    private String title;
    private static ModelChemin model;
    private TextView titreListe;
    private RecyclerView mRecyclerView;
    private RecyclerViewClickListener listener;

    public FragmentListe() {
        // Required empty public constructor
    }

    public static FragmentListe newInstance(String titre) {
        FragmentListe fragment = new FragmentListe();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, titre);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_PARAM1);
            model = new ViewModelProvider(requireActivity()).get(ModelChemin.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_liste, container, false);
        titreListe = v.findViewById(R.id.nom_liste);
        titreListe.setText(title);
        mRecyclerView = v.findViewById(R.id.listechemins);
        return v;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getArguments() != null;
        title = getArguments().getString(ARG_PARAM1, "liste");
        titreListe.setText(title);
// observateur pour recueillir la liste des chemins
        final Observer<List<UnChemin>> listeObserver = chemins -> {
            if (chemins != null) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
// click listener sur les éléments de la recycler view
                listener = (view1, position) -> {
                    UnChemin unC = chemins.get(position);
                    Uri uriFichier = unC.getUrifichier();
        // pour voir sur Iphigenie
                    if(view1.getId() == R.id.iphi_button){
                        Intent cartoIntent = new Intent(Intent.ACTION_VIEW, uriFichier);
                        cartoIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                        if (BuildConfig.DEBUG){
                            Log.i("APPCHEMINS", "cartointent = "+cartoIntent);}
                        if (cartoIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                            startActivity(cartoIntent);
                        }
                    }
        // pour envoyer par email
                    if (view1.getId() == R.id.email_button) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"claude.pastre@free.fr"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "envoi fichier");
                        intent.putExtra(Intent.EXTRA_STREAM, uriFichier);
                        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                        if (BuildConfig.DEBUG){
                            Log.i("APPCHEMINS", "intent = "+intent);}
                        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
        // pour supprimer ce chemin de l'archive
                    if (view1.getId() == R.id.delete_button) {
                        model.deleteChemin(unC);
                        String nom = unC.getNomfichier();
                        File path = getPrivateDocStorageDir(requireActivity(), "MesChemins");
                        File file = new File(path, nom);
//                        File file = new File(uriFichier.getPath());
                        if (file.delete()) {
                            Log.i("APPCHEMINS", "fichier supprimé "+file);
                        }else{
                            Log.i("APPCHEMINS", "fichier pas supprimé "+file);
                        }
                    }
                };   // end listener
                ArchivesAdapter mAdapter = new ArchivesAdapter(getActivity(), chemins, listener);
                mRecyclerView.setAdapter(mAdapter);
            }
        };   // end observer

        if ("Liste par noms".equals(title)) {
            model.getDesNomsChemins().observe(getViewLifecycleOwner(), listeObserver);
        }else{
            model.getDesChemins().observe(getViewLifecycleOwner(), listeObserver);
        }
    }
}