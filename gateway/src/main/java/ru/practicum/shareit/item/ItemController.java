package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;


@Validated
@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        return itemClient.create(ownerId, itemDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable @Positive Long id) {
        return itemClient.getById(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        return itemClient.getAllByOwnerId(ownerId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody ItemUpdateDto itemDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        return itemClient.update(id, ownerId, itemDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        itemClient.delete(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam String text) {
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody CommentDto commentDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long authorId) {
        return itemClient.addComment(itemId, authorId, commentDto);
    }

}