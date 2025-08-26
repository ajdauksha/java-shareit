package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + ownerId + " не найден"));

        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Вещь с id " + id + " не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwnerId(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + ownerId + " не найден"));

        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(Long id, ItemUpdateDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Вещь с id " + id + " не найдена"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.update(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
