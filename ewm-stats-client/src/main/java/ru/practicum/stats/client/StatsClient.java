package ru.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsClient {

    private final RestTemplate restTemplate;

    @Value("${stats.server.url:http://localhost:9090}")
    private String serverUrl;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EndpointHitDto hit(EndpointHitDto hitDto) {
        String url = serverUrl + "/hit";
        return restTemplate.postForObject(url, hitDto, EndpointHitDto.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {

        try {
            String encodedStart = URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8);
            String encodedEnd = URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8);

            StringBuilder urlBuilder = new StringBuilder(serverUrl + "/stats");
            urlBuilder.append("?start=").append(encodedStart);
            urlBuilder.append("&end=").append(encodedEnd);
            urlBuilder.append("&unique=").append(unique);

            if (uris != null && !uris.isEmpty()) {
                for (String uri : uris) {
                    urlBuilder.append("&uris=").append(uri);
                }
            }

            String url = urlBuilder.toString();

            ViewStatsDto[] response = restTemplate.getForObject(url, ViewStatsDto[].class);
            if (response == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(response);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}