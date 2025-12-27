package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
public class StatsClient extends BaseClient {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl,
                       RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build());
    }

    public void hit(EndpointHitDto hitDto) {
        post(hitDto);  // Используем BaseClient.post()
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        StringBuilder path = new StringBuilder("/stats?start=");
        path.append(start.format(FORMATTER))
                .append("&end=").append(end.format(FORMATTER))
                .append("&unique=").append(unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                path.append("&uris=").append(uri);  // ✅ Правильный формат для List<String>
            }
        }

        ResponseEntity<Object> response = get(path.toString(), null);
        ViewStatsDto[] statsArray = (ViewStatsDto[]) response.getBody();
        return statsArray != null ? Arrays.asList(statsArray) : List.of();
    }
}
