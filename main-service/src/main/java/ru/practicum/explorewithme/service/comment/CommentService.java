package ru.practicum.explorewithme.service.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewComment;

public interface CommentService {
    CommentDto create(Long userId, Long eventId, NewComment newComment);

    CommentDto update(Long userId, Long commentId, NewComment newComment);

    void delete(Long userId, Long commentId);

    Page<CommentDto> getAll(Long userId, Pageable pageable);
}
