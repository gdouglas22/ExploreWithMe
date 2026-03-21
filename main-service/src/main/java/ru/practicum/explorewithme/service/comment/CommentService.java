package ru.practicum.explorewithme.service.comment;

import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewComment;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewComment newComment);
}
