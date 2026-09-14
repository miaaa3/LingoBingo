package com.lingobingo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translates short text via the free MyMemory API (no API key). Results are
 * cached (bounded LRU) so repeated words are instant and we stay well under the
 * free daily quota. Any failure returns null — callers degrade gracefully
 * (the UI simply shows no suggestion) rather than surfacing an error.
 *
 * Kept dependency-free (JDK HttpClient) and side-effect-free apart from its
 * own cache, so it stays easy to reason about and swap out later.
 */
@Service
public class TranslationService {

    private static final String ENDPOINT = "https://api.mymemory.translated.net/get";
    private static final int MAX_LEN = 200;         // free-tier friendly
    private static final int CACHE_MAX = 5000;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    // Simple thread-safe LRU: evicts the least-recently-used entry past the cap.
    private final Map<String, String> cache = Collections.synchronizedMap(
            new LinkedHashMap<String, String>(1024, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > CACHE_MAX;
                }
            });

    /**
     * @return the translated text, or null if input is invalid or the lookup failed.
     */
    public String translate(String text, String from, String to) {
        if (text == null) return null;
        String q = text.trim();
        if (q.isEmpty() || q.length() > MAX_LEN) return null;
        from = normalizeLang(from);
        to = normalizeLang(to);
        if (from.equals(to)) return q;

        String key = from + "|" + to + "|" + q.toLowerCase();
        String cached = cache.get(key);
        if (cached != null) return cached;

        try {
            String url = ENDPOINT + "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8)
                    + "&langpair=" + from + "%7C" + to;
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("User-Agent", "LingoBingo/1.0")
                    .GET()
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) return null;

            JsonNode root = mapper.readTree(res.body());
            JsonNode data = root.path("responseData").path("translatedText");
            if (data.isMissingNode() || data.asText().isBlank()) return null;

            String result = data.asText().trim();
            // MyMemory echoes an uppercase error message in translatedText on quota errors
            if (result.startsWith("MYMEMORY WARNING") || result.startsWith("PLEASE SELECT")) return null;

            cache.put(key, result);
            return result;
        } catch (Exception e) {
            return null;   // network/parse failure -> no suggestion
        }
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) return "en";
        return lang.trim().toLowerCase().substring(0, Math.min(2, lang.trim().length()));
    }
}
