package com.example.ticketsmodule.impl.tickets.resource;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchRequest;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchResponse;
import com.example.ticketsmodule.api.model.TicketPatchRequest;
import com.example.ticketsmodule.api.model.TicketsSearchRequest;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.ticket.exception.TicketNotFoundException;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.oauth.service.JwtCurrentUserService;
import com.example.ticketsmodule.impl.tickets.service.TicketPdfService;
import com.example.ticketsmodule.impl.tickets.service.TicketsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketsControllerApiImpl unit tests")
class TicketsControllerApiImplTest {

    @Mock private TicketsService ticketsService;
    @Mock private JwtCurrentUserService jwtCurrentUserService;
    @Mock private TicketPdfService ticketPdfService;

    @InjectMocks
    private TicketsControllerApiImpl controller;

    private UUID ticketId;
    private UUID userId;
    private CreateTicketResponse response;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        userId = UUID.randomUUID();
        response = new CreateTicketResponse();
    }

    @Nested
    @DisplayName("createTicket()")
    class CreateTicketTests {
        @Test
        @DisplayName("should return 201 Created")
        void shouldReturn201Created() {
            CreateTicketRequest request = new CreateTicketRequest();
            when(ticketsService.create(request)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.createTicket(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("getAllTickets()")
    class GetAllTicketsTests {

        @Test
        @DisplayName("should convert Pageable to PageRequest and return Spring Page")
        void shouldConvertPageableToDomainAndBack() {
            Pageable springPageable = org.springframework.data.domain.PageRequest.of(1, 5);
            TicketsSearchRequest search = new TicketsSearchRequest();
            PageResult<CreateTicketResponse> domainResult = PageResult.<CreateTicketResponse>builder()
                    .content(List.of(response))
                    .totalElements(100)
                    .build();

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            when(ticketsService.findAll(eq(search), captor.capture())).thenReturn(domainResult);

            ResponseEntity<Page> result = controller.getAllTickets(springPageable, search);

            assertThat(captor.getValue().getPage()).isEqualTo(1);
            assertThat(captor.getValue().getSize()).isEqualTo(5);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getTotalElements()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("getAllMyTickets()")
    class GetAllMyTicketsTests {

        @Test
        @DisplayName("should return 200 OK with list")
        void shouldReturn200OkWithList() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.findAllMyTickets(userId)).thenReturn(List.of(response));

            ResponseEntity<List<CreateTicketResponse>> result = controller.getAllMyTickets();

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("getTicket() / patchTicket()")
    class SingleTicketTests {

        @Test
        @DisplayName("should return 200 OK")
        void shouldReturn200Ok() {
            when(ticketsService.findOne(ticketId)).thenReturn(response);
            ResponseEntity<CreateTicketResponse> result = controller.getTicket(ticketId);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }

        @Test
        @DisplayName("should propagate TicketNotFoundException")
        void shouldPropagateException() {
            when(ticketsService.findOne(ticketId)).thenThrow(new TicketNotFoundException(ticketId));

            assertThatThrownBy(() -> controller.getTicket(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);
        }

        @Test
        @DisplayName("should return 200 OK for patch")
        void shouldReturn200OkForPatch() {
            TicketPatchRequest patch = new TicketPatchRequest();
            when(ticketsService.patch(ticketId, patch)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.patchTicket(ticketId, patch);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("buyTicket() / returnTicket()")
    class BuyAndReturnTests {

        @Test
        @DisplayName("should return 200 OK for buyTicket")
        void shouldReturn200OkForBuy() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.buyTicket(ticketId, userId)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.buyTicket(ticketId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }

        @Test
        @DisplayName("should return 200 OK for returnTicket")
        void shouldReturn200OkForReturn() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.returnTicket(ticketId, userId)).thenReturn(response);

            ResponseEntity<CreateTicketResponse> result = controller.returnTicket(ticketId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("releaseTicketsBatch()")
    class ReleaseTicketsBatchTests {

        @Test
        @DisplayName("should return 200 OK")
        void shouldReturn200Ok() {
            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            ReleaseTicketsBatchResponse batchResponse = new ReleaseTicketsBatchResponse();
            when(ticketsService.releaseTicketsBatch(request)).thenReturn(batchResponse);

            ResponseEntity<ReleaseTicketsBatchResponse> result = controller.releaseTicketsBatch(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isSameAs(batchResponse);
        }
    }

    @Nested
    @DisplayName("downloadTicketPdf()")
    class DownloadTicketPdfTests {

        private Ticket ticket;
        private byte[] pdfBytes;

        @BeforeEach
        void setUp() {
            User user = User.builder().id(userId).build();
            ticket = Ticket.builder()
                    .id(ticketId)
                    .user(user)
                    .build();
            pdfBytes = new byte[]{37, 80, 68, 70}; // %PDF
        }

        @Test
        @DisplayName("should return 200 OK with PDF file")
        void shouldReturn200OkWithPdf() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.findById(ticketId)).thenReturn(ticket);
            when(ticketPdfService.generatePdf(ticket)).thenReturn(pdfBytes);

            ResponseEntity<Resource> response = controller.downloadTicketPdf(ticketId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
            assertThat(response.getHeaders().getContentLength()).isEqualTo(pdfBytes.length);
            assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                    .isEqualTo("attachment; filename=\"ticket-" + ticketId + ".pdf\"");

            assertThat(response.getBody()).isInstanceOf(ByteArrayResource.class);
            assertThat(((ByteArrayResource) response.getBody()).getByteArray()).isEqualTo(pdfBytes);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when different user")
        void shouldThrowWhenDifferentUser() {
            ticket.setUser(User.builder().id(UUID.randomUUID()).build());
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.findById(ticketId)).thenReturn(ticket);

            assertThatThrownBy(() -> controller.downloadTicketPdf(ticketId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Это не ваш билет");

            verifyNoInteractions(ticketPdfService);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when no user")
        void shouldThrowWhenTicketHasNoUser() {
            ticket.setUser(null);
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.findById(ticketId)).thenReturn(ticket);

            assertThatThrownBy(() -> controller.downloadTicketPdf(ticketId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Это не ваш билет");

            verifyNoInteractions(ticketPdfService);
        }

        @Test
        @DisplayName("should propagate TicketNotFoundException")
        void shouldPropagateExceptionWhenNotFound() {
            when(jwtCurrentUserService.getCurrentUserId()).thenReturn(userId);
            when(ticketsService.findById(ticketId)).thenThrow(new TicketNotFoundException(ticketId));

            assertThatThrownBy(() -> controller.downloadTicketPdf(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);

            verifyNoInteractions(ticketPdfService);
        }
    }
}