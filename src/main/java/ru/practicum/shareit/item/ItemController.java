package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        return itemService.create(itemDto, ownerId);
    }

    @GetMapping("/{id}")
    public ItemDto getById(@PathVariable @Positive Long id) {
        return itemService.getById(id);
    }

    @GetMapping
    public List<ItemDto> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        return itemService.getAllByOwnerId(ownerId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(
            @PathVariable Long id,
            @RequestBody ItemUpdateDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        return itemService.update(id, itemDto, ownerId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        itemService.delete(id);
    }

    @GetMapping("/search")
    public List<ItemDto> search(
            @RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody CommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long authorId) {
        return itemService.addComment(itemId, commentDto, authorId);
    }

}