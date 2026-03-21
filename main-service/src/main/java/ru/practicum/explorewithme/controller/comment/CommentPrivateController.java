package ru.practicum.explorewithme.controller.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewComment;
import ru.practicum.explorewithme.service.comment.CommentService;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    public ResponseEntity<CommentDto> createComment(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @Valid @RequestBody NewComment newComment) {
        CommentDto commentDto = commentService.createComment(userId, eventId, newComment);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(commentDto);
    }
}