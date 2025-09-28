package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto createUserDto(String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Test
    void createShouldCreateUserSuccessfully() {
        UserDto userDto = createUserDto("User", "user@email.com");

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("User", result.getName());
        assertEquals("user@email.com", result.getEmail());
    }

    @Test
    void getByIdShouldReturnUserSuccessfully() {
        User user = createUser("User", "user@email.com");

        UserDto result = userService.getById(user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("User", result.getName());
        assertEquals("user@email.com", result.getEmail());
    }

    @Test
    void getByIdShouldThrowExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;

        assertThrows(NoSuchElementException.class, () -> userService.getById(nonExistentUserId));
    }

    @Test
    void getAllShouldReturnAllUsers() {
        User user1 = createUser("User1", "user1@email.com");
        User user2 = createUser("User2", "user2@email.com");

        List<UserDto> result = userService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User1")));
        assertTrue(result.stream().anyMatch(u -> u.getName().equals("User2")));
    }

    @Test
    void getAllShouldReturnEmptyListWhenNoUsers() {
        List<UserDto> result = userService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateShouldUpdateUserSuccessfully() {
        User user = createUser("OldName", "old@email.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("NewName");
        updateDto.setEmail("new@email.com");

        UserDto result = userService.update(user.getId(), updateDto);

        assertNotNull(result);
        assertEquals("NewName", result.getName());
        assertEquals("new@email.com", result.getEmail());
    }

    @Test
    void updateShouldUpdatePartialFields() {
        User user = createUser("User", "user@email.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("UpdatedName");

        UserDto result = userService.update(user.getId(), updateDto);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals("user@email.com", result.getEmail());
    }

    @Test
    void updateShouldUpdateOnlyEmail() {
        User user = createUser("User", "user@email.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("new@email.com");

        UserDto result = userService.update(user.getId(), updateDto);

        assertNotNull(result);
        assertEquals("User", result.getName());
        assertEquals("new@email.com", result.getEmail());
    }

    @Test
    void updateShouldThrowExceptionWhenUserNotFound() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("NewName");

        Long nonExistentUserId = 999L;

        assertThrows(NoSuchElementException.class, () -> userService.update(nonExistentUserId, updateDto));
    }

    @Test
    void deleteShouldDeleteUserSuccessfully() {
        User user = createUser("User", "user@email.com");

        assertDoesNotThrow(() -> userService.delete(user.getId()));
        assertThrows(NoSuchElementException.class, () -> userService.getById(user.getId()));
    }

    @Test
    void deleteShouldNotThrowExceptionWhenUserNotFound() {
        assertDoesNotThrow(() -> userService.delete(999L));
    }

    @Test
    void createShouldHandleMultipleUsers() {
        UserDto userDto1 = createUserDto("User1", "user1@email.com");
        UserDto userDto2 = createUserDto("User2", "user2@email.com");

        UserDto result1 = userService.create(userDto1);
        UserDto result2 = userService.create(userDto2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getId(), result2.getId());

        List<UserDto> allUsers = userService.getAll();
        assertEquals(2, allUsers.size());
    }

    @Test
    void updateShouldNotChangeId() {
        User user = createUser("User", "user@email.com");
        Long originalId = user.getId();

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("UpdatedName");

        UserDto result = userService.update(user.getId(), updateDto);

        assertNotNull(result);
        assertEquals(originalId, result.getId());
        assertEquals("UpdatedName", result.getName());
    }

    @Test
    void getByIdShouldReturnCorrectUserAfterUpdate() {
        User user = createUser("User", "user@email.com");

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("UpdatedName");
        userService.update(user.getId(), updateDto);

        UserDto result = userService.getById(user.getId());

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals("user@email.com", result.getEmail());
    }
}
