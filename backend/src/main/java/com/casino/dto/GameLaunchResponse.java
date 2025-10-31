package com.casino.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameLaunchResponse {
    private String sessionToken;
    private String launchUrl;
    private String integrationType; // API or IFRAME
}
