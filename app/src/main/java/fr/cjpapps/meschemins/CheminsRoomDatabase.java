package fr.cjpapps.meschemins;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {UnChemin.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class CheminsRoomDatabase extends RoomDatabase {

    public abstract CheminsDao cheminsDao();
    private static final String DB_NAME = "meschemins";
    private static volatile CheminsRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static CheminsRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CheminsRoomDatabase.class) {
                if (INSTANCE ==null) {
                    INSTANCE =
                            Room.databaseBuilder(context.getApplicationContext(),
                                    CheminsRoomDatabase.class, DB_NAME). build();
                }
            }
        }
        return INSTANCE;
    }
}
