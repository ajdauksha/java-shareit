package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequestDto createBookingRequestDto() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        return dto;
    }

    private BookingDto createBookingDto() {
        UserDto booker = new UserDto();
        booker.setId(1L);
        booker.setName("Booker");
        booker.setEmail("booker@email.com");

        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);

        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setStatus(Booking.Status.WAITING);
        dto.setBooker(booker);
        dto.setItem(item);
        return dto;
    }

    @Test
    void create() throws Exception {
        BookingRequestDto requestDto = createBookingRequestDto();
        BookingDto responseDto = createBookingDto();

        when(bookingService.create(any(BookingRequestDto.class), anyLong())).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L));
    }

    @Test
    void createWithoutUserIdHeader() throws Exception {
        BookingRequestDto requestDto = createBookingRequestDto();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateStatus() throws Exception {
        BookingDto responseDto = createBookingDto();
        responseDto.setStatus(Booking.Status.APPROVED);

        when(bookingService.updateStatus(anyLong(), anyBoolean(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updateStatusWithInvalidBookingId() throws Exception {
        when(bookingService.updateStatus(anyLong(), anyBoolean(), anyLong()))
                .thenThrow(new IllegalArgumentException("Бронирование не найдено"));

        mockMvc.perform(patch("/bookings/999")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateStatusWithoutApprovedParam() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getById() throws Exception {
        BookingDto responseDto = createBookingDto();

        when(bookingService.getById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L));
    }

    @Test
    void getByIdWithInvalidId() throws Exception {
        when(bookingService.getById(anyLong(), anyLong()))
                .thenThrow(new IllegalArgumentException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getByIdWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingsByBookerId() throws Exception {
        BookingDto bookingDto = createBookingDto();
        List<BookingDto> bookings = List.of(bookingDto);

        when(bookingService.getBookingsByBookerId(anyLong(), anyString())).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].booker.id").value(1L));
    }

    @Test
    void getBookingsByBookerIdWithDefaultState() throws Exception {
        BookingDto bookingDto = createBookingDto();
        List<BookingDto> bookings = List.of(bookingDto);

        when(bookingService.getBookingsByBookerId(anyLong(), eq("ALL"))).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getBookingsByBookerIdWithDifferentStates() throws Exception {
        BookingDto bookingDto = createBookingDto();
        List<BookingDto> bookings = List.of(bookingDto);

        when(bookingService.getBookingsByBookerId(anyLong(), anyString())).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "PAST"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByBookerIdWithInvalidState() throws Exception {
        when(bookingService.getBookingsByBookerId(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Unknown state: INVALID"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingsByOwnerId() throws Exception {
        BookingDto bookingDto = createBookingDto();
        List<BookingDto> bookings = List.of(bookingDto);

        when(bookingService.getBookingsByOwnerId(anyLong(), anyString())).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].item.id").value(1L));
    }

    @Test
    void getBookingsByOwnerIdWithDefaultState() throws Exception {
        BookingDto bookingDto = createBookingDto();
        List<BookingDto> bookings = List.of(bookingDto);

        when(bookingService.getBookingsByOwnerId(anyLong(), eq("ALL"))).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getBookingsByOwnerIdWithDifferentStates() throws Exception {
        BookingDto bookingDto = createBookingDto();
        List<BookingDto> bookings = List.of(bookingDto);

        when(bookingService.getBookingsByOwnerId(anyLong(), anyString())).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "PAST"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByOwnerIdWithInvalidState() throws Exception {
        when(bookingService.getBookingsByOwnerId(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Unknown state: INVALID"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingsByOwnerIdWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings/owner"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateStatusWithInvalidUserId() throws Exception {
        when(bookingService.updateStatus(anyLong(), anyBoolean(), anyLong()))
                .thenThrow(new IllegalArgumentException("Пользователь не найден"));

        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 999L))
                .andExpect(status().isInternalServerError());
    }
}
