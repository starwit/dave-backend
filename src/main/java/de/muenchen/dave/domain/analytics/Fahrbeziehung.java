package de.muenchen.dave.domain.analytics;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.Hochrechnungsfaktor;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
// Definition of getter, setter, ...
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Fahrbeziehung extends BaseEntity {

    Boolean isKreuzung;

    // Kreuzung
    Integer von;

    Integer nach;

    // Kreisverkehr
    Integer knotenarm;

    Boolean hinein;

    Boolean heraus;

    Boolean vorbei;

    // Knoten-Kanten-Modell
    String vonknotvonstrnr;

    String nachknotvonstrnr;

    String von_strnr;

    String vonknotennachstrnr;

    String nachknotnachstrnr;

    String nach_strnr;

    Hochrechnungsfaktor hochrechnungsfaktor;

}
