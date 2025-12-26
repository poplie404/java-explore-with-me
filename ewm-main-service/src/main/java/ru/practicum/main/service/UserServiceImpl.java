package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserShortDto;
import ru.practicum.main.entity.User;
import ru.practicum.main.mapper.UserMapper;
import ru.practicum.main.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional
    public UserShortDto create(NewUserRequest dto) {
        User user = UserMapper.toEntity(dto);
        user = repository.save(user);
        return UserMapper.toShortDto(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        repository.deleteById(userId);
    }

    @Override
    public List<UserShortDto> getUsers(List<Long> ids, Integer from, Integer size) {
        if (ids != null && !ids.isEmpty()) {
            Pageable pageable = PageRequest.of(from / size, size);
            return repository.findAllByIdIn(ids, pageable).stream()
                    .map(UserMapper::toShortDto)
                    .collect(Collectors.toList());
        }
        return repository.findAll(PageRequest.of(from / size, size)).stream()
                .map(UserMapper::toShortDto)
                .collect(Collectors.toList());
    }
}
