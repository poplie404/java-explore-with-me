package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.ewm.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getEventComments(@PathVariable("eventId") Long eventId,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return commentService.getEventComments(eventId, pageable);
    }

    @GetMapping("/events/{eventId}/comments/{commentId}")
    public CommentDto getComment(@PathVariable("eventId") Long eventId,
                                @PathVariable("commentId") Long commentId) {
        return commentService.getCommentById(commentId);
    }
}
