package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private UserController userController;

    @Test
    void create() {
        UserDto userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@email.com");

        UserDto result = userController.create(userDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("User", result.getName());
        assertEquals("user@email.com", result.getEmail());
    }

    @Test
    void getById() {
        UserDto userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@email.com");
        UserDto created = userController.create(userDto);

        UserDto result = userController.getById(created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
    }

    @Test
    void update() {
        UserDto userDto = new UserDto();
        userDto.setName("User");
        userDto.setEmail("user@email.com");
        UserDto created = userController.create(userDto);

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated User");

        UserDto result = userController.update(created.getId(), updateDto);

        assertNotNull(result);
        assertEquals("Updated User", result.getName());
    }
}
