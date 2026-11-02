package com.example.ticketsmodule.impl.tickets.service;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchRequest;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchResponse;
import com.example.ticketsmodule.api.model.TicketPatchRequest;
import com.example.ticketsmodule.api.model.TicketsSearchRequest;
import com.example.ticketsmodule.domain.common.PageRequest;
import com.example.ticketsmodule.domain.common.PageResult;
import com.example.ticketsmodule.domain.route.model.Route;
import com.example.ticketsmodule.domain.ticket.exception.TicketAlreadyPurchasedException;
import com.example.ticketsmodule.domain.ticket.exception.TicketNotFoundException;
import com.example.ticketsmodule.domain.ticket.model.Ticket;
import com.example.ticketsmodule.domain.ticket.repository.TicketsRepositoryPort;
import com.example.ticketsmodule.domain.user.model.User;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.conversion.TicketMapper;
import com.example.ticketsmodule.impl.users.service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketsService unit tests")
class TicketsServiceTest {

    @Mock private TicketsRepositoryPort ticketRepository;
    @Mock private TicketMapper ticketMapper;
    @Mock private RoutesService routesService;
    @Mock private UsersService usersService;

    @InjectMocks
    private TicketsService ticketsService;

    private UUID ticketId;
    private UUID routeId;
    private UUID userId;
    private Ticket ticket;
    private Route route;
    private User user;
    private CreateTicketResponse expectedResponse;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        route = Route.builder()
                .id(routeId)
                .departureAt(LocalDateTime.now().plusDays(1))
                .build();

        user = User.builder()
                .id(userId)
                .login("admin")
                .build();

        ticket = Ticket.builder()
                .id(ticketId)
                .route(route)
                .seatNumber(1)
                .price(BigDecimal.valueOf(100))
                .purchased(false)
                .build();

        expectedResponse = new CreateTicketResponse();
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should save ticket and return response")
        void shouldSaveTicketAndReturnResponse() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setRouteId(routeId);

