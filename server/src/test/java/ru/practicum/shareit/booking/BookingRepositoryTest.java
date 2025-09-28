package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return entityManager.persistAndFlush(user);
    }

    private Item createItem(String name, String description, boolean available, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return entityManager.persistAndFlush(item);
    }

    private Booking createBooking(LocalDateTime start, LocalDateTime end, Item item, User booker, Booking.Status status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return entityManager.persistAndFlush(booking);
    }

    @Test
    void findByBookerId() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, Booking.Status.WAITING
        );

        List<Booking> result = bookingRepository.findByBookerId(booker.getId(), Sort.by(Sort.Direction.DESC, "start"));

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByItemOwnerId() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, Booking.Status.WAITING
        );

        List<Booking> result = bookingRepository.findByItemOwnerId(owner.getId(), Sort.by(Sort.Direction.DESC, "start"));

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByItemIdAndStatus() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, Booking.Status.APPROVED
        );

        List<Booking> result = bookingRepository.findByItemIdAndStatus(item.getId(), Booking.Status.APPROVED);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByBookerIdAndEndBefore() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item, booker, Booking.Status.APPROVED
        );

        List<Booking> result = bookingRepository.findByBookerIdAndEndBefore(
                booker.getId(), LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start")
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByBookerIdAndStartAfter() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, Booking.Status.APPROVED
        );

        List<Booking> result = bookingRepository.findByBookerIdAndStartAfter(
                booker.getId(), LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start")
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByBookerIdAndStartBeforeAndEndAfter() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item, booker, Booking.Status.APPROVED
        );

        List<Booking> result = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                booker.getId(), LocalDateTime.now(), LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start")
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByBookerIdAndStatus() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, Booking.Status.WAITING
        );

        List<Booking> result = bookingRepository.findByBookerIdAndStatus(
                booker.getId(), Booking.Status.WAITING, Sort.by(Sort.Direction.DESC, "start")
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByItemOwnerIdAndEndBefore() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item, booker, Booking.Status.APPROVED
        );

        List<Booking> result = bookingRepository.findByItemOwnerIdAndEndBefore(
                owner.getId(), LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start")
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByItemOwnerIdAndStartAfter() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, Booking.Status.APPROVED
        );

        List<Booking> result = bookingRepository.findByItemOwnerIdAndStartAfter(
                owner.getId(), LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start")
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByItemOwnerIdAndStartBeforeAndEndAfter() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item, booker, Booking.Status.APPROVED
        );

        List<Booking> result = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                owner.getId(), LocalDateTime.now(), LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "start")
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByBookerIdAndItemIdAndEndBefore() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item, booker, Booking.Status.APPROVED
        );

        Optional<Booking> result = bookingRepository.findByBookerIdAndItemIdAndEndBefore(
                booker.getId(), item.getId(), LocalDateTime.now()
        );

        assertTrue(result.isPresent());
        assertEquals(booking.getId(), result.get().getId());
    }

    @Test
    void findByItemOwnerIdAndStatus() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking booking = createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, Booking.Status.REJECTED
        );

        List<Booking> result = bookingRepository.findByItemOwnerIdAndStatus(
                owner.getId(), Booking.Status.REJECTED, Sort.by(Sort.Direction.DESC, "start")
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findLastBooking() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Booking pastBooking = createBooking(
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                item, booker, Booking.Status.APPROVED
        );
        Booking currentBooking = createBooking(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item, booker, Booking.Status.APPROVED
        );

        Optional<Booking> result = bookingRepository.findLastBooking(item.getId(), LocalDateTime.now());

        assertTrue(result.isPresent());
        assertEquals(pastBooking.getId(), result.get().getId());
    }

    @Test
    void findByBookerIdShouldReturnEmptyListForNonExistentBooker() {
        List<Booking> result = bookingRepository.findByBookerId(999L, Sort.by(Sort.Direction.DESC, "start"));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByItemOwnerIdShouldReturnEmptyListForNonExistentOwner() {
        List<Booking> result = bookingRepository.findByItemOwnerId(999L, Sort.by(Sort.Direction.DESC, "start"));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByBookerIdAndItemIdAndEndBeforeShouldReturnEmptyForNonExistentBooking() {
        Optional<Booking> result = bookingRepository.findByBookerIdAndItemIdAndEndBefore(999L, 999L, LocalDateTime.now());

        assertFalse(result.isPresent());
    }

    @Test
    void findLastBookingShouldReturnEmptyWhenNoPastBookings() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        createBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, Booking.Status.APPROVED
        );

        Optional<Booking> result = bookingRepository.findLastBooking(item.getId(), LocalDateTime.now());

        assertFalse(result.isPresent());
    }

    @Test
    void findNextBookingShouldReturnEmptyWhenNoFutureBookings() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);
        createBooking(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item, booker, Booking.Status.APPROVED
        );

        Optional<Booking> result = bookingRepository.findNextBooking(item.getId(), LocalDateTime.now());

        assertFalse(result.isPresent());
    }
}
