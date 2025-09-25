package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto createItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        return itemDto;
    }

    private CommentDto createCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setAuthorName("Author");
        return commentDto;
    }

    @Test
    void create() throws Exception {
        ItemDto itemDto = createItemDto();
        when(itemService.create(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void createWithoutUserIdHeader() throws Exception {
        ItemDto itemDto = createItemDto();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getById() throws Exception {
        ItemDto itemDto = createItemDto();
        when(itemService.getById(anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item"));
    }

    @Test
    void getByIdWithInvalidId() throws Exception {
        when(itemService.getById(anyLong())).thenThrow(new IllegalArgumentException("Item not found"));

        mockMvc.perform(get("/items/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllByOwnerId() throws Exception {
        ItemDto itemDto = createItemDto();
        when(itemService.getAllByOwnerId(anyLong())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Item"));
    }

    @Test
    void getAllByOwnerIdWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void update() throws Exception {
        ItemDto itemDto = createItemDto();
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Item");

        when(itemService.update(anyLong(), any(ItemUpdateDto.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateWithoutUserIdHeader() throws Exception {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Item");

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateWithInvalidUserId() throws Exception {
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("Updated Item");

        when(itemService.update(anyLong(), any(ItemUpdateDto.class), anyLong()))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteTest() throws Exception {
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void search() throws Exception {
        ItemDto itemDto = createItemDto();
        when(itemService.search(anyString())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Item"));
    }

    @Test
    void searchWithEmptyText() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchWithoutTextParam() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addComment() throws Exception {
        CommentDto commentDto = createCommentDto();
        when(itemService.addComment(anyLong(), any(CommentDto.class), anyLong())).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("Author"));
    }

    @Test
    void addCommentWithoutUserIdHeader() throws Exception {
        CommentDto commentDto = createCommentDto();

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addCommentWithInvalidItemId() throws Exception {
        CommentDto commentDto = createCommentDto();

        when(itemService.addComment(anyLong(), any(CommentDto.class), anyLong()))
                .thenThrow(new IllegalArgumentException("Item not found"));

        mockMvc.perform(post("/items/999/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllByOwnerIdWithInvalidUserId() throws Exception {
        when(itemService.getAllByOwnerId(anyLong()))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isInternalServerError());
    }
}
