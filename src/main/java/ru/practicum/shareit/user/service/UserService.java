package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto getById(Long id);

    List<UserDto> getAll();

    UserDto update(Long id, UserUpdateDto userDto);

    void delete(Long id);
}
