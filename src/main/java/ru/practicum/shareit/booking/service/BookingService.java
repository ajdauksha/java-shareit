package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingRequestDto bookingDto, Long bookerId);

    BookingDto updateStatus(Long bookingId, Boolean approved, Long ownerId);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getBookingsByBookerId(Long bookerId, String state);

    List<BookingDto> getBookingsByOwnerId(Long ownerId, String state);
}