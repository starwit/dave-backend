package de.muenchen.dave.services.kalendertag;

import de.muenchen.dave.domain.dtos.HolidaysDTO;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OpenHolidaysApiClient {

    private final RestTemplateBuilder restTemplateBuilder;

    public List<HolidaysDTO> loadHolidays(final URI uri) {
        final RestTemplate restTemplate = restTemplateBuilder.build();

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        final ResponseEntity<HolidaysDTO[]> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                HolidaysDTO[].class);

        final HolidaysDTO[] body = response.getBody();
        if (body == null) {
            return List.of();
        }
        return Arrays.asList(body);
    }
}
