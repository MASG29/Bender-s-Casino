package com.bendercasino.service;

import com.bendercasino.model.JokeTrigger;
import com.bendercasino.model.blackjack.Outcome;
import com.bendercasino.model.blackjack.Player;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JokeService {

    private final Map<JokeTrigger, List<String>> jokes;
    private final Map<String, Integer> lastJokeIndex = new ConcurrentHashMap<>();
    private final Random random = new Random();


    public JokeService(ObjectMapper objectMapper) {
        InputStream is = getClass().getResourceAsStream("/bender-jokes.json");
        jokes = objectMapper.readValue(is,
                new TypeReference<Map<JokeTrigger, List<String>>>() {});
    }

    public String jokeFor(Player player, Outcome outcome) {
        JokeTrigger trigger;

        if (player.getConsecutiveBlackjacks() >= 2 ) {
            trigger = JokeTrigger.DOUBLE_BLACKJACK;
        } else if (player.getConsecutiveLosses() >= 3 ) {
            trigger = JokeTrigger.LOSING_STREAK;
        } else if (player.getConsecutiveWins() >= 3 ) {
            trigger = JokeTrigger.WINNING_STREAK;
        } else if (player.getBalance() == 0 ) {
            trigger = JokeTrigger.BROKE;
        } else if (outcome != null) {
            trigger = mapOutcome(outcome);
        } else {
            trigger =JokeTrigger.GAME_START;
        }
        return jokeFor(player, trigger);
    }

    private JokeTrigger mapOutcome(Outcome outcome) {
        return switch (outcome) {
            case PLAYER_BLACKJACK ->  JokeTrigger.PLAYER_BLACKJACK;
            case PLAYER_WIN       ->  JokeTrigger.PLAYER_WIN;
            case DEALER_WIN       ->  JokeTrigger.DEALER_WIN;
            case PLAYER_BUST      ->  JokeTrigger.PLAYER_BUST;
            case DEALER_BUST      ->  JokeTrigger.DEALER_BUST;
            case PUSH             ->  JokeTrigger.PUSH;
        };
    }

    public String jokeFor(Player player, JokeTrigger trigger) {
        List<String> pool = jokes.getOrDefault(trigger,
                jokes.getOrDefault(JokeTrigger.GAME_START, List.of("...")));

        return pick(player, trigger, pool);
    }
    private String pick(Player player, JokeTrigger trigger, List<String> pool) {
        if (pool.isEmpty()) {
            pool = jokes.getOrDefault(JokeTrigger.GAME_START, List.of("..."));
        }
        String key = player.getId() + ":" + trigger.name();
        int last =  lastJokeIndex.getOrDefault(key, -1);

        int index;
        if (pool.size() == 1) {
            index = 0;
        } else {
            do {
                index = random.nextInt(pool.size());
            } while (index == last);
        }

        lastJokeIndex.put(key, index);
        String joke = pool.get(index);
        return joke.replace("{player}", player.getName());
    }
}
