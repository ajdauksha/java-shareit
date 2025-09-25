package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user1;
    private User user2;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@email.com");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@email.com");
        user2 = userRepository.save(user2);

        itemRequest1 = new ItemRequest();
        itemRequest1.setDescription("test_1");
        itemRequest1.setRequestor(user1);
        itemRequest1.setCreated(LocalDateTime.now().minusDays(1));
        itemRequest1 = itemRequestRepository.save(itemRequest1);

        itemRequest2 = new ItemRequest();
        itemRequest2.setDescription("test_2");
        itemRequest2.setRequestor(user2);
        itemRequest2.setCreated(LocalDateTime.now());
        itemRequest2 = itemRequestRepository.save(itemRequest2);
    }

    @Test
    void createShouldCreateItemRequest() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("test_3");

        ItemRequestDto result = itemRequestService.create(requestDto, user1.getId());

        assertNotNull(result.getId());
        assertEquals("test_3", result.getDescription());
        assertNotNull(result.getCreated());
        assertNull(result.getItems());
    }

    @Test
    void createShouldThrowExceptionWhenUserNotFound() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("test_3");

        assertThrows(NoSuchElementException.class,
                () -> itemRequestService.create(requestDto, 999L));
    }

    @Test
    void getOwnRequestsShouldReturnUserRequests() {
        List<ItemRequestDto> result = itemRequestService.getOwnRequests(user1.getId());

        assertEquals(1, result.size());
        assertEquals("test_1", result.get(0).getDescription());
    }

    @Test
    void getOwnRequestsShouldReturnEmptyListWhenNoRequests() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@email.com");
        newUser = userRepository.save(newUser);

        List<ItemRequestDto> result = itemRequestService.getOwnRequests(newUser.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getOwnRequestsShouldThrowExceptionWhenUserNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> itemRequestService.getOwnRequests(999L));
    }

    @Test
    void getAllRequestsShouldReturnOtherUsersRequests() {
        List<ItemRequestDto> result = itemRequestService.getAllRequests(user1.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals("test_2", result.get(0).getDescription());
    }

    @Test
    void getAllRequestsShouldReturnEmptyListWhenNoOtherRequests() {
        List<ItemRequestDto> result = itemRequestService.getAllRequests(user2.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals("test_1", result.get(0).getDescription());
    }

    @Test
    void getAllRequestsShouldRespectPagination() {
        for (int i = 0; i < 5; i++) {
            ItemRequest request = new ItemRequest();
            request.setDescription("Запрос " + i);
            request.setRequestor(user2);
            request.setCreated(LocalDateTime.now().plusHours(i));
            itemRequestRepository.save(request);
        }

        List<ItemRequestDto> result = itemRequestService.getAllRequests(user1.getId(), 0, 3);

        assertEquals(3, result.size());
    }

    @Test
    void getAllRequestsShouldThrowExceptionWhenUserNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> itemRequestService.getAllRequests(999L, 0, 10));
    }

    @Test
    void getByIdShouldReturnRequestWithItems() {
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(user2);
        item.setRequestId(itemRequest1.getId());
        itemRepository.save(item);

        ItemRequestDto result = itemRequestService.getById(itemRequest1.getId(), user1.getId());

        assertNotNull(result);
        assertEquals("test_1", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Дрель", result.getItems().get(0).getName());
    }

    @Test
    void getByIdShouldReturnRequestWithoutItemsWhenNoItems() {
        ItemRequestDto result = itemRequestService.getById(itemRequest1.getId(), user1.getId());

        assertNotNull(result);
        assertEquals("test_1", result.getDescription());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void getByIdShouldThrowExceptionWhenRequestNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> itemRequestService.getById(999L, user1.getId()));
    }

    @Test
    void getByIdShouldThrowExceptionWhenUserNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> itemRequestService.getById(itemRequest1.getId(), 999L));
    }

    @Test
    void getOwnRequestsShouldReturnRequestsOrderedByCreatedDesc() {
        ItemRequest newRequest = new ItemRequest();
        newRequest.setDescription("Новый запрос");
        newRequest.setRequestor(user1);
        newRequest.setCreated(LocalDateTime.now().plusHours(1));
        itemRequestRepository.save(newRequest);

        List<ItemRequestDto> result = itemRequestService.getOwnRequests(user1.getId());

        assertEquals(2, result.size());
        assertEquals("Новый запрос", result.get(0).getDescription());
        assertEquals("test_1", result.get(1).getDescription());
    }

    @Test
    void getAllRequestsShouldReturnRequestsOrderedByCreatedDesc() {
        ItemRequest newRequest = new ItemRequest();
        newRequest.setDescription("Еще один запрос");
        newRequest.setRequestor(user2);
        newRequest.setCreated(LocalDateTime.now().plusHours(1));
        itemRequestRepository.save(newRequest);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(user1.getId(), 0, 10);

        assertEquals(2, result.size());
        assertEquals("Еще один запрос", result.get(0).getDescription());
        assertEquals("test_2", result.get(1).getDescription());
    }

    @Test
    void createShouldSetCorrectTimestamp() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Тестовый запрос");
        LocalDateTime beforeCreate = LocalDateTime.now();

        ItemRequestDto result = itemRequestService.create(requestDto, user1.getId());

        assertNotNull(result.getCreated());
        assertTrue(result.getCreated().isAfter(beforeCreate.minusSeconds(1)));
        assertTrue(result.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
