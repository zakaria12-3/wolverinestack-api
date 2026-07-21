package com.example.dto;

import java.util.List;

public class SearchResponseDto {
    private List<FitnessProfileDto> users;
    private List<WorkoutDto> workouts;
    private List<PostDto> posts;

    public List<FitnessProfileDto> getUsers() { return users; }
    public void setUsers(List<FitnessProfileDto> users) { this.users = users; }

    public List<WorkoutDto> getWorkouts() { return workouts; }
    public void setWorkouts(List<WorkoutDto> workouts) { this.workouts = workouts; }

    public List<PostDto> getPosts() { return posts; }
    public void setPosts(List<PostDto> posts) { this.posts = posts; }
}
