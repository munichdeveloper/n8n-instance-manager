package de.dgtlschmd.n8n.dto;

public class PasswordResetRequestDto {
    private String email;

    public PasswordResetRequestDto() {
    }

    public PasswordResetRequestDto(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

