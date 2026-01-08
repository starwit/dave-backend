package de.muenchen.dave.domain.analytics;

import de.muenchen.dave.domain.BaseEntity;
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
public class Knotenarm extends BaseEntity {

    int nummer;

    String Strassenname;

    String filename;
}
