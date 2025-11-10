package com.casino.controller;

import com.casino.entity.PokerTable;
import com.casino.entity.PokerTableSeat;
import com.casino.entity.User;
import com.casino.repository.UserRepository;
import com.casino.service.PokerGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PokerController {

    private final PokerGameService pokerGameService;
    private final UserRepository userRepository;

    @GetMapping("/api/poker/tables")
    public ResponseEntity<List<PokerTable>> getAvailableTables() {
        return ResponseEntity.ok(pokerGameService.getAvailableTables());
    }

    @GetMapping("/api/poker/tables/{tableId}")
    public ResponseEntity<PokerTable> getTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(pokerGameService.getTable(tableId));
    }

    @GetMapping("/api/poker/tables/{tableId}/seats")
    public ResponseEntity<List<PokerTableSeat>> getTableSeats(@PathVariable Long tableId) {
        return ResponseEntity.ok(pokerGameService.getTableSeats(tableId));
    }

    @PostMapping("/api/poker/tables/create")
    public ResponseEntity<PokerTable> createTable(@RequestBody CreateTableRequest request) {
        PokerTable table = pokerGameService.createTable(
                request.tableName(),
                request.smallBlind(),
                request.bigBlind(),
                request.minBuyIn(),
                request.maxBuyIn(),
                request.maxPlayers()
        );
        return ResponseEntity.ok(table);
    }

    @MessageMapping("/poker/table/{tableId}/join")
    @SendTo("/topic/poker/table/{tableId}")
    public Map<String, Object> joinTable(@DestinationVariable Long tableId,
                                         Map<String, Object> payload,
                                         Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal buyInAmount = new BigDecimal(payload.get("buyInAmount").toString());
        Integer seatNumber = (Integer) payload.get("seatNumber");

        pokerGameService.joinTable(tableId, user.getId(), buyInAmount, seatNumber);

        return Map.of(
                "type", "PLAYER_JOINED",
                "userId", user.getId(),
                "seatNumber", seatNumber
        );
    }

    @MessageMapping("/poker/table/{tableId}/leave")
    @SendTo("/topic/poker/table/{tableId}")
    public Map<String, Object> leaveTable(@DestinationVariable Long tableId,
                                          Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        pokerGameService.leaveTable(tableId, user.getId());

        return Map.of(
                "type", "PLAYER_LEFT",
                "userId", user.getId()
        );
    }

    @MessageMapping("/poker/table/{tableId}/action")
    @SendTo("/topic/poker/table/{tableId}")
    public Map<String, Object> playerAction(@DestinationVariable Long tableId,
                                            Map<String, Object> payload,
                                            Principal principal) {
        // Handle player actions: fold, call, raise, check
        String action = (String) payload.get("action");
        BigDecimal amount = payload.containsKey("amount") ?
                new BigDecimal(payload.get("amount").toString()) : BigDecimal.ZERO;

        return Map.of(
                "type", "PLAYER_ACTION",
                "action", action,
                "amount", amount
        );
    }

    public record CreateTableRequest(
            String tableName,
            BigDecimal smallBlind,
            BigDecimal bigBlind,
            BigDecimal minBuyIn,
            BigDecimal maxBuyIn,
            Integer maxPlayers
    ) {}
}
