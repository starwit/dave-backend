package de.muenchen.dave.domain.analytics;

import de.muenchen.dave.domain.BaseEntity;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Laengsverkehr extends BaseEntity {

    @Column(name = "richtung")
    @Enumerated(EnumType.STRING)
    private Bewegungsrichtung richtung;

    @Column(name = "strassenseite")
    @Enumerated(EnumType.STRING)
    private Himmelsrichtung strassenseite;

    @Column(name = "knotenarm")
    private Integer knotenarm;

    @ManyToOne
    @JoinColumn(name = "zaehlung", referencedColumnName = "id")
    private Zaehlung zaehlung;

}
