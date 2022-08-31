package fr.cjpapps.meschemins;

public class Constantes {

    static final String ACTION_GETLOCATION = "fr.cjpapps.meschemins.action.GETLOCATION";
    static final String EXTRA_LOCATION = "fr.cjpapps.meschemins.EXTRA_LOCATION";

/* géoïde en mètres sur une grille de pas 4° (sur latitude et sur longitude), allant de 41 N à 53 N et de 66 à 10
/* disposition des coins   (41N, -6)  (41N, 10)
/*                         (53N, -6)  (53N, 10)
/* extrait de ICGEM(http://icgem.gfz-potsdam.de/home) calcul sur le modèle EGM2008  */
    static final float[][] geoide = {
        {56.6f, 53.9f, 49.0f, 45.5f, 47.1f},
        {45.4f, 47.9f, 50.8f, 54.0f, 39.8f},
        {53.3f, 49.6f, 45.1f, 47.9f, 48.7f},
        {56.7f, 51.7f, 44.7f, 42.3f, 41.4f}
    };

}
