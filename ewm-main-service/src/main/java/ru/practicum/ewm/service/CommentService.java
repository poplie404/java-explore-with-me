package ru.practicum.ewm.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentRequest;

import java.util.List;

public interface CommentService {
    CommentDto create(Long userId, Long eventId, NewCommentDto commentDto);
    
    CommentDto update(Long userId, Long commentId, UpdateCommentRequest request);
    
    void delete(Long userId, Long commentId);
    
    List<CommentDto> getEventComments(Long eventId, Pageable pageable);
    
    List<CommentDto> getUserComments(Long userId, Pageable pageable);
    
    CommentDto getCommentById(Long commentId);
}