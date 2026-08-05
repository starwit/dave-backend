package de.muenchen.dave.services;

import de.muenchen.dave.domain.ConfigurationEntity;
import de.muenchen.dave.domain.dtos.init.ConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.MapConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.TenantConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.ZaehlstelleConfigurationDTO;
import de.muenchen.dave.domain.enums.ConfigDataTypes;
import de.muenchen.dave.repositories.relationaldb.ConfigurationRepository;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
public class ConfigurationService {

    private final ConfigurationRepository repository;

    private final ConfigurationDTO configuration;

    public ConfigurationService(
            final ConfigurationRepository repository,
            @Value("${dave.tenant.map.center.lat:48.137227}") final String lat,
            @Value("${dave.tenant.map.center.lng:11.575517}") final String lng,
            @Value("${dave.tenant.map.center.zoom:12}") final Integer zoom,
            @Value("${dave.zaehlstelle.automatic-number-assignment:true}") final boolean zaehlstelleAutomaticNumberAssignment,
            @Value("${dave.tenant.datenportal-header:Datenportal}") final String datenportalHeader,
            @Value("${dave.tenant.city:München}") final String city,
            @Value("${dave.zaehlstelle.link-documentation-csv-file-for-upload-zaehlung}") final String linkDocumentationCsvFileForUploadZaehlung) {
        final var zaehlstelleConfig = new ZaehlstelleConfigurationDTO(
                zaehlstelleAutomaticNumberAssignment,
                linkDocumentationCsvFileForUploadZaehlung);
        final var mapConfiguration = new MapConfigurationDTO(lat, lng, zoom);
        final var tenantConfiguration = new TenantConfigurationDTO(datenportalHeader, mapConfiguration);
        this.configuration = new ConfigurationDTO(zaehlstelleConfig, tenantConfiguration, city);
        this.repository = repository;
    }

    public ConfigurationDTO getConfiguration() {
        String latitude = configuration.getTenant().getMapConfiguration().getLat();
        String longitude = configuration.getTenant().getMapConfiguration().getLng();
        int zoom = configuration.getTenant().getMapConfiguration().getZoom();
        boolean zaehlstelleAutomaticNumberAssignment = configuration.getZaehlstelle().isAutomaticNumberAssignment();
        String linkDocumentationCsvFileForUploadZaehlung = configuration.getZaehlstelle().getLinkDocumentationCsvFileForUploadZaehlung();
        String city = configuration.getCity();

        for (ConfigurationEntity ce : repository.findAll()) {
            if ("city".equals(ce.getKeyname())) {
                city = ce.getValuefield();
            }
            if ("location_lat".equals(ce.getKeyname())) {
                Double.parseDouble(ce.getValuefield());
                latitude = ce.getValuefield();
            }
            if ("location_lon".equals(ce.getKeyname())) {
                Double.parseDouble(ce.getValuefield());
                longitude = ce.getValuefield();
            }
            if ("zoom".equals(ce.getKeyname())) {
                zoom = Integer.parseInt(ce.getValuefield());
            }
            if ("zaehlstelleAutomaticNumberAssignment".equals(ce.getKeyname())) {
                zaehlstelleAutomaticNumberAssignment = Boolean.parseBoolean(ce.getValuefield());
            }
            if ("linkDocumentationCsvFileForUploadZaehlung".equals(ce.getKeyname())) {
                linkDocumentationCsvFileForUploadZaehlung = ce.getValuefield();
            }
        }
        ZaehlstelleConfigurationDTO zaehlstelleConfig = new ZaehlstelleConfigurationDTO(
                zaehlstelleAutomaticNumberAssignment,
                linkDocumentationCsvFileForUploadZaehlung);
        configuration.setZaehlstelle(zaehlstelleConfig);
        MapConfigurationDTO mapConfiguration = new MapConfigurationDTO("" + latitude, "" + longitude, zoom);
        configuration.getTenant().setMapConfiguration(mapConfiguration);
        configuration.setCity(city);
        return configuration;
    }

    public List<ConfigurationEntity> findAll() {
        return repository.findAll();
    }

    public ConfigurationEntity findByKeyname(String keyname) {
        return repository.findByKeyname(keyname);
    }

    public String getConfiguredCity() {
        ConfigurationEntity cityConfig = findByKeyname("city");
        if (cityConfig == null || cityConfig.getValuefield().isEmpty()) {
            log.warn("City not found in configuration, defaulting to 'München'");
            return "München";
        }
        return cityConfig.getValuefield();
    }

    public List<ConfigurationEntity> saveOrUpdateList(List<ConfigurationEntity> configs) throws IllegalArgumentException {
        configs.forEach(this::testTypeCorrectness);
        return repository.saveAll(configs);
    }

    public ConfigurationEntity saveOrUpdate(ConfigurationEntity config) throws IllegalArgumentException {
        testTypeCorrectness(config);

        ConfigurationEntity existingConfig = repository.findByKeyname(config.getKeyname());
        if (existingConfig != null) {
            existingConfig.setValuefield(config.getValuefield());
            existingConfig.setCategory(config.getCategory());
            existingConfig.setDatatype(config.getDatatype());
            return repository.save(existingConfig);
        } else {
            return repository.save(config);
        }
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    private void testTypeCorrectness(ConfigurationEntity config) throws IllegalArgumentException {
        ConfigDataTypes type = config.getDatatype();
        if (type == null) {
            throw new IllegalArgumentException("No type information found for: " + config.toString());
        }
        switch (type) {
        case INTEGER:
            try {
                Integer.parseInt(config.getValuefield());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Valuefield for key " + config.getKeyname() + " is not a valid INTEGER.");
            }
            break;
        case DOUBLE:
            try {
                Double.parseDouble(config.getValuefield());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Valuefield for key " + config.getKeyname() + " is not a valid DOUBLE.");
            }
            break;

        case BOOLEAN:
            if (!"true".equalsIgnoreCase(config.getValuefield())
                    && !"false".equalsIgnoreCase(config.getValuefield())) {
                throw new IllegalArgumentException(
                        "Valuefield for key " + config.getKeyname() + " is not a valid BOOLEAN.");
            }
            break;
        default:
            // String requires no special handling
            break;
        }
    }
}
