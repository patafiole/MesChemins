package fr.cjpapps.meschemins;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;

import java.io.File;
import java.sql.Blob;
import java.util.Date;

@Fts4
@Entity(tableName = "meschemins")
public class UnChemin {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    public int rowid;

    @ColumnInfo
    private String datejour;

    @ColumnInfo
    private String nomchemin;

    @ColumnInfo
    private String nomfichier;

    @ColumnInfo
    private Uri urifichier;

//    public UnChemin() {}

   public UnChemin(String datejour, String nomchemin, String nomfichier, Uri urifichier) {
        this.datejour = datejour;
        this.nomchemin = nomchemin;
        this.nomfichier = nomfichier;
        this.urifichier = urifichier;
    }

    public int getRowid (){
        return rowid;
    }

    public void setRowid(int id) {
        this.rowid = id;
    }

    public String getDatejour() {
        return datejour;
    }

    public void setDatejour(String datejour) {
        this.datejour = datejour;
    }

    public String getNomchemin() {
        return nomchemin;
    }

    public void setNomchemin(String nomchemin) {
        this.nomchemin = nomchemin;
    }

    public String getNomfichier() {
        return nomfichier;
    }

    public void setNomfichier(String nomfichier) {
        this.nomfichier = nomfichier;
    }

    public Uri getUrifichier() {
        return urifichier;
    }

    public void setUrifichier(Uri urifichier) {
        this.urifichier = urifichier;
    }
}
