package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.service.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private ItemRequestDto createItemRequestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a drill");
        return dto;
    }

    @Test
    void create() throws Exception {
        ItemRequestDto requestDto = createItemRequestDto();
        when(requestService.create(any(ItemRequestDto.class), anyLong())).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void createWithoutUserIdHeader() throws Exception {
        ItemRequestDto requestDto = createItemRequestDto();

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getOwnRequests() throws Exception {
        ItemRequestDto requestDto = createItemRequestDto();
        when(requestService.getOwnRequests(anyLong())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill"));
    }

    @Test
    void getOwnRequestsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getOwnRequestsWithEmptyList() throws Exception {
        when(requestService.getOwnRequests(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllRequests() throws Exception {
        ItemRequestDto requestDto = createItemRequestDto();
        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill"));
    }

    @Test
    void getAllRequestsWithDefaultPagination() throws Exception {
        ItemRequestDto requestDto = createItemRequestDto();
        when(requestService.getAllRequests(anyLong(), eq(0), eq(10))).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllRequestsWithCustomPagination() throws Exception {
        ItemRequestDto requestDto = createItemRequestDto();
        when(requestService.getAllRequests(anyLong(), eq(5), eq(20))).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllRequestsWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getById() throws Exception {
        ItemRequestDto requestDto = createItemRequestDto();
        when(requestService.getById(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void getByIdWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getByIdWithInvalidRequestId() throws Exception {
        when(requestService.getById(anyLong(), anyLong()))
                .thenThrow(new IllegalArgumentException("Request not found"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getByIdWithInvalidUserId() throws Exception {
        when(requestService.getById(anyLong(), anyLong()))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isInternalServerError());
    }

}
