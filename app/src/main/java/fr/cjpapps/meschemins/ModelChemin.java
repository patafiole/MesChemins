package fr.cjpapps.meschemins;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ModelChemin extends AndroidViewModel {

    private final LiveData<UnChemin> monChemin;
    private final LiveData<List<UnChemin>> desChemins;
    private final LiveData<List<UnChemin>> desNomsChemins;
//    private final LiveData<List<UnChemin>> searchResults;
    private final CheminsRepository repository;
    private UnChemin mChemin;
//    private final String mSearchQuery;

    public ModelChemin(Application application) {
            super (application);
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "constructeur ModelChemin");}
        repository = new CheminsRepository(application);
//        mSearchQuery = searchQuery;
        monChemin = repository.getDernierChemin();
        desChemins = repository.getAllChemins();
        desNomsChemins = repository.getAllParNom();
//        searchResults = repository.getSearchResult(searchQuery);
    }

    LiveData<UnChemin> getMonChemin() {return monChemin;}
    LiveData<List<UnChemin>> getDesChemins() {return desChemins;}
    LiveData<List<UnChemin>> getDesNomsChemins() {return desNomsChemins;}
    LiveData<List<UnChemin>> getSearchResults(String searchQuery) {return repository.getSearchResult(searchQuery);}

    public void insertChemin(UnChemin chemin){
        repository.insert(chemin);
        if (BuildConfig.DEBUG){
            Log.i("APPCHEMINS", "ModelChemins, insertion");}
    }

    public void deleteChemin(UnChemin chemin){
        repository.delete(chemin);
    }

}
