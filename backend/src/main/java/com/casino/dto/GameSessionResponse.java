package com.casino.dto;

import com.casino.entity.GameSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionResponse {
    private Long id;
    private String sessionToken;
    private String gameName;
    private String gameCode;
    private BigDecimal totalBet;
    private BigDecimal totalWin;
    private Integer totalRounds;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public static GameSessionResponse fromEntity(GameSession session) {
        GameSessionResponse response = new GameSessionResponse();
        response.setId(session.getId());
        response.setSessionToken(session.getSessionToken());
        response.setGameName(session.getGame().getName());
        response.setGameCode(session.getGame().getGameCode());
        response.setTotalBet(session.getTotalBet());
        response.setTotalWin(session.getTotalWin());
        response.setTotalRounds(session.getRoundsPlayed());
        response.setStatus(session.getStatus().name());
        response.setStartedAt(session.getStartedAt());
        response.setEndedAt(session.getEndedAt());
        return response;
    }
}
