package com.projeto.ticket_wave;

import com.projeto.ticket_wave.modules.events.application.port.EventRepository;
import com.projeto.ticket_wave.modules.events.application.dto.EventSummaryResponse;
import com.projeto.ticket_wave.modules.events.application.service.EventService;
import com.projeto.ticket_wave.modules.events.domain.entity.Category;
import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.modules.events.domain.entity.Venue;
import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;
import com.projeto.ticket_wave.modules.events.domain.enums.EventSort;
import com.projeto.ticket_wave.modules.events.domain.enums.EventType;
import com.projeto.ticket_wave.modules.events.domain.search.EventSearchCriteria;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.CategoryJpaRepository;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.VenueJpaRepository;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.AddressJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.CategoryJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.VenueJpaEntity;
import com.projeto.ticket_wave.modules.identity.application.port.UserRepository;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.domain.enums.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TicketWaveApplicationTests {

    @Autowired
    private EventRepository events;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository users;

    @Autowired
    private CategoryJpaRepository categories;

    @Autowired
    private VenueJpaRepository venues;

    @Autowired
    private EntityManager entityManager;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void savesAndUpdatesDomainEventWithExistingRelations() {
        User user = new User();
        user.setName("Organizador");
        user.setEmail("organizador@example.test");
        user.setPassword("test-password-hash");
        user.setPhone("11987654321");
        user.setRole(Role.ROLE_ORGANIZER);
        User organizer = users.save(user);

        CategoryJpaEntity categoryEntity = new CategoryJpaEntity();
        categoryEntity.setName("Tecnologia");
        categoryEntity.setSlug("tecnologia");
        Category category = new Category();
        category.setId(categories.save(categoryEntity).getId());

        AddressJpaEntity address = new AddressJpaEntity();
        address.setStreet("Rua de teste");
        address.setNumber("1");
        address.setDistrict("Centro");
        address.setCity("São Paulo");
        address.setState("SP");
        address.setZipCode("01000-000");

        VenueJpaEntity venueEntity = new VenueJpaEntity();
        venueEntity.setName("Auditório");
        venueEntity.setSlug("auditorio");
        venueEntity.setCapacity(100);
        venueEntity.setAddress(address);
        Venue venue = new Venue();
        venue.setId(venues.save(venueEntity).getId());

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 9, 0);
        Event event = events.save(Event.builder()
                .title("Conferência")
                .slug("conferencia")
                .shortDescription("Programação de teste")
                .description("Descrição da conferência")
                .startDate(start)
                .endDate(start.plusHours(2))
                .eventStatus(EventStatus.DRAFT)
                .eventType(EventType.PRESENTIAL)
                .capacity(100)
                .availableTickets(100)
                .ticketPrice(BigDecimal.TEN)
                .totalViews(0)
                .totalFavorites(0)
                .totalTicketsSold(0)
                .category(category)
                .venue(venue)
                .organizer(organizer)
                .build());

        assertThat(event.getId()).isNotNull();
        assertThat(event.getCreatedDate()).isNotNull();
        entityManager.clear();

        Event reloaded = events.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getCategory().getName()).isEqualTo("Tecnologia");
        assertThat(reloaded.getVenue().getAddress().getCity()).isEqualTo("São Paulo");
        assertThat(reloaded.getOrganizer().getId()).isEqualTo(organizer.getId());

        reloaded.setTitle("Conferência atualizada");
        Event updated = events.save(reloaded);
        assertThat(updated.getVersion()).isGreaterThan(event.getVersion());
        entityManager.clear();

        assertThat(events.findById(event.getId()).orElseThrow().getTitle()).isEqualTo("Conferência atualizada");

        reloaded.setId(null);
        reloaded.setSlug("conferencia-premium");
        reloaded.setTicketPrice(BigDecimal.valueOf(50));
        Event premium = events.save(reloaded);
        EventSearchCriteria criteria = new EventSearchCriteria(
                null, category.getId(), null, null, null, null, null,
                null, true, true, true, start.toLocalDate(), start.toLocalDate(),
                null, null, null, null, null, EventSort.PRICE_DESC);

        var results = eventService.search(criteria, PageRequest.of(0, 10));
        assertThat(results.getContent()).extracting(EventSummaryResponse::id)
                .containsExactly(premium.getId(), event.getId());
        assertThat(results.getContent()).allSatisfy(result -> {
            assertThat(result.city()).isEqualTo("São Paulo");
            assertThat(result.status()).isEqualTo(EventStatus.DRAFT);
        });

        assertThatThrownBy(() -> events.save(event)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
