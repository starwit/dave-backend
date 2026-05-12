package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.analytics.ZaehlstelleImage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZaehlstelleImageRepository extends JpaRepository<ZaehlstelleImage, UUID> {

    public ZaehlstelleImage findByZaehlstelleId(UUID zaehlstelleId);
}