            when(routesService.findOneById(routeId)).thenReturn(route);
            when(ticketMapper.toDomain(request)).thenReturn(ticket);
            when(ticketRepository.save(ticket)).thenReturn(ticket);
            when(ticketMapper.toResponse(ticket)).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.create(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(routesService).findOneById(routeId);
            verify(ticketMapper).toDomain(request);
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("should propagate exception from RoutesService")
        void shouldPropagateExceptionFromRoutesService() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setRouteId(routeId);

            when(routesService.findOneById(routeId))
                    .thenThrow(new RuntimeException("Route not found"));

            assertThatThrownBy(() -> ticketsService.create(request))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(ticketRepository, ticketMapper);
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should return mapped PageResult")
        void shouldReturnMappedPageResult() {
            TicketsSearchRequest search = new TicketsSearchRequest();
            PageRequest pageReq = PageRequest.builder().page(0).size(10).build();

            PageResult<Ticket> domainPage = PageResult.<Ticket>builder()
                    .content(List.of(ticket))
                    .totalElements(1)
                    .build();

            when(ticketRepository.findAll(any(), eq(pageReq))).thenReturn(domainPage);
            when(ticketMapper.toResponse(ticket)).thenReturn(expectedResponse);

            PageResult<CreateTicketResponse> actual = ticketsService.findAll(search, pageReq);

            assertThat(actual.getContent()).hasSize(1);
            assertThat(actual.getContent().get(0)).isSameAs(expectedResponse);
            assertThat(actual.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findAllMyTickets()")
    class FindAllMyTicketsTests {

        @Test
        @DisplayName("should return mapped list")
        void shouldReturnMappedList() {
            when(ticketRepository.findPurchasedByUserId(userId)).thenReturn(List.of(ticket));
            when(ticketMapper.toResponse(ticket)).thenReturn(expectedResponse);

            List<CreateTicketResponse> actual = ticketsService.findAllMyTickets(userId);

            assertThat(actual).containsExactly(expectedResponse);
            verify(ticketRepository).findPurchasedByUserId(userId);
        }
    }

    @Nested
    @DisplayName("findOne() / findById()")
    class FindOneTests {

        @Test
        @DisplayName("should return response when ticket exists")
        void shouldReturnResponseWhenFound() {
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketMapper.toResponse(ticket)).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.findOne(ticketId);

            assertThat(actual).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("should throw TicketNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketsService.findOne(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);

            assertThatThrownBy(() -> ticketsService.findById(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("patch()")
    class PatchTests {

        @Test
        @DisplayName("should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setSeatNumber(42);
            patch.setPrice(BigDecimal.valueOf(250));

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.patch(ticketId, patch);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(ticket.getSeatNumber()).isEqualTo(42);
            assertThat(ticket.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
            assertThat(ticket.getUpdatedAt()).isNotNull();
            verifyNoInteractions(routesService, usersService);
        }

        @Test
        @DisplayName("should update route when routeId provided")
        void shouldUpdateRouteWhenProvided() {
            UUID newRouteId = UUID.randomUUID();
            Route newRoute = Route.builder().id(newRouteId).build();

            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setRouteId(newRouteId);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(routesService.findOneById(newRouteId)).thenReturn(newRoute);
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);

            ticketsService.patch(ticketId, patch);

            assertThat(ticket.getRoute()).isSameAs(newRoute);
            verify(routesService).findOneById(newRouteId);
        }

        @Test
        @DisplayName("should mark as purchased when purchasedById provided")
        void shouldMarkPurchasedWhenUserProvided() {
            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setPurchasedById(userId);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);

            ticketsService.patch(ticketId, patch);

            assertThat(ticket.getUser()).isSameAs(user);
            assertThat(ticket.isPurchased()).isTrue();
            assertThat(ticket.getPurchasedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("buyTicket()")
    class BuyTicketTests {

        @Test
        @DisplayName("should mark ticket as purchased for current user")
        void shouldBuyTicketSuccessfully() {
            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.buyTicket(ticketId, userId);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(ticket.isPurchased()).isTrue();
            assertThat(ticket.getUser()).isSameAs(user);
            assertThat(ticket.getPurchasedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when ticket already purchased")
        void shouldThrowWhenAlreadyPurchased() {
            ticket.setPurchased(true);

            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.buyTicket(ticketId, userId))
                    .isInstanceOf(TicketAlreadyPurchasedException.class)
                    .hasMessageContaining("already purchased");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when route departure is in the past")
        void shouldThrowWhenDepartureInPast() {
            route.setDepartureAt(LocalDateTime.now().minusDays(1));
            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.buyTicket(ticketId, userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("past departure");

            verify(ticketRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("returnTicket()")
    class ReturnTicketTests {

        @BeforeEach
        void preparePurchasedTicket() {
            ticket.setPurchased(true);
            ticket.setUser(user);
            ticket.setPurchasedAt(LocalDateTime.now().minusHours(1));
        }

        @Test
        @DisplayName("should return ticket successfully")
        void shouldReturnTicketSuccessfully() {
            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
            when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.returnTicket(ticketId, userId);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(ticket.isPurchased()).isFalse();
            assertThat(ticket.getUser()).isNull();
            assertThat(ticket.getPurchasedAt()).isNull();
        }

        @Test
        @DisplayName("should throw when ticket is not purchased")
        void shouldThrowWhenNotPurchased() {
            ticket.setPurchased(false);
            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.returnTicket(ticketId, userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not purchased");
        }

        @Test
        @DisplayName("should throw when departure is in the past")
        void shouldThrowWhenDepartureInPast() {
            route.setDepartureAt(LocalDateTime.now().minusDays(1));
            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.returnTicket(ticketId, userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("after departure");
        }

        @Test
        @DisplayName("should throw when ticket belongs to another user")
        void shouldThrowWhenDifferentUser() {
            User otherUser = User.builder().id(UUID.randomUUID()).build();
            ticket.setUser(otherUser);
            when(usersService.findOneById(userId)).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.returnTicket(ticketId, userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Users are different");
        }
    }

    @Nested
    @DisplayName("releaseTicketsBatch()")
    class ReleaseTicketsBatchTests {

        @Test
        @DisplayName("should release tickets for all free seats")
        void shouldReleaseFreeSeats() {
            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            request.setRouteId(routeId);
            request.setSeatCount(5);
            request.setPrice(BigDecimal.valueOf(150));

            when(routesService.findOneById(routeId)).thenReturn(route);
            when(ticketRepository.findSeatNumbersByRouteId(routeId)).thenReturn(Set.of(1, 2));
            when(ticketRepository.saveAll(anyList())).thenAnswer(inv -> {
                List<Ticket> arg = inv.getArgument(0);
                arg.forEach(t -> t.setId(UUID.randomUUID()));
                return arg;
            });

            ReleaseTicketsBatchResponse response = ticketsService.releaseTicketsBatch(request);

            assertThat(response.getCreatedCount()).isEqualTo(3);
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
            assertThat(response.getRouteId()).isEqualTo(routeId);
            assertThat(response.getTicketsIds()).hasSize(3);

            ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
            verify(ticketRepository).saveAll(captor.capture());
            assertThat(captor.getValue())
                    .extracting(Ticket::getSeatNumber)
                    .containsExactlyInAnyOrder(3, 4, 5);
        }

        @Test
        @DisplayName("should throw when departure is in the past")
        void shouldThrowWhenDepartureInPast() {
            route.setDepartureAt(LocalDateTime.now().minusDays(1));

            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            request.setRouteId(routeId);
            request.setSeatCount(5);
            request.setPrice(BigDecimal.valueOf(150));

            when(routesService.findOneById(routeId)).thenReturn(route);

            assertThatThrownBy(() -> ticketsService.releaseTicketsBatch(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("в прошлом");

            verify(ticketRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("should throw when all seats are occupied")
        void shouldThrowWhenAllSeatsOccupied() {
            ReleaseTicketsBatchRequest request = new ReleaseTicketsBatchRequest();
            request.setRouteId(routeId);
            request.setSeatCount(3);
            request.setPrice(BigDecimal.valueOf(150));

            when(routesService.findOneById(routeId)).thenReturn(route);
            when(ticketRepository.findSeatNumbersByRouteId(routeId)).thenReturn(Set.of(1, 2, 3));

            assertThatThrownBy(() -> ticketsService.releaseTicketsBatch(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Нет свободных мест");

            verify(ticketRepository, never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("findAvailableByRoute()")
    class FindAvailableByRouteTests {

        @Test
        @DisplayName("should return mapped available tickets")
        void shouldReturnMappedAvailableTickets() {
            when(routesService.findOneById(routeId)).thenReturn(route);
            when(ticketRepository.findAvailableByRouteId(routeId)).thenReturn(List.of(ticket));
            when(ticketMapper.toResponse(ticket)).thenReturn(expectedResponse);

            List<CreateTicketResponse> actual = ticketsService.findAvailableByRoute(routeId);

            assertThat(actual).containsExactly(expectedResponse);
            verify(routesService).findOneById(routeId);
            verify(ticketRepository).findAvailableByRouteId(routeId);
        }

        @Test
        @DisplayName("should propagate exception when route not found")
        void shouldPropagateExceptionWhenRouteNotFound() {
            when(routesService.findOneById(routeId))
                    .thenThrow(new RuntimeException("Route not found"));

            assertThatThrownBy(() -> ticketsService.findAvailableByRoute(routeId))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(ticketRepository, ticketMapper);
        }
    }
}