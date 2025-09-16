package ru.practicum.shareit.item.model;

import lombok.Data;

@Data
public class BookingInfo {
    private Long id;
    private Long bookerId;

    public BookingInfo(Long id, Long bookerId) {
        this.id = id;
        this.bookerId = bookerId;
    }

}
