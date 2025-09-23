package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.BookingInfo;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
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

        ItemDto itemDto = ItemMapper.toItemDto(item);
        addComments(itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllByOwnerId(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + ownerId + " не найден"));

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        itemDtos.forEach(this::addBookingInfo);
        itemDtos.forEach(this::addComments);

        return itemDtos;
    }

    @Override
    @Transactional
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

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long authorId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с id " + itemId + " не найдена"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + authorId + " не найден"));

        boolean hasBooked = bookingRepository.findByBookerIdAndItemIdAndEndBefore(
                authorId, itemId, LocalDateTime.now()).isPresent();

        if (!hasBooked) {
            throw new IllegalArgumentException("Пользователь не брал вещь в аренду или аренда еще не завершена");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }

    private void addBookingInfo(ItemDto itemDto) {
        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookingRepository.findLastBooking(itemDto.getId(), now);
        Optional<Booking> nextBooking = bookingRepository.findNextBooking(itemDto.getId(), now);

        lastBooking.ifPresent(booking -> itemDto.setLastBooking(new BookingInfo(booking.getId(), booking.getBooker().getId())));
        nextBooking.ifPresent(booking -> itemDto.setNextBooking(new BookingInfo(booking.getId(), booking.getBooker().getId())));
    }

    private void addComments(ItemDto itemDto) {
        List<Comment> comments = commentRepository.findByItemId(itemDto.getId());
        itemDto.setComments(comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
    }
}
