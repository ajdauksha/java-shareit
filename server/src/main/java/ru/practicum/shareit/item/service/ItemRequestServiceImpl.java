package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long requestorId) {
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + requestorId + " не найден"));

        ItemRequest request = new ItemRequest();
        request.setDescription(itemRequestDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long requestorId) {
        userRepository.findById(requestorId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + requestorId + " не найден"));

        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(requestorId);
        return requests.stream()
                .map(this::enrichWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + userId + " не найден"));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pageable);

        return requests.stream()
                .map(this::enrichWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + userId + " не найден"));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос с id " + requestId + " не найден"));

        return enrichWithItems(request);
    }

    private ItemRequestDto enrichWithItems(ItemRequest request) {
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        List<Item> items = itemRepository.findByRequestId(request.getId());

        List<ItemResponseDto> itemDtos = items.stream()
                .map(item -> {
                    ItemResponseDto itemDto = new ItemResponseDto();
                    itemDto.setId(item.getId());
                    itemDto.setName(item.getName());
                    itemDto.setDescription(item.getDescription());
                    itemDto.setAvailable(item.getAvailable());
                    itemDto.setOwnerId(item.getOwner().getId());
                    itemDto.setRequestId(item.getRequestId());
                    return itemDto;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}
