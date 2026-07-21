package com.example.dto;

public class SignupResponseDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String approvalStatus;
    private boolean onboardingComplete;

    public SignupResponseDto() {}

    public SignupResponseDto(Long id, String username, String email, String role, String approvalStatus, boolean onboardingComplete) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.approvalStatus = approvalStatus;
        this.onboardingComplete = onboardingComplete;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public boolean isOnboardingComplete() { return onboardingComplete; }
    public void setOnboardingComplete(boolean onboardingComplete) { this.onboardingComplete = onboardingComplete; }
}
