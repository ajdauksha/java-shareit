package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto createUserDto() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("User");
        userDto.setEmail("user@email.com");
        return userDto;
    }

    @Test
    void create() throws Exception {
        UserDto userDto = createUserDto();
        when(userService.create(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@email.com"));
    }

    @Test
    void getById() throws Exception {
        UserDto userDto = createUserDto();
        when(userService.getById(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    void getByIdWithInvalidId() throws Exception {
        when(userService.getById(anyLong())).thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAll() throws Exception {
        UserDto userDto = createUserDto();
        when(userService.getAll()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User"))
                .andExpect(jsonPath("$[0].email").value("user@email.com"));
    }

    @Test
    void getAllWithEmptyList() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void update() throws Exception {
        UserDto userDto = createUserDto();
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated User");

        when(userService.update(anyLong(), any(UserUpdateDto.class))).thenReturn(userDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    void updateWithNameOnly() throws Exception {
        UserDto userDto = createUserDto();
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated Name");

        when(userService.update(anyLong(), any(UserUpdateDto.class))).thenReturn(userDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateWithEmailOnly() throws Exception {
        UserDto userDto = createUserDto();
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("updated@email.com");

        when(userService.update(anyLong(), any(UserUpdateDto.class))).thenReturn(userDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateWithInvalidId() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated User");

        when(userService.update(anyLong(), any(UserUpdateDto.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateWithEmptyBody() throws Exception {
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTest() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteWithInvalidId() throws Exception {
        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNoContent());
    }

}
