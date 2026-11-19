package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZaehlstelleIndex {

    /**
     * Toleranz in Metern, innerhalb der {@link #findByLocation(double, double)} eine Zählstelle als
     * "am selben Ort" betrachtet. Ein exakter Vergleich der Koordinaten ist nicht möglich, da
     * Gleitkommazahlen beim Serialisieren (JSON) und Speichern (Elasticsearch kodiert geo_point
     * verlustbehaftet) leicht abweichen können.
     */
    double LOCATION_TOLERANCE_METERS = 5.0;

    /** Länge eines Breitengrads in Metern (Näherung, WGS84). */
    double METERS_PER_DEGREE_LAT = 111_320.0;

    /**
     * @return Toleranz in Grad Breite, entsprechend {@link #LOCATION_TOLERANCE_METERS}.
     */
    static double latToleranceDegrees() {
        return LOCATION_TOLERANCE_METERS / METERS_PER_DEGREE_LAT;
    }

    /**
     * Ein Längengrad wird zu den Polen hin kürzer (Faktor cos(lat)), daher hängt die Toleranz in Grad
     * Länge vom Breitengrad ab.
     *
     * @param lat Breitengrad, an dem die Toleranz gelten soll
     * @return Toleranz in Grad Länge, entsprechend {@link #LOCATION_TOLERANCE_METERS}.
     */
    static double lngToleranceDegrees(final double lat) {
        return LOCATION_TOLERANCE_METERS / (METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(lat)));
    }

    Zaehlung initializeZaehlung(Zaehlung zaehlung, String zaehlstelleId);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    void deleteAll();

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    void deleteAll(Iterable<? extends Zaehlstelle> var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    void deleteById(String var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    void delete(Zaehlstelle var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    Zaehlstelle save(Zaehlstelle var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    Iterable<Zaehlstelle> saveAll(Iterable<Zaehlstelle> var1);

    Optional<Zaehlstelle> findById(String var1);

    Page<Zaehlstelle> suggestSearch(String query, Pageable pageable);

    Page<Zaehlstelle> findAllByStatus(Status[] statusArray, Pageable pageable);

    List<Zaehlstelle> findAll();

    Optional<Zaehlstelle> findByZaehlungenId(String id);

    List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer);

    Optional<Zaehlstelle> findByNummer(String nummer);

    /**
     * Findet alle Zählstellen, deren Koordinate höchstens {@link #LOCATION_TOLERANCE_METERS} in
     * Nord-Süd- bzw. Ost-West-Richtung von der übergebenen Koordinate entfernt liegt (Bounding-Box).
     *
     * @param lat Breitengrad (WGS84)
     * @param lng Längengrad (WGS84)
     * @return gefundene Zählstellen, ggf. leer
     */
    List<Zaehlstelle> findByLocation(double lat, double lng);

    List<Zaehlstelle> findAllByZaehlungenStatus(String status);

    /**
     * @param prefix Anfang der Projektnummer
     * @return alle Zählstellen mit mindestens einer Zählung, deren Projektnummer so beginnt, mit allen
     *         ihren Zählungen
     */
    List<Zaehlstelle> findAllByZaehlungenProjektNummerStartsWith(String prefix);

    List<Zaehlstelle> findAllByZaehlungenJahr(String jahr);

    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();

    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesDienstleisterTrue();

}
