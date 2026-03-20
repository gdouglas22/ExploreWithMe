package ru.practicum.explorewithme.controller.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.service.comment.CommentService;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("admin/comments")
public class CommentAdminController {
    private final CommentService commentService;
}
