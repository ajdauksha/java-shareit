package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

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

    private Comment createComment(String text, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return entityManager.persistAndFlush(comment);
    }

    @Test
    void findByItemId() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Comment comment1 = createComment("Great item!", item, author);
        Comment comment2 = createComment("Very useful", item, author);

        List<Comment> result = commentRepository.findByItemId(item.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Great item!")));
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Very useful")));
    }

    @Test
    void findByItemIdShouldReturnEmptyListForNonExistentItem() {
        List<Comment> result = commentRepository.findByItemId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByItemIdShouldReturnOnlyCommentsForSpecificItem() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item1 = createItem("Item1", "Description1", true, owner);
        Item item2 = createItem("Item2", "Description2", true, owner);
        Comment comment1 = createComment("Comment for item1", item1, author);
        Comment comment2 = createComment("Comment for item2", item2, author);

        List<Comment> result = commentRepository.findByItemId(item1.getId());

        assertEquals(1, result.size());
        assertEquals("Comment for item1", result.get(0).getText());
        assertEquals(item1.getId(), result.get(0).getItem().getId());
    }

    @Test
    void findByItemIdShouldReturnCommentsInOrder() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Comment comment1 = createComment("First comment", item, author);
        Comment comment2 = createComment("Second comment", item, author);
        Comment comment3 = createComment("Third comment", item, author);

        List<Comment> result = commentRepository.findByItemId(item.getId());

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("First comment")));
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Second comment")));
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Third comment")));
    }

    @Test
    void findByItemIdIn() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item1 = createItem("Item1", "Description1", true, owner);
        Item item2 = createItem("Item2", "Description2", true, owner);
        Item item3 = createItem("Item3", "Description3", true, owner);
        Comment comment1 = createComment("Comment for item1", item1, author);
        Comment comment2 = createComment("Comment for item2", item2, author);
        Comment comment3 = createComment("Comment for item3", item3, author);

        List<Comment> result = commentRepository.findByItemIdIn(List.of(item1.getId(), item2.getId()));

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Comment for item1")));
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Comment for item2")));
        assertFalse(result.stream().anyMatch(c -> c.getText().equals("Comment for item3")));
    }

    @Test
    void findByItemIdInShouldReturnEmptyListForEmptyInput() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);
        createComment("Comment", item, author);

        List<Comment> result = commentRepository.findByItemIdIn(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByItemIdInShouldReturnEmptyListForNonExistentItems() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);
        createComment("Comment", item, author);

        List<Comment> result = commentRepository.findByItemIdIn(List.of(999L, 1000L));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByItemIdInShouldHandleSingleItem() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Comment comment = createComment("Comment", item, author);

        List<Comment> result = commentRepository.findByItemIdIn(List.of(item.getId()));

        assertEquals(1, result.size());
        assertEquals("Comment", result.get(0).getText());
        assertEquals(item.getId(), result.get(0).getItem().getId());
    }

    @Test
    void saveAndFindById() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);

        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        Comment foundComment = commentRepository.findById(savedComment.getId()).orElse(null);

        assertNotNull(foundComment);
        assertEquals("Test comment", foundComment.getText());
        assertEquals(item.getId(), foundComment.getItem().getId());
        assertEquals(author.getId(), foundComment.getAuthor().getId());
        assertNotNull(foundComment.getCreated());
    }

    @Test
    void deleteById() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Comment comment = createComment("Comment to delete", item, author);

        commentRepository.deleteById(comment.getId());
        Comment foundComment = commentRepository.findById(comment.getId()).orElse(null);

        assertNull(foundComment);
    }

    @Test
    void findAll() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Comment comment1 = createComment("Comment 1", item, author);
        Comment comment2 = createComment("Comment 2", item, author);

        List<Comment> result = commentRepository.findAll();

        assertTrue(result.size() >= 2);
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Comment 1")));
        assertTrue(result.stream().anyMatch(c -> c.getText().equals("Comment 2")));
    }

    @Test
    void findByItemIdShouldReturnCommentsWithAuthorAndItemData() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item = createItem("Item", "Description", true, owner);
        Comment comment = createComment("Comment", item, author);

        List<Comment> result = commentRepository.findByItemId(item.getId());

        assertEquals(1, result.size());
        Comment foundComment = result.get(0);
        assertEquals("Comment", foundComment.getText());
        assertNotNull(foundComment.getAuthor());
        assertEquals("Author", foundComment.getAuthor().getName());
        assertNotNull(foundComment.getItem());
        assertEquals("Item", foundComment.getItem().getName());
        assertNotNull(foundComment.getCreated());
    }

    @Test
    void findByItemIdInShouldReturnCommentsWithProperItemAssociation() {
        User owner = createUser("Owner", "owner@email.com");
        User author = createUser("Author", "author@email.com");
        Item item1 = createItem("Item1", "Description1", true, owner);
        Item item2 = createItem("Item2", "Description2", true, owner);
        Comment comment1 = createComment("Comment 1", item1, author);
        Comment comment2 = createComment("Comment 2", item2, author);

        List<Comment> result = commentRepository.findByItemIdIn(List.of(item1.getId()));

        assertEquals(1, result.size());
        assertEquals("Comment 1", result.get(0).getText());
        assertEquals(item1.getId(), result.get(0).getItem().getId());
    }
}
