package de.muenchen.dave.domain.analytics;

import de.muenchen.dave.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ZaehlstelleImage extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "imagedata")
    private byte[] data;

    @Column(name = "zaehlstelle_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID zaehlstelleId;

    @Column(name = "contenttype")
    private String contentType;
}
