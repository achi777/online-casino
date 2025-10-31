package com.casino.dto;

import com.casino.entity.Game;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameResponse {
    private Long id;
    private String gameCode;
    private String name;
    private String description;
    private String providerName;
    private String category;
    private String thumbnailUrl;
    private BigDecimal rtp;
    private String status;
    private Boolean featured;

    public static GameResponse fromEntity(Game game) {
        GameResponse response = new GameResponse();
        response.setId(game.getId());
        response.setGameCode(game.getGameCode());
        response.setName(game.getName());
        response.setDescription(game.getDescription());
        response.setProviderName(game.getProvider().getName());
        response.setCategory(game.getCategory().name());
        response.setThumbnailUrl(game.getThumbnailUrl());
        response.setRtp(game.getRtp());
        response.setStatus(game.getStatus().name());
        response.setFeatured(game.getFeatured());
        return response;
    }
}
