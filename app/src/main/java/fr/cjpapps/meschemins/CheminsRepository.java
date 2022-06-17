package fr.cjpapps.meschemins;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CheminsRepository {

    private CheminsDao cheminsDao;
    private LiveData<UnChemin> dernierChemin;
    private LiveData<List<UnChemin>> allChemins;
    private LiveData<List<UnChemin>> allNomsChemins;

    CheminsRepository (Application application){
        CheminsRoomDatabase db = CheminsRoomDatabase.getDatabase(application);
        cheminsDao = db.cheminsDao();
        dernierChemin = cheminsDao.getDernierChemin();
        allChemins = cheminsDao.getAllChemins();
        allNomsChemins = cheminsDao.getAllNomsChemins();
    }

    LiveData<UnChemin> getDernierChemin(){
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "lu par repo = "+dernierChemin.toString());}
        return dernierChemin;}

    LiveData<List<UnChemin>> getAllChemins() {return allChemins;}

    LiveData<List<UnChemin>> getAllParNom() {return allNomsChemins;}

    void insert(UnChemin chemin) {
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "inséré par repo = "+chemin.getNomchemin());}
        CheminsRoomDatabase.databaseExecutor.execute(() -> {
            cheminsDao.insert(chemin);
        });
    }

    void delete(UnChemin chemin) {
        CheminsRoomDatabase.databaseExecutor.execute(() -> {
            cheminsDao.delete(chemin);
        });
    }
}
