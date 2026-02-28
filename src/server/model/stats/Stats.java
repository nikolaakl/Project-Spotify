package server.model.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Stats {
    private final Map<String, Long> plays;

    public Stats() {
        this.plays = new ConcurrentHashMap<>();
    }

    public Map<String, Long> getPlays() {
        return Map.copyOf(this.plays);
    }

    public void incrementPlays(String songId) {
        this.plays.compute(songId, (k, v) -> (v == null) ? 1L : v + 1);
    }
}