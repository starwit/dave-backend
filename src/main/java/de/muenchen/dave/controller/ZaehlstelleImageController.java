package de.muenchen.dave.controller;

import de.muenchen.dave.domain.analytics.Zaehlstelle;
import de.muenchen.dave.domain.analytics.ZaehlstelleImage;
import de.muenchen.dave.repositories.relationaldb.ZaehlstelleImageRepository;
import de.muenchen.dave.repositories.relationaldb.ZaehlstelleRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/zaehlstelleimage")
public class ZaehlstelleImageController {

    ZaehlstelleImageRepository zaehlstelleImageRepository;

    ZaehlstelleRepository zaehlstelleRepository;

    public ZaehlstelleImageController(ZaehlstelleImageRepository zaehlstelleImageRepository, ZaehlstelleRepository zaehlstelleRepository) {
        this.zaehlstelleImageRepository = zaehlstelleImageRepository;
        this.zaehlstelleRepository = zaehlstelleRepository;
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ZaehlstelleImage>> getAllZaehlstelleImages() {
        log.info("GET request for all ZaehlstelleImages");
        List<ZaehlstelleImage> zaehlstelleImages = zaehlstelleImageRepository.findAll();
        return ResponseEntity.ok(zaehlstelleImages);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZaehlstelleImage> getZaehlstelleImageByZaehlstelleId(@PathVariable("id") UUID zaehlstelleId) {
        log.info("GET request for ZaehlstelleImage with Zaehlstelle ID: {}", zaehlstelleId);
        ZaehlstelleImage zaehlstelleImageOpt = zaehlstelleImageRepository.findByZaehlstelleId(zaehlstelleId);
        if (zaehlstelleImageOpt != null) {
            return ResponseEntity.ok(zaehlstelleImageOpt);
        } else {
            log.warn("No ZaehlstelleImage found for Zaehlstelle ID: {}", zaehlstelleId);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<ZaehlstelleImage> updateZaehlstelleImage(@RequestBody ZaehlstelleImage zaehlstelleImage) {
        log.info("PUT request to create ZaehlstelleImage for Zaehlstelle ID: {}", zaehlstelleImage.getZaehlstelleId());
        ZaehlstelleImage savedZaehlstelleImage = zaehlstelleImageRepository.save(zaehlstelleImage);
        return ResponseEntity.ok(savedZaehlstelleImage);
    }

    @PostMapping(value = "/{id}")
    public ResponseEntity<ZaehlstelleImage> createZaehlstelleImage(@RequestParam("image") MultipartFile file, @PathVariable("id") UUID id) {
        log.info("POST request to create ZaehlstelleImage for Zaehlstelle ID: {}", id);
        Optional<Zaehlstelle> zaehlstelle = zaehlstelleRepository.findById(id);

        // test if there is already an image for the given Zaehlstelle, if yes delete it and create a new one
        ZaehlstelleImage existingImage = zaehlstelleImageRepository.findByZaehlstelleId(id);
        if (existingImage != null) {
            log.info("Existing ZaehlstelleImage found for Zaehlstelle ID: {}, deleting it before creating a new one", id);
            zaehlstelleImageRepository.delete(existingImage);
        }

        if (zaehlstelle.isPresent()) {
            var zaehlstelleImage = uploadImage(file, zaehlstelle.get());
            if (zaehlstelleImage != null) {
                log.info("Successfully updated ZaehlstelleImage for Zaehlstelle ID: {}", id);
                return ResponseEntity.ok(zaehlstelleImage);
            } else {
                log.warn("Failed to update ZaehlstelleImage for Zaehlstelle ID: {}", id);
                return ResponseEntity.internalServerError().build();
            }

        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/as-file/{id}")
    public ResponseEntity<Resource> getImageWithIdAsFile(@PathVariable("id") UUID id) {
        ZaehlstelleImage image = zaehlstelleImageRepository.findByZaehlstelleId(id);
        if (image == null) {
            log.warn("No ZaehlstelleImage found for Zaehlstelle ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        var imageData = new ByteArrayResource(image.getData());

        if (imageData.contentLength() == 0) {
            log.warn("ZaehlstelleImage for Zaehlstelle ID: {} has no data", id);
            return ResponseEntity.noContent().build();
        }

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + image.getName());
        header.add("Cache-Control", "max-age=3600, must-revalidate");
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(image.getData().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageData);
    }

    private ZaehlstelleImage uploadImage(MultipartFile imageFile, Zaehlstelle zaehlstelle) {
        ZaehlstelleImage imageEntity = new ZaehlstelleImage();
        imageEntity.setName(zaehlstelle.getNummer() + "_" + zaehlstelle.getStadtbezirk() + "_" + zaehlstelle.getKommentar());
        imageEntity.setContentType(imageFile.getContentType());

        byte[] imageData;
        try {
            imageData = imageFile.getBytes();
        } catch (IOException e) {
            imageData = null;
            log.error("Error reading image file for Zaehlstelle ID: {}", zaehlstelle.getId(), e);
            return null;
        }

        imageEntity.setData(imageData);
        imageEntity.setZaehlstelleId(zaehlstelle.getId());
        imageEntity.setVersion(0L);

        return zaehlstelleImageRepository.save(imageEntity);
    }
}
