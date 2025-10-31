package com.casino.service;

import com.casino.dto.*;
import com.casino.entity.Game;
import com.casino.entity.GameProvider;
import com.casino.exception.BadRequestException;
import com.casino.repository.GameProviderRepository;
import com.casino.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminGameService {

    private final GameRepository gameRepository;
    private final GameProviderRepository providerRepository;
    private final AuditService auditService;

    @Transactional
    public GameResponse createGame(Long adminId, CreateGameRequest request) {
        if (gameRepository.findByGameCode(request.getGameCode()).isPresent()) {
            throw new BadRequestException("Game code already exists");
        }

        GameProvider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new BadRequestException("Provider not found"));

        Game game = new Game();
        game.setGameCode(request.getGameCode());
        game.setName(request.getName());
        game.setDescription(request.getDescription());
        game.setProvider(provider);
        game.setCategory(request.getCategory());
        game.setThumbnailUrl(request.getThumbnailUrl());
        game.setIframeUrl(request.getIframeUrl());
        game.setRtp(request.getRtp());
        game.setSortOrder(request.getSortOrder());
        game.setFeatured(request.getFeatured());

        game = gameRepository.save(game);

        auditService.logAdminAction(adminId, "GAME_CREATED", "Game", game.getId(),
                null, "Game: " + game.getName());

        return GameResponse.fromEntity(game);
    }

    @Transactional
    public GameResponse updateGameStatus(Long adminId, Long gameId, Game.GameStatus status) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BadRequestException("Game not found"));

        String oldStatus = game.getStatus().name();
        game.setStatus(status);
        game = gameRepository.save(game);

        auditService.logAdminAction(adminId, "GAME_STATUS_CHANGED", "Game", gameId,
                oldStatus, status.name());

        return GameResponse.fromEntity(game);
    }

    @Transactional
    public void deleteGame(Long adminId, Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BadRequestException("Game not found"));

        auditService.logAdminAction(adminId, "GAME_DELETED", "Game", gameId,
                game.getName(), null);

        gameRepository.delete(game);
    }

    public Page<GameResponse> getAllGames(Pageable pageable) {
        return gameRepository.findAll(pageable)
                .map(GameResponse::fromEntity);
    }

    @Transactional
    public ProviderResponse createProvider(Long adminId, CreateProviderRequest request) {
        if (providerRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Provider code already exists");
        }

        GameProvider provider = new GameProvider();
        provider.setCode(request.getCode());
        provider.setName(request.getName());
        provider.setLogoUrl(request.getLogoUrl());
        provider.setApiUrl(request.getApiUrl());
        provider.setApiKey(request.getApiKey());
        provider.setIntegrationType(request.getIntegrationType());

        provider = providerRepository.save(provider);

        auditService.logAdminAction(adminId, "PROVIDER_CREATED", "GameProvider", provider.getId(),
                null, "Provider: " + provider.getName());

        return ProviderResponse.fromEntity(provider);
    }

    @Transactional
    public ProviderResponse updateProviderStatus(Long adminId, Long providerId, GameProvider.ProviderStatus status) {
        GameProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new BadRequestException("Provider not found"));

        String oldStatus = provider.getStatus().name();
        provider.setStatus(status);
        provider = providerRepository.save(provider);

        auditService.logAdminAction(adminId, "PROVIDER_STATUS_CHANGED", "GameProvider", providerId,
                oldStatus, status.name());

        return ProviderResponse.fromEntity(provider);
    }

    public List<ProviderResponse> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(ProviderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public GameResponse updateGame(Long adminId, Long gameId, UpdateGameRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BadRequestException("Game not found"));

        if (request.getName() != null) {
            game.setName(request.getName());
        }
        if (request.getDescription() != null) {
            game.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            game.setCategory(request.getCategory());
        }
        if (request.getThumbnailUrl() != null) {
            game.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getIframeUrl() != null) {
            game.setIframeUrl(request.getIframeUrl());
        }
        if (request.getRtp() != null) {
            game.setRtp(request.getRtp());
        }
        if (request.getSortOrder() != null) {
            game.setSortOrder(request.getSortOrder());
        }
        if (request.getFeatured() != null) {
            game.setFeatured(request.getFeatured());
        }
        if (request.getStatus() != null) {
            game.setStatus(request.getStatus());
        }

        game = gameRepository.save(game);

        auditService.logAdminAction(adminId, "GAME_UPDATED", "Game", gameId,
                null, "Game: " + game.getName());

        return GameResponse.fromEntity(game);
    }
}
