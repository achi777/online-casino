package com.casino.dto;

import com.casino.entity.GameProvider;
import lombok.Data;

@Data
public class ProviderResponse {
    private Long id;
    private String code;
    private String name;
    private String logoUrl;
    private String status;
    private String integrationType;

    public static ProviderResponse fromEntity(GameProvider provider) {
        ProviderResponse response = new ProviderResponse();
        response.setId(provider.getId());
        response.setCode(provider.getCode());
        response.setName(provider.getName());
        response.setLogoUrl(provider.getLogoUrl());
        response.setStatus(provider.getStatus().name());
        response.setIntegrationType(provider.getIntegrationType().name());
        return response;
    }
}
