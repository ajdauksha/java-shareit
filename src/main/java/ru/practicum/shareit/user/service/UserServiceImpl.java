package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validateEmail(user.getEmail());
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    private void validateEmail(String email) {
        userRepository.findAll().forEach(user -> {
            if (email.equals(user.getEmail())) {
                throw new AlreadyExistException("Пользователь с email %s уже зарегистрирован".formatted(email));
            }
        });
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id %d не найден".formatted(id)));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(Long id, UserUpdateDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id %d не найден".formatted(id)));

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.update(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
