package com.example.service;

import com.example.model.User;
import com.example.model.Role;
import com.example.repository.UserRepository;
import com.example.repository.MealEntryRepository;
import com.example.dto.FitnessProfileDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WorkoutService workoutService;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, MealEntryRepository mealEntryRepository,
                       WorkoutService workoutService,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.workoutService = workoutService;
        this.notificationService = notificationService;
    }

    public List<User> allUsers(){
        List<User> users=new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;

    }
    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        if (user.getRole() == Role.ROLE_MEMBER) {
            mealEntryRepository.deleteById(id);
        } else if (user.getRole() == Role.ROLE_TRAINER) {
            List<com.example.model.Workout> trainerWorkouts = workoutService.getWorkoutsByTrainer(id);
            for(com.example.model.Workout workout : trainerWorkouts) {
                workoutService.deleteWorkout(workout.getId());
            }
        }
        userRepository.delete(user);
    }

    public User updateUserRole(Long id, Role newRole) {
        User user = findById(id);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public User updateUserStatus(Long id, boolean enabled) {
        User user = findById(id);
        user.setEnabled(enabled);
        if (user.getRole() == Role.ROLE_TRAINER) {
            user.setApprovalStatus(enabled ? "APPROVED" : "REJECTED");
        }
        User savedUser = userRepository.save(user);
        if (savedUser.getRole() == Role.ROLE_TRAINER) {
            workoutService.setWorkoutsActiveByTrainer(id, enabled);
        }
        return savedUser;
    }

    public User approveTrainer(Long id) {
        User user = findById(id);
        if (user.getRole() != Role.ROLE_TRAINER) {
            throw new RuntimeException("Only trainer accounts can be approved");
        }
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Trainer must verify email before approval");
        }
        user.setEnabled(true);
        user.setApprovalStatus("APPROVED");
        User savedUser = userRepository.save(user);
        notificationService.create(
                savedUser,
                "ACCOUNT_STATUS",
                "Trainer account approved",
                "Your trainer account is now approved. You can create workouts and manage plans.",
                "/trainer"
        );
        return savedUser;
    }

    public User rejectTrainer(Long id) {
        User user = findById(id);
        if (user.getRole() != Role.ROLE_TRAINER) {
            throw new RuntimeException("Only trainer accounts can be rejected");
        }
        user.setEnabled(false);
        user.setApprovalStatus("REJECTED");
        workoutService.setWorkoutsActiveByTrainer(id, false);
        User savedUser = userRepository.save(user);
        notificationService.create(
                savedUser,
                "ACCOUNT_STATUS",
                "Trainer account rejected",
                "Your trainer account has been rejected by the administration. Contact support if you believe this is an error.",
                "/profile"
        );
        return savedUser;
    }

    public FitnessProfileDto getUserProfile(Long id) {
        User user = findById(id);
        return mapToProfileDto(user);
    }

    public User toggleDailyTip(Long id, boolean enabled) {
        User user = findById(id);
        user.setDailyTipEnabled(enabled);
        return userRepository.save(user);
    }

    public FitnessProfileDto updateUserProfile(Long id, FitnessProfileDto dto) {
        User user = findById(id);
        user.setBio(dto.getBio());
        user.setHeadline(dto.getHeadline());
        user.setLocation(dto.getLocation());
        user.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getGender() != null) {
            try { user.setGender(com.example.model.Gender.valueOf(dto.getGender().toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        user.setWeightKg(dto.getWeightKg());
        user.setHeightCm(dto.getHeightCm());
        user.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getFitnessGoal() != null) {
            try { user.setFitnessGoal(com.example.model.FitnessGoal.valueOf(dto.getFitnessGoal().toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        if (dto.getActivityLevel() != null) {
            try { user.setActivityLevel(com.example.model.ActivityLevel.valueOf(dto.getActivityLevel().toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        user.setDailyCalorieGoal(dto.getDailyCalorieGoal());
        user.setDailyProteinGoal(dto.getDailyProteinGoal());
        user.setDailyCarbsGoal(dto.getDailyCarbsGoal());
        user.setDailyFatGoal(dto.getDailyFatGoal());
        User savedUser = userRepository.save(user);
        return mapToProfileDto(savedUser);
    }

    private FitnessProfileDto mapToProfileDto(User user) {
        FitnessProfileDto dto = new FitnessProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getRealUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setHeadline(user.getHeadline());
        dto.setLocation(user.getLocation());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGender(user.getGender() != null ? user.getGender().name() : null);
        dto.setWeightKg(user.getWeightKg());
        dto.setHeightCm(user.getHeightCm());
        dto.setDateOfBirth(user.getDateOfBirth());
        if (user.getFitnessGoal() != null) dto.setFitnessGoal(user.getFitnessGoal().name());
        if (user.getActivityLevel() != null) dto.setActivityLevel(user.getActivityLevel().name());
        dto.setDailyCalorieGoal(user.getDailyCalorieGoal());
        dto.setDailyProteinGoal(user.getDailyProteinGoal());
        dto.setDailyCarbsGoal(user.getDailyCarbsGoal());
        dto.setDailyFatGoal(user.getDailyFatGoal());
        if (user.getGym() != null) {
            dto.setGymName(user.getGym().getName());
        }
        dto.setDailyTipEnabled(user.getDailyTipEnabled() != null && user.getDailyTipEnabled());
        dto.setReported(user.isReported());
        dto.setSuspended(user.isSuspended());
        return dto;
    }
}
