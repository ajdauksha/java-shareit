package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto getById(Long id);

    List<ItemDto> getAllByOwnerId(Long ownerId);

    ItemDto update(Long id, ItemUpdateDto itemDto, Long ownerId);

    void delete(Long id);

    List<ItemDto> search(String text);
}
