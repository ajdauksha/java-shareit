package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingInfo {
    private Long id;
    private Long bookerId;

    public BookingInfo(Long id, Long bookerId) {
        this.id = id;
        this.bookerId = bookerId;
    }

}
