package de.muenchen.dave.domain.dtos.bearbeiten;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BearbeiteQuerungsverkehrDTO extends BearbeiteBewegungsbeziehungDTO {

    @Transient
    Long version;

    private Integer knotenarm;

    private Himmelsrichtung richtung;

}
