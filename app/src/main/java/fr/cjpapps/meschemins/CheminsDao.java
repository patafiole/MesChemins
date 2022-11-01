package fr.cjpapps.meschemins;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface CheminsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UnChemin chemin);

    @Delete
    void delete(UnChemin chemin);

    @Query("SELECT `rowid`,*  FROM meschemins ORDER BY datejour DESC")
    LiveData<List<UnChemin>> getAllChemins();

    @Query("SELECT `rowid`,*  FROM meschemins ORDER BY nomchemin")
    LiveData<List<UnChemin>> getAllNomsChemins();

    @Query("SELECT `rowid`,*   FROM meschemins ORDER BY rowid DESC LIMIT 1 ")
    LiveData<UnChemin> getDernierChemin();

    @Query("SELECT `rowid`,* FROM meschemins WHERE (nomchemin LIKE '%' || :search_query || '%')")
    LiveData<List<UnChemin>> getPattern(String search_query);
}
