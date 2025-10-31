package com.casino.dto;

import com.casino.entity.User;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private BigDecimal balance;
    private String status;
    private String kycStatus;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setBalance(user.getBalance());
        response.setStatus(user.getStatus().name());
        response.setKycStatus(user.getKycStatus().name());
        return response;
    }
}
