package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return entityManager.persistAndFlush(user);
    }

    private ItemRequest createItemRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return entityManager.persistAndFlush(request);
    }

    @Test
    void findByRequestorIdOrderByCreatedDesc() {
        User requestor = createUser("Requestor", "requestor@email.com");
        ItemRequest request1 = createItemRequest("Need item 1", requestor);
        ItemRequest request2 = createItemRequest("Need item 2", requestor);

        User otherUser = createUser("Other", "other@email.com");
        createItemRequest("Other request", otherUser);

        List<ItemRequest> result = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestor.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(req -> req.getRequestor().getId().equals(requestor.getId())));
        assertTrue(result.get(0).getCreated().isAfter(result.get(1).getCreated()) ||
                result.get(0).getCreated().isEqual(result.get(1).getCreated()));
    }

    @Test
    void findByRequestorIdOrderByCreatedDescShouldReturnEmptyListForNonExistentUser() {
        List<ItemRequest> result = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByRequestorIdOrderByCreatedDescShouldReturnEmptyListForUserWithoutRequests() {
        User user = createUser("User", "user@email.com");

        List<ItemRequest> result = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllExceptRequestor() {
        User requestor = createUser("Requestor", "requestor@email.com");
        User otherUser1 = createUser("Other1", "other1@email.com");
        User otherUser2 = createUser("Other2", "other2@email.com");

        createItemRequest("Request from requestor", requestor);
        ItemRequest request1 = createItemRequest("Request from other1", otherUser1);
        ItemRequest request2 = createItemRequest("Request from other2", otherUser2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> result = itemRequestRepository.findAllExceptRequestor(requestor.getId(), pageable);

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(req -> req.getRequestor().getId().equals(requestor.getId())));
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(otherUser1.getId())));
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(otherUser2.getId())));
    }

    @Test
    void findAllExceptRequestor_WithPagination() {
        User requestor = createUser("Requestor", "requestor@email.com");
        User otherUser = createUser("Other", "other@email.com");

        for (int i = 1; i <= 5; i++) {
            createItemRequest("Request " + i, otherUser);
        }

        Pageable firstPage = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> firstPageResult = itemRequestRepository.findAllExceptRequestor(requestor.getId(), firstPage);

        Pageable secondPage = PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> secondPageResult = itemRequestRepository.findAllExceptRequestor(requestor.getId(), secondPage);

        assertEquals(2, firstPageResult.size());
        assertEquals(2, secondPageResult.size());
    }

    @Test
    void findAllExceptRequestorShouldReturnEmptyListWhenOnlyRequestorHasRequests() {
        User requestor = createUser("Requestor", "requestor@email.com");
        createItemRequest("Request 1", requestor);
        createItemRequest("Request 2", requestor);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> result = itemRequestRepository.findAllExceptRequestor(requestor.getId(), pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllExceptRequestorShouldReturnEmptyListForNonExistentRequestor() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> result = itemRequestRepository.findAllExceptRequestor(999L, pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByRequestorIdNotOrderByCreatedDesc() {
        User requestor = createUser("Requestor", "requestor@email.com");
        User otherUser1 = createUser("Other1", "other1@email.com");
        User otherUser2 = createUser("Other2", "other2@email.com");

        createItemRequest("Request from requestor", requestor);
        ItemRequest request1 = createItemRequest("Request from other1", otherUser1);
        ItemRequest request2 = createItemRequest("Request from other2", otherUser2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> result = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(requestor.getId(), pageable);

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(req -> req.getRequestor().getId().equals(requestor.getId())));
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(otherUser1.getId())));
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(otherUser2.getId())));
    }

    @Test
    void findByRequestorIdNotOrderByCreatedDesc_WithPagination() {
        User requestor = createUser("Requestor", "requestor@email.com");
        User otherUser = createUser("Other", "other@email.com");

        for (int i = 1; i <= 5; i++) {
            createItemRequest("Request " + i, otherUser);
        }

        Pageable firstPage = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> firstPageResult = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(requestor.getId(), firstPage);

        Pageable secondPage = PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> secondPageResult = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(requestor.getId(), secondPage);

        assertEquals(3, firstPageResult.size());
        assertEquals(2, secondPageResult.size());
    }

    @Test
    void findByRequestorIdNotOrderByCreatedDescShouldReturnAllRequestsForNonExistentRequestor() {
        User user1 = createUser("User1", "user1@email.com");
        User user2 = createUser("User2", "user2@email.com");

        createItemRequest("Request 1", user1);
        createItemRequest("Request 2", user2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> result = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(999L, pageable);

        assertEquals(2, result.size());
    }

    @Test
    void saveAndFindById() {
        User requestor = createUser("Requestor", "requestor@email.com");
        ItemRequest request = new ItemRequest();
        request.setDescription("Need a new item");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(request);
        ItemRequest foundRequest = itemRequestRepository.findById(savedRequest.getId()).orElse(null);

        assertNotNull(foundRequest);
        assertEquals("Need a new item", foundRequest.getDescription());
        assertEquals(requestor.getId(), foundRequest.getRequestor().getId());
        assertNotNull(foundRequest.getCreated());
    }

    @Test
    void deleteById() {
        User requestor = createUser("Requestor", "requestor@email.com");
        ItemRequest request = createItemRequest("Request to delete", requestor);

        itemRequestRepository.deleteById(request.getId());
        ItemRequest foundRequest = itemRequestRepository.findById(request.getId()).orElse(null);

        assertNull(foundRequest);
    }

    @Test
    void findAll() {
        User requestor1 = createUser("Requestor1", "requestor1@email.com");
        User requestor2 = createUser("Requestor2", "requestor2@email.com");

        createItemRequest("Request 1", requestor1);
        createItemRequest("Request 2", requestor2);

        List<ItemRequest> result = itemRequestRepository.findAll();

        assertTrue(result.size() >= 2);
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(requestor1.getId())));
        assertTrue(result.stream().anyMatch(req -> req.getRequestor().getId().equals(requestor2.getId())));
    }

    @Test
    void findByRequestorIdOrderByCreatedDescShouldMaintainOrder() throws InterruptedException {
        User requestor = createUser("Requestor", "requestor@email.com");

        ItemRequest request1 = createItemRequest("First request", requestor);
        Thread.sleep(10);
        ItemRequest request2 = createItemRequest("Second request", requestor);
        Thread.sleep(10);
        ItemRequest request3 = createItemRequest("Third request", requestor);

        List<ItemRequest> result = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestor.getId());

        assertEquals(3, result.size());
        assertTrue(result.get(0).getCreated().isAfter(result.get(1).getCreated()));
        assertTrue(result.get(1).getCreated().isAfter(result.get(2).getCreated()));
        assertEquals("Third request", result.get(0).getDescription());
        assertEquals("First request", result.get(2).getDescription());
    }

    @Test
    void findAllExceptRequestorShouldOrderByCreatedDesc() throws InterruptedException {
        User requestor = createUser("Requestor", "requestor@email.com");
        User otherUser = createUser("Other", "other@email.com");

        ItemRequest request1 = createItemRequest("Old request", otherUser);
        Thread.sleep(10);
        ItemRequest request2 = createItemRequest("New request", otherUser);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> result = itemRequestRepository.findAllExceptRequestor(requestor.getId(), pageable);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreated().isAfter(result.get(1).getCreated()));
        assertEquals("New request", result.get(0).getDescription());
        assertEquals("Old request", result.get(1).getDescription());
    }

    @Test
    void findByRequestorIdNotOrderByCreatedDescShouldOrderByCreatedDesc() throws InterruptedException {
        User requestor = createUser("Requestor", "requestor@email.com");
        User otherUser = createUser("Other", "other@email.com");

        ItemRequest request1 = createItemRequest("Old request", otherUser);
        Thread.sleep(10);
        ItemRequest request2 = createItemRequest("New request", otherUser);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> result = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(requestor.getId(), pageable);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreated().isAfter(result.get(1).getCreated()));
        assertEquals("New request", result.get(0).getDescription());
        assertEquals("Old request", result.get(1).getDescription());
    }

    @Test
    void findByRequestorIdNotOrderByCreatedDescShouldHandleEmptyPage() {
        User requestor = createUser("Requestor", "requestor@email.com");
        User otherUser = createUser("Other", "other@email.com");
        createItemRequest("Request", otherUser);

        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> result = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(requestor.getId(), pageable);

        assertTrue(result.isEmpty());
    }
}
