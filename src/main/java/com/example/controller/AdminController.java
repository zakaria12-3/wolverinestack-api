package com.example.controller;

import com.example.model.User;
import com.example.model.Workout;
import com.example.service.UserService;
import com.example.service.WorkoutService;
import com.example.service.ReportService;
import com.example.service.WorkoutPlanService;
import com.example.dto.ReportDto;
import com.example.model.ReportStatus;
import com.example.model.WorkoutPlan;
import com.example.model.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final WorkoutService workoutService;
    private final ReportService reportService;
    private final WorkoutPlanService workoutPlanService;

    public AdminController(UserService userService, WorkoutService workoutService,
                           ReportService reportService,
                           WorkoutPlanService workoutPlanService) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.reportService = reportService;
        this.workoutPlanService = workoutPlanService;
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return "Admin dashboard";
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.allUsers();
    }

    @GetMapping("/workouts")
    public List<Workout> getAllWorkouts() {
        return workoutService.getAllWorkouts();
    }

    @GetMapping("/plans")
    public List<WorkoutPlan> getAllPlans() {
        return workoutPlanService.getAllPlans();
    }

    @GetMapping("/reports")
    public List<ReportDto> getReports(@RequestParam(required = false) String status) {
        return reportService.getReports(status);
    }

    @PutMapping("/reports/{id}/status")
    public ResponseEntity<?> updateReportStatus(@PathVariable Long id, @RequestParam ReportStatus status) {
        return ResponseEntity.ok(reportService.updateStatus(id, status));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.updateUserStatus(id, enabled));
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveTrainer(@PathVariable Long id) {
        return ResponseEntity.ok(userService.approveTrainer(id));
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectTrainer(@PathVariable Long id) {
        return ResponseEntity.ok(userService.rejectTrainer(id));
    }

    @DeleteMapping("/workouts/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable Long id) {
        workoutService.deleteWorkout(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/workouts/{id}/approve")
    public Workout approveWorkout(@PathVariable Long id) {
        return workoutService.approveWorkout(id);
    }

    @PutMapping("/workouts/{id}/block")
    public Workout blockWorkout(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return workoutService.blockWorkout(id, reason);
    }

    @PutMapping("/workouts/{id}/rescan")
    public Workout rescanWorkout(@PathVariable Long id) {
        return workoutService.rescanWorkout(id);
    }
}
