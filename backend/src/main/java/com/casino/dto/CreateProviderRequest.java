package com.casino.dto;

import com.casino.entity.GameProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProviderRequest {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String logoUrl;

    private String apiUrl;

    private String apiKey;

    @NotNull(message = "Integration type is required")
    private GameProvider.IntegrationType integrationType;
}
