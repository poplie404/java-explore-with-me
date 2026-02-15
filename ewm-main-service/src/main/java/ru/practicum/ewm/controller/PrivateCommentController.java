package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentRequest;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable("userId") Long userId,
                             @PathVariable("eventId") Long eventId,
                             @Valid @RequestBody NewCommentDto commentDto) {
        return commentService.create(userId, eventId, commentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable("userId") Long userId,
                            @PathVariable("commentId") Long commentId,
                            @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.update(userId, commentId, request);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("userId") Long userId,
                      @PathVariable("commentId") Long commentId) {
        commentService.delete(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserComments(@PathVariable("userId") Long userId,
                                           @RequestParam(defaultValue = "0") int from,
                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return commentService.getUserComments(userId, pageable);
    }
}
