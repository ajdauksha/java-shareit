package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final Sort sort = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDto create(BookingRequestDto bookingDto, Long bookerId) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + bookerId + " не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь с id " + bookingDto.getItemId() + " не найдена"));

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new IllegalArgumentException("Владелец не может бронировать свою вещь");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().isEqual(bookingDto.getStart())) {
            throw new IllegalArgumentException("Дата окончания должна быть после даты начала");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Booking.Status.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto updateStatus(Long bookingId, Boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Только владелец вещи может подтверждать бронирование");
        }

        if (!booking.getStatus().equals(Booking.Status.WAITING)) {
            throw new IllegalArgumentException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? Booking.Status.APPROVED : Booking.Status.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Просмотр бронирования доступен только автору или владельцу вещи");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBookerId(Long bookerId, String state) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + bookerId + " не найден"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByBookerId(bookerId, sort);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                        bookerId, now, now, sort);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndBefore(bookerId, now, sort);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartAfter(bookerId, now, sort);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatus(
                        bookerId, Booking.Status.WAITING, sort);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatus(
                        bookerId, Booking.Status.REJECTED, sort);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByOwnerId(Long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id " + ownerId + " не найден"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerId(ownerId, sort);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                        ownerId, now, now, sort);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndBefore(ownerId, now, sort);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerIdAndStartAfter(ownerId, now, sort);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(
                        ownerId, Booking.Status.WAITING, sort);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(
                        ownerId, Booking.Status.REJECTED, sort);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}
