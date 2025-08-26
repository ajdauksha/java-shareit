package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Item save(Item item) {
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item update(Item item) {
        if (items.containsKey(item.getId())) {
            items.put(item.getId(), item);
            return item;
        }
        throw new NoSuchElementException("Вещь с id " + item.getId() + " не найдена");
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> containsIgnoreCase(item.getName(), searchText) ||
                        containsIgnoreCase(item.getDescription(), searchText))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source != null && source.toLowerCase().contains(target);
    }
}
