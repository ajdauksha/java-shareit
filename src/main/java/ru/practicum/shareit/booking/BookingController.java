package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(
            @Valid @RequestBody BookingRequestDto bookingDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long bookerId) {
        return bookingService.create(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(
            @PathVariable @Positive Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        return bookingService.updateStatus(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(
            @PathVariable @Positive Long bookingId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByBookerId(
            @RequestHeader("X-Sharer-User-Id") @Positive Long bookerId,
            @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getBookingsByBookerId(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwnerId(
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
            @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getBookingsByOwnerId(ownerId, state);
    }
}
