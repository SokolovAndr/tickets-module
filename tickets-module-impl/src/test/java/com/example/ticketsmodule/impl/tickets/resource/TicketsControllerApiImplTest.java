package com.example.ticketsmodule.impl.tickets.resource;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchRequest;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchResponse;
import com.example.ticketsmodule.api.model.TicketPatchRequest;
import com.example.ticketsmodule.api.model.TicketsSearchRequest;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketsControllerApiImpl unit tests")
class TicketsControllerApiImplTest {

    @Mock private TicketsService ticketsService;

    @InjectMocks
    private TicketsControllerApiImpl controller;

    private UUID ticketId;
    private CreateTicketResponse response;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        response = new CreateTicketResponse();
    }

    // ---------- createTicket() ----------

    @Nested
    @DisplayName("createTicket()")
    class CreateTicketTests {

        @Test
        @DisplayName("should return 201 Created with body from service")
        void shouldReturn201Created() {
            CreateTicketRequest request = new CreateTicketRequest();
            when(ticketsService.create(request)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.createTicket(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isSameAs(response);
            verify(ticketsService).create(request);
        }

        @Test
        @DisplayName("should propagate exception from service")
        void shouldPropagateException() {
            CreateTicketRequest request = new CreateTicketRequest();
            when(ticketsService.create(request))
                    .thenThrow(new EntityNotFoundException("Route not found"));

            assertThatThrownBy(() -> controller.createTicket(request))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ---------- getAllTickets() ----------

    @Nested
    @DisplayName("getAllTickets()")
    class GetAllTicketsTests {

        @Test
        @DisplayName("should return 200 OK with page from service")
        void shouldReturn200OkWithPage() {
            Pageable pageable = PageRequest.of(0, 10);
            TicketsSearchRequest search = new TicketsSearchRequest();
            Page<CreateTicketResponse> page = new PageImpl<>(List.of(response));
            when(ticketsService.findAll(search, pageable)).thenReturn(page);

            ResponseEntity<Page> result = controller.getAllTickets(pageable, search);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(page);
            verify(ticketsService).findAll(search, pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty page")
        void shouldReturn200OkWithEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CreateTicketResponse> empty = Page.empty();
            when(ticketsService.findAll(null, pageable)).thenReturn(empty);

            ResponseEntity<Page> result = controller.getAllTickets(pageable, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isEmpty();
        }
    }

    // ---------- getTicket() ----------

    @Nested
    @DisplayName("getTicket()")
    class GetTicketTests {

        @Test
        @DisplayName("should return 200 OK with body from service")
        void shouldReturn200Ok() {
            when(ticketsService.findOne(ticketId)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.getTicket(ticketId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
            verify(ticketsService).findOne(ticketId);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException from service")
        void shouldPropagateException() {
            when(ticketsService.findOne(ticketId))
                    .thenThrow(new EntityNotFoundException("Ticket not found"));

            assertThatThrownBy(() -> controller.getTicket(ticketId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ticket not found");
        }
    }

    // ---------- patchTicket() ----------

    @Nested
    @DisplayName("patchTicket()")
    class PatchTicketTests {

        @Test
        @DisplayName("should return 200 OK with patched body")
        void shouldReturn200OkWithPatchedBody() {
            TicketPatchRequest patch = new TicketPatchRequest();
            when(ticketsService.patch(ticketId, patch)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.patchTicket(ticketId, patch);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
            verify(ticketsService).patch(ticketId, patch);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException from service")
        void shouldPropagateException() {
            TicketPatchRequest patch = new TicketPatchRequest();
            when(ticketsService.patch(ticketId, patch))
                    .thenThrow(new EntityNotFoundException("Ticket not found"));

            assertThatThrownBy(() -> controller.patchTicket(ticketId, patch))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ---------- buyTicket() ----------

    @Nested
    @DisplayName("buyTicket()")
    class BuyTicketTests {

        @Test
        @DisplayName("should return 200 OK with purchased ticket")
        void shouldReturn200Ok() {
            when(ticketsService.buyTicket(ticketId)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.buyTicket(ticketId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
            verify(ticketsService).buyTicket(ticketId);
        }

        @Test
        @DisplayName("should propagate IllegalStateException when already purchased")
        void shouldPropagateIllegalState() {
            when(ticketsService.buyTicket(ticketId))
                    .thenThrow(new IllegalStateException("Ticket already purchased"));

            assertThatThrownBy(() -> controller.buyTicket(ticketId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already purchased");
        }
    }

    // ---------- returnTicket() ----------

    @Nested
    @DisplayName("returnTicket()")
    class ReturnTicketTests {

        @Test
        @DisplayName("should return 200 OK with returned ticket")
        void shouldReturn200Ok() {
            when(ticketsService.returnTicket(ticketId)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.returnTicket(ticketId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
            verify(ticketsService).returnTicket(ticketId);
        }

        @Test
        @DisplayName("should propagate IllegalStateException when not purchased")
        void shouldPropagateIllegalState() {
            when(ticketsService.returnTicket(ticketId))
                    .thenThrow(new IllegalStateException("Ticket is not purchased"));

            assertThatThrownBy(() -> controller.returnTicket(ticketId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not purchased");
        }
    }

    // ---------- releaseTicketsBatch() ----------

    @Nested
    @DisplayName("releaseTicketsBatch()")
    class ReleaseTicketsBatchTests {

        @Test
        @DisplayName("should return 200 OK with batch response")
        void shouldReturn200Ok() {
            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            ReleaseTicketsBatchResponse batchResponse = new ReleaseTicketsBatchResponse();
            when(ticketsService.releaseTicketsBatch(request)).thenReturn(batchResponse);

            ResponseEntity<ReleaseTicketsBatchResponse> result =
                    controller.releaseTicketsBatch(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(batchResponse);
            verify(ticketsService).releaseTicketsBatch(request);
        }

        @Test
        @DisplayName("should propagate IllegalStateException when no free seats")
        void shouldPropagateIllegalState() {
            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            when(ticketsService.releaseTicketsBatch(request))
                    .thenThrow(new IllegalStateException("Нет свободных мест"));

            assertThatThrownBy(() -> controller.releaseTicketsBatch(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Нет свободных мест");
        }
    }
}