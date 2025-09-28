package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return entityManager.persistAndFlush(user);
    }

    private Item createItem(String name, String description, boolean available, User owner, Long requestId) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequestId(requestId);
        return entityManager.persistAndFlush(item);
    }

    @Test
    void findByOwnerId() {
        User owner = createUser("Owner", "owner@email.com");
        Item item1 = createItem("Item1", "Description1", true, owner, null);
        Item item2 = createItem("Item2", "Description2", true, owner, null);

        List<Item> result = itemRepository.findByOwnerId(owner.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item1")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item2")));
    }

    @Test
    void findByOwnerIdShouldReturnEmptyListForNonExistentOwner() {
        List<Item> result = itemRepository.findByOwnerId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByOwnerIdShouldReturnOnlyOwnersItems() {
        User owner1 = createUser("Owner1", "owner1@email.com");
        User owner2 = createUser("Owner2", "owner2@email.com");
        Item item1 = createItem("Item1", "Description1", true, owner1, null);
        createItem("Item2", "Description2", true, owner2, null);

        List<Item> result = itemRepository.findByOwnerId(owner1.getId());

        assertEquals(1, result.size());
        assertEquals("Item1", result.get(0).getName());
    }

    @Test
    void search() {
        User owner = createUser("Owner", "owner@email.com");
        Item drill = createItem("Drill", "Powerful electric drill", true, owner, null);
        Item hammer = createItem("Hammer", "Heavy construction hammer", true, owner, null);
        Item brokenDrill = createItem("Broken Drill", "Not working drill", false, owner, null);

        List<Item> result = itemRepository.search("drill");

        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void searchShouldBeCaseInsensitive() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = createItem("DRILL", "Powerful tool", true, owner, null);

        List<Item> result1 = itemRepository.search("drill");
        List<Item> result2 = itemRepository.search("DRILL");
        List<Item> result3 = itemRepository.search("DrIlL");

        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());
        assertEquals("DRILL", result1.get(0).getName());
    }

    @Test
    void searchShouldMatchNameAndDescription() {
        User owner = createUser("Owner", "owner@email.com");
        Item nameMatch = createItem("Drill Machine", "Tool for drilling", true, owner, null);
        Item descriptionMatch = createItem("Tool", "Electric drill for construction", true, owner, null);

        List<Item> result = itemRepository.search("drill");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Drill Machine")));
        assertTrue(result.stream().anyMatch(item -> item.getDescription().equals("Electric drill for construction")));
    }

    @Test
    void searchShouldReturnEmptyListForNoMatches() {
        User owner = createUser("Owner", "owner@email.com");
        createItem("Hammer", "Heavy tool", true, owner, null);

        List<Item> result = itemRepository.search("drill");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchShouldReturnEmptyListForNullText() {
        User owner = createUser("Owner", "owner@email.com");
        createItem("Drill", "Powerful tool", true, owner, null);

        List<Item> result = itemRepository.search(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchShouldNotReturnUnavailableItems() {
        User owner = createUser("Owner", "owner@email.com");
        Item availableDrill = createItem("Drill", "Powerful tool", true, owner, null);
        Item unavailableDrill = createItem("Old Drill", "Broken tool", false, owner, null);

        List<Item> result = itemRepository.search("drill");

        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
        assertTrue(result.get(0).getAvailable());
    }

    @Test
    void searchShouldHandlePartialMatches() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = createItem("Electric Drill Machine", "Powerful tool for drilling", true, owner, null);

        List<Item> result = itemRepository.search("drill");

        assertEquals(1, result.size());
        assertEquals("Electric Drill Machine", result.get(0).getName());
    }

    @Test
    void saveAndFindById() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        Item foundItem = itemRepository.findById(savedItem.getId()).orElse(null);

        assertNotNull(foundItem);
        assertEquals("Test Item", foundItem.getName());
        assertEquals("Test Description", foundItem.getDescription());
        assertTrue(foundItem.getAvailable());
        assertEquals(owner.getId(), foundItem.getOwner().getId());
    }

    @Test
    void deleteById() {
        User owner = createUser("Owner", "owner@email.com");
        Item item = createItem("Item", "Description", true, owner, null);

        itemRepository.deleteById(item.getId());
        Item foundItem = itemRepository.findById(item.getId()).orElse(null);

        assertNull(foundItem);
    }

    @Test
    void findAll() {
        User owner = createUser("Owner", "owner@email.com");
        Item item1 = createItem("Item1", "Description1", true, owner, null);
        Item item2 = createItem("Item2", "Description2", true, owner, null);

        List<Item> result = itemRepository.findAll();

        assertTrue(result.size() >= 2);
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item1")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item2")));
    }
}
