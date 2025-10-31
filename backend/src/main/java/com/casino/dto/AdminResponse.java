package com.casino.dto;

import com.casino.entity.Admin;
import lombok.Data;

@Data
public class AdminResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String status;

    public static AdminResponse fromEntity(Admin admin) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setEmail(admin.getEmail());
        response.setFirstName(admin.getFirstName());
        response.setLastName(admin.getLastName());
        response.setRole(admin.getRole().name());
        response.setStatus(admin.getStatus().name());
        return response;
    }
}
