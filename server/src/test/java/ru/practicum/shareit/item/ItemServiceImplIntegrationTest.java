package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private ItemDto createItemDto(String name, String description, Boolean available) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }

    private Item createItem(String name, String description, boolean available, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Test
    void createShouldCreateItemSuccessfully() {
        User owner = createUser("Owner", "owner@email.com");
        ItemDto itemDto = createItemDto("Item", "Description", true);

        ItemDto result = itemService.create(itemDto, owner.getId());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Item", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void createShouldThrowExceptionWhenUserNotFound() {
        ItemDto itemDto = createItemDto("Item", "Description", true);
        Long nonExistentUserId = 999L;

        assertThrows(NoSuchElementException.class, () -> itemService.create(itemDto, nonExistentUserId));
    }

    @Test
    void getByIdShouldReturnItemSuccessfully() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = createItem("Item", "Description", true, owner);

        ItemDto result = itemService.getById(item.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals("Item", result.getName());
        assertEquals("Description", result.getDescription());
    }

    @Test
    void getByIdShouldThrowExceptionWhenItemNotFound() {
        Long nonExistentItemId = 999L;

        assertThrows(NoSuchElementException.class, () -> itemService.getById(nonExistentItemId));
    }

    @Test
    void getAllByOwnerIdShouldReturnItemsForOwner() {
        User owner = createUser("Owner", "owner@email.com");
        Item item1 = createItem("Item1", "Description1", true, owner);
        Item item2 = createItem("Item2", "Description2", true, owner);

        List<ItemDto> result = itemService.getAllByOwnerId(owner.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item1")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item2")));
    }

    @Test
    void getAllByOwnerIdShouldReturnEmptyListForUserWithoutItems() {
        User owner = createUser("Owner", "owner@email.com");

        List<ItemDto> result = itemService.getAllByOwnerId(owner.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllByOwnerIdShouldThrowExceptionWhenUserNotFound() {
        Long nonExistentUserId = 999L;

        assertThrows(NoSuchElementException.class, () -> itemService.getAllByOwnerId(nonExistentUserId));
    }

    @Test
    void updateShouldUpdateItemSuccessfully() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = createItem("OldName", "OldDescription", true, owner);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("NewName");
        updateDto.setDescription("NewDescription");
        updateDto.setAvailable(false);

        ItemDto result = itemService.update(item.getId(), updateDto, owner.getId());

        assertNotNull(result);
        assertEquals("NewName", result.getName());
        assertEquals("NewDescription", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateShouldUpdatePartialFields() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = createItem("Item", "Description", true, owner);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("UpdatedName");

        ItemDto result = itemService.update(item.getId(), updateDto, owner.getId());

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateShouldThrowExceptionWhenItemNotFound() {
        User owner = createUser("Owner", "owner@email.com");
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("NewName");

        Long nonExistentItemId = 999L;

        assertThrows(NoSuchElementException.class, () -> itemService.update(nonExistentItemId, updateDto, owner.getId()));
    }

    @Test
    void updateShouldThrowExceptionWhenNotOwner() {
        User owner = createUser("Owner", "owner@email.com");
        User otherUser = createUser("Other", "other@email.com");
        Item item = createItem("Item", "Description", true, owner);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("NewName");

        assertThrows(ForbiddenException.class, () -> itemService.update(item.getId(), updateDto, otherUser.getId()));
    }

    @Test
    void deleteShouldDeleteItemSuccessfully() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = createItem("Item", "Description", true, owner);

        assertDoesNotThrow(() -> itemService.delete(item.getId()));
        assertThrows(NoSuchElementException.class, () -> itemService.getById(item.getId()));
    }

    @Test
    void searchShouldReturnMatchingItems() {
        User owner = createUser("Owner", "owner@email.com");
        Item item1 = createItem("Drill", "Powerful drill", true, owner);
        Item item2 = createItem("Hammer", "Heavy hammer", true, owner);
        Item item3 = createItem("Saw", "Wood saw", false, owner);

        List<ItemDto> result = itemService.search("drill");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void searchShouldReturnOnlyAvailableItems() {
        User owner = createUser("Owner", "owner@email.com");
        Item item1 = createItem("Drill", "Powerful drill", true, owner);
        Item item2 = createItem("Broken Drill", "Not working", false, owner);

        List<ItemDto> result = itemService.search("drill");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void searchShouldReturnEmptyListForBlankText() {
        User owner = createUser("Owner", "owner@email.com");
        createItem("Drill", "Powerful drill", true, owner);

        List<ItemDto> result1 = itemService.search("");
        List<ItemDto> result2 = itemService.search("   ");

        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
    }

    @Test
    void searchShouldReturnEmptyListForNoMatches() {
        User owner = createUser("Owner", "owner@email.com");
        createItem("Drill", "Powerful drill", true, owner);

        List<ItemDto> result = itemService.search("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addCommentShouldAddCommentSuccessfully() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(ru.practicum.shareit.booking.model.Booking.Status.APPROVED);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        CommentDto result = itemService.addComment(item.getId(), commentDto, booker.getId());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
    }

    @Test
    void addCommentShouldThrowExceptionWhenItemNotFound() {
        User booker = createUser("Booker", "booker@email.com");
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        Long nonExistentItemId = 999L;

        assertThrows(NoSuchElementException.class, () ->
                itemService.addComment(nonExistentItemId, commentDto, booker.getId()));
    }

    @Test
    void addCommentShouldThrowExceptionWhenUserNotFound() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = createItem("Item", "Description", true, owner);
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        Long nonExistentUserId = 999L;

        assertThrows(NoSuchElementException.class, () ->
                itemService.addComment(item.getId(), commentDto, nonExistentUserId));
    }

    @Test
    void addCommentShouldThrowExceptionWhenUserNeverBookedItem() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        assertThrows(IllegalArgumentException.class, () ->
                itemService.addComment(item.getId(), commentDto, booker.getId()));
    }

    @Test
    void addCommentShouldThrowExceptionWhenBookingNotCompleted() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(ru.practicum.shareit.booking.model.Booking.Status.APPROVED);
        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        assertThrows(IllegalArgumentException.class, () ->
                itemService.addComment(item.getId(), commentDto, booker.getId()));
    }

    @Test
    void getByIdShouldIncludeComments() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        ItemDto result = itemService.getById(item.getId());

        assertNotNull(result);
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals("Test comment", result.getComments().get(0).getText());
    }

    @Test
    void getAllByOwnerIdShouldIncludeBookingInfoAndComments() {
        User owner = createUser("Owner", "owner@email.com");
        User booker = createUser("Booker", "booker@email.com");
        Item item = createItem("Item", "Description", true, owner);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(ru.practicum.shareit.booking.model.Booking.Status.APPROVED);
        bookingRepository.save(booking);

        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        List<ItemDto> result = itemService.getAllByOwnerId(owner.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getComments());
        assertEquals(1, result.get(0).getComments().size());
    }
}
