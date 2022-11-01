package fr.cjpapps.meschemins;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class CheminsRepository {

    private final CheminsDao cheminsDao;
    private final LiveData<UnChemin> dernierChemin;
    private final LiveData<List<UnChemin>> allChemins;
    private final LiveData<List<UnChemin>> allNomsChemins;
//    private final LiveData<List<UnChemin>> searchResult;
    String searchQuery;

    CheminsRepository (Application application){
        CheminsRoomDatabase db = CheminsRoomDatabase.getDatabase(application);
        cheminsDao = db.cheminsDao();
        dernierChemin = cheminsDao.getDernierChemin();
        allChemins = cheminsDao.getAllChemins();
        allNomsChemins = cheminsDao.getAllNomsChemins();
 //       searchResult = cheminsDao.getPattern(searchQuery);
    }

    LiveData<UnChemin> getDernierChemin(){
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "lu par repo = "+dernierChemin.toString());}
        return dernierChemin;}

    LiveData<List<UnChemin>> getAllChemins() {return allChemins;}

    LiveData<List<UnChemin>> getAllParNom() {return allNomsChemins;}

    LiveData<List<UnChemin>> getSearchResult(String searchQuery) {
        return cheminsDao.getPattern(searchQuery);}

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
