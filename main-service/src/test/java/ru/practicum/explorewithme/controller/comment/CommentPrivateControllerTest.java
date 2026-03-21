package ru.practicum.explorewithme.controller.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewComment;
import ru.practicum.explorewithme.service.comment.CommentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebMvcTest(CommentPrivateController.class)
class CommentPrivateControllerTest {
    private final DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CommentService commentService;

    @Test
    void createCommentShouldCreateCommentCorrectly() throws Exception {
        NewComment newComment = new NewComment("test text".repeat(20));
        String dateTime = LocalDateTime.now().format(customFormatter);
        CommentDto expectedComment = CommentDto.builder()
                .id(1L)
                .text("test text".repeat(20))
                .user(10L)
                .event(20L)
                .createdOn(dateTime)
                .build();

        Mockito.when(commentService.createComment(10L, 20L, newComment)).thenReturn(expectedComment);

        String requestContent = mapper.writeValueAsString(newComment);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/10/comments/events/20")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("test text".repeat(20)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.event").value(20))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdOn").value(dateTime));

        Mockito.verify(commentService, Mockito.times(1)).createComment(
                Mockito.eq(10L), Mockito.eq(20L), Mockito.eq(newComment));
    }

    @Test
    void createCommentShouldThrowBadRequestWhenTextLessThan20() throws Exception {
        NewComment newComment = new NewComment("first_".repeat(3));

        String requestContent = mapper.writeValueAsString(newComment);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/10/comments/events/20")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(commentService, Mockito.never()).createComment(
                Mockito.eq(10L), Mockito.eq(20L), Mockito.eq(newComment));
    }

    @Test
    void createCommentShouldThrowBadRequestWhenTextMoreThan2000() throws Exception {
        NewComment newComment = new NewComment("tes".repeat(667));

        String requestContent = mapper.writeValueAsString(newComment);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/10/comments/events/20")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(commentService, Mockito.never()).createComment(
                Mockito.eq(10L), Mockito.eq(20L), Mockito.eq(newComment));
    }

    @Test
    void createCommentShouldThrowBadRequestWhenTextIsEmpty() throws Exception {
        NewComment newComment = new NewComment("  ".repeat(100));

        String requestContent = mapper.writeValueAsString(newComment);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/10/comments/events/20")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(commentService, Mockito.never()).createComment(
                Mockito.eq(10L), Mockito.eq(20L), Mockito.eq(newComment));
    }

    @Test
    void createCommentShouldThrowBadRequestWhenTextIsNull() throws Exception {
        NewComment newComment = new NewComment(null);

        String requestContent = mapper.writeValueAsString(newComment);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/10/comments/events/20")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(commentService, Mockito.never()).createComment(
                Mockito.eq(10L), Mockito.eq(20L), Mockito.eq(newComment));
    }
}