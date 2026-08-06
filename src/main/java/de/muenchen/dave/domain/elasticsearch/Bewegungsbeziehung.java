package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import lombok.Data;
import org.springframework.data.annotation.Transient;

@Data
public abstract class Bewegungsbeziehung implements Serializable {

    String id;

    @Transient
    Long version;

}
