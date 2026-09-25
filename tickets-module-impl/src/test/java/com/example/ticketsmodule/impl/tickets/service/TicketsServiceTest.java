package com.example.ticketsmodule.impl.tickets.service;

import com.example.ticketsmodule.api.model.CreateTicketRequest;
import com.example.ticketsmodule.api.model.CreateTicketResponse;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchRequest;
import com.example.ticketsmodule.api.model.ReleaseTicketsBatchResponse;
import com.example.ticketsmodule.api.model.TicketPatchRequest;
import com.example.ticketsmodule.api.model.TicketsSearchRequest;
import com.example.ticketsmodule.impl.oauth.service.CurrentUserService;
import com.example.ticketsmodule.impl.routes.domain.RouteEntity;
import com.example.ticketsmodule.impl.routes.service.RoutesService;
import com.example.ticketsmodule.impl.tickets.conversion.TicketFromEntityConverter;
import com.example.ticketsmodule.impl.tickets.conversion.TicketToEntityConverter;
import com.example.ticketsmodule.impl.tickets.domain.TicketEntity;
import com.example.ticketsmodule.impl.tickets.repository.TicketRepository;
import com.example.ticketsmodule.impl.users.domain.UserEntity;
import com.example.ticketsmodule.impl.users.service.UsersService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketFromEntityConverter fromEntityConverter;
    @Mock private TicketToEntityConverter toEntityConverter;
    @Mock private RoutesService routesService;
    @Mock private CurrentUserService currentUserService;
    @Mock private UsersService usersService;

    @InjectMocks
    private TicketsService ticketsService;

    private UUID ticketId;
    private UUID routeId;
    private UUID userId;
    private TicketEntity ticket;
    private RouteEntity route;
    private UserEntity user;
    private CreateTicketResponse expectedResponse;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        route = new RouteEntity();
        route.setId(routeId);
        route.setDepartureAt(LocalDateTime.now().plusDays(1));

        user = new UserEntity();
        user.setId(userId);
        user.setLogin("admin");

        ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setRoute(route);
        ticket.setSeatNumber(1);
        ticket.setPrice(BigDecimal.valueOf(100));
        ticket.setPurchased(false);

        expectedResponse = new CreateTicketResponse();
    }

    // ---------- create() ----------

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should save ticket and return converted response")
        void shouldSaveTicketAndReturnResponse() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setRouteId(routeId);

            when(routesService.findOneById(routeId)).thenReturn(route);
            when(toEntityConverter.convert(request, route)).thenReturn(ticket);
            when(ticketRepository.save(ticket)).thenReturn(ticket);
            when(fromEntityConverter.convert(ticket)).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.create(request);

            assertThat(actual).isSameAs(expectedResponse);
            verify(routesService).findOneById(routeId);
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("should throw IllegalStateException when converter returns null")
        void shouldThrowWhenConverterReturnsNull() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setRouteId(routeId);

            when(routesService.findOneById(routeId)).thenReturn(route);
            when(toEntityConverter.convert(request, route)).thenReturn(null);

            assertThatThrownBy(() -> ticketsService.create(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed to convert ticket entity");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException from RoutesService")
        void shouldPropagateExceptionFromRoutesService() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setRouteId(routeId);

            when(routesService.findOneById(routeId))
                    .thenThrow(new EntityNotFoundException("Route not found"));

            assertThatThrownBy(() -> ticketsService.create(request))
                    .isInstanceOf(EntityNotFoundException.class);

            verifyNoInteractions(ticketRepository, toEntityConverter, fromEntityConverter);
        }
    }

    // ---------- findAll() ----------

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should return mapped page")
        void shouldReturnMappedPage() {
            TicketsSearchRequest search = new TicketsSearchRequest();
            Pageable pageable = PageRequest.of(0, 10);

            when(ticketRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(ticket)));
            when(fromEntityConverter.convert(ticket)).thenReturn(expectedResponse);

            Page<CreateTicketResponse> actual = ticketsService.findAll(search, pageable);

            assertThat(actual).hasSize(1);
            assertThat(actual.getContent()).containsExactly(expectedResponse);
        }

        @Test
        @DisplayName("should return empty page")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(ticketRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(Page.empty());

            Page<CreateTicketResponse> actual = ticketsService.findAll(null, pageable);

            assertThat(actual).isEmpty();
            verifyNoInteractions(fromEntityConverter);
        }
    }

    // ---------- findOne() ----------

    @Nested
    @DisplayName("findOne()")
    class FindOneTests {

        @Test
        @DisplayName("should return response when ticket exists")
        void shouldReturnResponseWhenFound() {
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(fromEntityConverter.convert(ticket)).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.findOne(ticketId);

            assertThat(actual).isSameAs(expectedResponse);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketsService.findOne(ticketId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(ticketId.toString());
        }
    }

    // ---------- patch() ----------

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
            when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(fromEntityConverter.convert(any(TicketEntity.class))).thenReturn(expectedResponse);

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
            RouteEntity newRoute = new RouteEntity();
            newRoute.setId(newRouteId);

            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setRouteId(newRouteId);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(routesService.findOneById(newRouteId)).thenReturn(newRoute);
            when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(fromEntityConverter.convert(any(TicketEntity.class))).thenReturn(expectedResponse);

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
            when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(fromEntityConverter.convert(any(TicketEntity.class))).thenReturn(expectedResponse);

            ticketsService.patch(ticketId, patch);

            assertThat(ticket.getUser()).isSameAs(user);
            assertThat(ticket.isPurchased()).isTrue();
            assertThat(ticket.getPurchasedAt()).isNotNull();
        }

        @Test
        @DisplayName("should set purchasedAt without user when only purchasedAt provided")
        void shouldSetPurchasedAtWhenOnlyDateProvided() {
            LocalDateTime purchasedAt = LocalDateTime.now().minusDays(1);
            TicketPatchRequest patch = new TicketPatchRequest();
            patch.setPurchasedAt(purchasedAt);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(fromEntityConverter.convert(any(TicketEntity.class))).thenReturn(expectedResponse);

            ticketsService.patch(ticketId, patch);

            assertThat(ticket.getPurchasedAt()).isEqualTo(purchasedAt);
            assertThat(ticket.getUser()).isNull();
            verifyNoInteractions(usersService);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketsService.patch(ticketId, new TicketPatchRequest()))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(ticketRepository, never()).save(any());
        }
    }

    // ---------- buyTicket() ----------

    @Nested
    @DisplayName("buyTicket()")
    class BuyTicketTests {

        @Test
        @DisplayName("should mark ticket as purchased for current user")
        void shouldBuyTicketSuccessfully() {
            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(fromEntityConverter.convert(any(TicketEntity.class))).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.buyTicket(ticketId);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(ticket.isPurchased()).isTrue();
            assertThat(ticket.getUser()).isSameAs(user);
            assertThat(ticket.getPurchasedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when ticket already purchased")
        void shouldThrowWhenAlreadyPurchased() {
            ticket.setPurchased(true);

            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.buyTicket(ticketId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already purchased");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when route departure is in the past")
        void shouldThrowWhenDepartureInPast() {
            route.setDepartureAt(LocalDateTime.now().minusDays(1));

            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.buyTicket(ticketId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("past departure");

            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when ticket not found")
        void shouldThrowWhenNotFound() {
            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketsService.buyTicket(ticketId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ---------- returnTicket() ----------

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
            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(inv -> inv.getArgument(0));
            when(fromEntityConverter.convert(any(TicketEntity.class))).thenReturn(expectedResponse);

            CreateTicketResponse actual = ticketsService.returnTicket(ticketId);

            assertThat(actual).isSameAs(expectedResponse);
            assertThat(ticket.isPurchased()).isFalse();
            assertThat(ticket.getUser()).isNull();
            assertThat(ticket.getPurchasedAt()).isNull();
        }

        @Test
        @DisplayName("should throw when ticket is not purchased")
        void shouldThrowWhenNotPurchased() {
            ticket.setPurchased(false);

            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.returnTicket(ticketId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not purchased");
        }

        @Test
        @DisplayName("should throw when departure is in the past")
        void shouldThrowWhenDepartureInPast() {
            route.setDepartureAt(LocalDateTime.now().minusDays(1));

            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.returnTicket(ticketId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("after departure");
        }

        @Test
        @DisplayName("should throw when ticket belongs to another user")
        void shouldThrowWhenDifferentUser() {
            UserEntity otherUser = new UserEntity();
            otherUser.setId(UUID.randomUUID());
            ticket.setUser(otherUser);

            when(currentUserService.getCurrentUser()).thenReturn(user);
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketsService.returnTicket(ticketId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Users are different");
        }
    }

    // ---------- releaseTicketsBatch() ----------

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
                List<TicketEntity> arg = inv.getArgument(0);
                arg.forEach(t -> t.setId(UUID.randomUUID()));
                return arg;
            });

            ReleaseTicketsBatchResponse response = ticketsService.releaseTicketsBatch(request);

            assertThat(response.getCreatedCount()).isEqualTo(3); // 5 - 2 occupied = 3
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
            assertThat(response.getRouteId()).isEqualTo(routeId);
            assertThat(response.getTicketsIds()).hasSize(3);

            ArgumentCaptor<List<TicketEntity>> captor = ArgumentCaptor.forClass(List.class);
            verify(ticketRepository).saveAll(captor.capture());
            assertThat(captor.getValue())
                    .extracting(TicketEntity::getSeatNumber)
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
}