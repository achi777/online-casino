package com.casino.dto;

import com.casino.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResponsibleGamingResponse {
    private BigDecimal dailyDepositLimit;
    private BigDecimal weeklyDepositLimit;
    private BigDecimal monthlyDepositLimit;
    private Integer dailyTimeLimit;
    private LocalDateTime selfExclusionUntil;
    private Boolean temporaryBlock;

    public static ResponsibleGamingResponse fromUser(User user) {
        ResponsibleGamingResponse response = new ResponsibleGamingResponse();
        response.setDailyDepositLimit(user.getDailyDepositLimit());
        response.setWeeklyDepositLimit(user.getWeeklyDepositLimit());
        response.setMonthlyDepositLimit(user.getMonthlyDepositLimit());
        response.setDailyTimeLimit(user.getDailyTimeLimit());
        response.setSelfExclusionUntil(user.getSelfExclusionUntil());
        response.setTemporaryBlock(user.getTemporaryBlock());
        return response;
    }
}
