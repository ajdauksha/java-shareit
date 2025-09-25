package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item createItem(String name, String description, boolean available, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private BookingRequestDto createBookingRequestDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    @Test
    void getBookingsByBookerIdShouldReturnCurrentBookings() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto bookingRequest = createBookingRequestDto(item.getId(), start, end);
        bookingService.create(bookingRequest, booker.getId());

        List<BookingDto> result = bookingService.getBookingsByBookerId(booker.getId(), "CURRENT");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getBookingsByBookerIdShouldReturnPastBookings() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        BookingRequestDto bookingRequest = createBookingRequestDto(item.getId(), start, end);
        bookingService.create(bookingRequest, booker.getId());

        List<BookingDto> result = bookingService.getBookingsByBookerId(booker.getId(), "PAST");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getBookingsByOwnerIdShouldReturnCurrentBookings() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto bookingRequest = createBookingRequestDto(item.getId(), start, end);
        bookingService.create(bookingRequest, booker.getId());

        List<BookingDto> result = bookingService.getBookingsByOwnerId(owner.getId(), "CURRENT");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getBookingsByOwnerIdShouldReturnPastBookings() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        BookingRequestDto bookingRequest = createBookingRequestDto(item.getId(), start, end);
        bookingService.create(bookingRequest, booker.getId());

        List<BookingDto> result = bookingService.getBookingsByOwnerId(owner.getId(), "PAST");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getBookingsByOwnerIdShouldReturnWaitingBookings() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingRequestDto bookingRequest = createBookingRequestDto(item.getId(), start, end);
        bookingService.create(bookingRequest, booker.getId());

        List<BookingDto> result = bookingService.getBookingsByOwnerId(owner.getId(), "WAITING");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Booking.Status.WAITING, result.get(0).getStatus());
    }

    @Test
    void getBookingsByOwnerIdShouldReturnRejectedBookings() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingRequestDto bookingRequest = createBookingRequestDto(item.getId(), start, end);
        BookingDto createdBooking = bookingService.create(bookingRequest, booker.getId());
        bookingService.updateStatus(createdBooking.getId(), false, owner.getId());

        List<BookingDto> result = bookingService.getBookingsByOwnerId(owner.getId(), "REJECTED");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Booking.Status.REJECTED, result.get(0).getStatus());
    }

    @Test
    void createShouldHandleEqualStartAndEndDates() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        BookingRequestDto bookingRequest = createBookingRequestDto(item.getId(), sameTime, sameTime);

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.create(bookingRequest, booker.getId()));
    }

    @Test
    void getByIdShouldThrowExceptionWhenBookingNotFound() {
        User user = createUser("User", "user@email.com");

        assertThrows(NoSuchElementException.class, () ->
                bookingService.getById(999L, user.getId()));
    }

    @Test
    void updateStatusShouldThrowExceptionWhenBookingNotFound() {
        User user = createUser("User", "user@email.com");

        assertThrows(NoSuchElementException.class, () ->
                bookingService.updateStatus(999L, true, user.getId()));
    }

    @Test
    void getBookingsByBookerIdShouldThrowExceptionWhenUserNotFound() {
        assertThrows(NoSuchElementException.class, () ->
                bookingService.getBookingsByBookerId(999L, "ALL"));
    }

    @Test
    void getBookingsByOwnerIdShouldThrowExceptionWhenUserNotFound() {
        assertThrows(NoSuchElementException.class, () ->
                bookingService.getBookingsByOwnerId(999L, "ALL"));
    }

    @Test
    void getBookingsByBookerIdShouldReturnEmptyListForUserWithoutBookings() {
        User booker = createUser("Booker", "booker@email.com");

        List<BookingDto> result = bookingService.getBookingsByBookerId(booker.getId(), "ALL");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getBookingsByOwnerIdShouldReturnEmptyListForUserWithoutItems() {
        User owner = createUser("Owner", "owner@email.com");

        List<BookingDto> result = bookingService.getBookingsByOwnerId(owner.getId(), "ALL");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createShouldPersistBookingCorrectly() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingRequestDto bookingRequest = createBookingRequestDto(item.getId(), start, end);

        BookingDto result = bookingService.create(bookingRequest, booker.getId());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(start, result.getStart());
        assertEquals(end, result.getEnd());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(Booking.Status.WAITING, result.getStatus());
    }
}