package com.example.service;

import com.example.dto.FitnessProfileDto;
import com.example.dto.PostDto;
import com.example.dto.SearchResponseDto;
import com.example.dto.WorkoutDto;
import com.example.model.Post;
import com.example.model.User;
import com.example.model.Workout;
import com.example.repository.PostRepository;
import com.example.repository.UserRepository;
import com.example.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final PostRepository postRepository;

    public SearchService(UserRepository userRepository, WorkoutRepository workoutRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.workoutRepository = workoutRepository;
        this.postRepository = postRepository;
    }

    public SearchResponseDto globalSearch(String query, Long currentUserId) {
        SearchResponseDto response = new SearchResponseDto();

        List<User> userResults = userRepository.findByUsernameContainingIgnoreCaseOrHeadlineContainingIgnoreCase(query, query);
        List<FitnessProfileDto> userDtos = userResults.stream().map(this::mapToProfileDto).collect(Collectors.toList());

        List<Workout> workoutResults = workoutRepository.searchVisibleWorkouts(query);
        List<WorkoutDto> workoutDtos = workoutResults.stream().map(this::mapToWorkoutDto).collect(Collectors.toList());

        List<Post> postResults = postRepository.findByContentContainingIgnoreCase(query);
        List<PostDto> postDtos = postResults.stream().map(post -> mapToPostDto(post, currentUserId)).collect(Collectors.toList());

        response.setUsers(userDtos);
        response.setWorkouts(workoutDtos);
        response.setPosts(postDtos);

        return response;
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
        return dto;
    }

    private WorkoutDto mapToWorkoutDto(Workout workout) {
        WorkoutDto dto = new WorkoutDto();
        dto.setId(workout.getId());
        dto.setName(workout.getName());
        dto.setDescription(workout.getDescription());
        dto.setMuscleGroup(workout.getMuscleGroup());
        dto.setCategory(workout.getCategory());
        dto.setDifficulty(workout.getDifficulty());
        dto.setEstimatedCaloriesBurned(workout.getEstimatedCaloriesBurned());
        dto.setEstimatedDurationMinutes(workout.getEstimatedDurationMinutes());
        dto.setCreatedAt(workout.getCreatedAt());
        dto.setActive(workout.getActive());
        return dto;
    }

    private PostDto mapToPostDto(Post post, Long currentUserId) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorName(post.getAuthor().getRealUsername());
        dto.setAuthorAvatar(post.getAuthor().getAvatarUrl());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        
        int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
        dto.setLikesCount(likeCount);
        
        boolean liked = post.getLikes() != null && currentUserId != null && post.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(currentUserId));
        dto.setLikedByCurrentUser(liked);
        return dto;
    }
}
