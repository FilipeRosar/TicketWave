package com.projeto.ticket_wave.domain.specification;

import com.projeto.ticket_wave.domain.entity.Event;
import com.projeto.ticket_wave.domain.enums.EventStatus;
import com.projeto.ticket_wave.domain.enums.EventType;
import com.projeto.ticket_wave.domain.search.EventSearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class EventSpecification {

    private EventSpecification() {}

    public static Specification<Event> withFilters(EventSearchCriteria filters) {
        Specification<Event> spec = (root, query, cb) -> cb.conjunction();

        if (filters.keyword() != null && !filters.keyword().isBlank()) {
            spec = spec.and(keyword(filters.keyword()));
        }

        if (filters.city() != null && !filters.city().isBlank()) {
            spec = spec.and(cityEquals(filters.city()));
        }

        if (filters.categoryId() != null) {
            spec = spec.and(categoryEquals(filters.categoryId()));
        }

        if (filters.organizerId() != null) {
            spec = spec.and(organizerEquals(filters.organizerId()));
        }

        if (filters.eventStatus() != null) {
            spec = spec.and(statusEquals(filters.eventStatus()));
        }

        if (filters.eventType() != null) {
            spec = spec.and(typeEquals(filters.eventType()));
        }

        if (filters.minPrice() != null || filters.maxPrice() != null) {
            spec = spec.and(priceBetween(filters.minPrice(), filters.maxPrice()));
        }

        if (filters.featured() != null) {
            spec = spec.and(featuredEquals(filters.featured()));
        }

        if (filters.networking() != null) {
            spec = spec.and(networkingEquals(filters.networking()));
        }

        if (filters.gamification() != null) {
            spec = spec.and(gamificationEquals(filters.gamification()));
        }

        if (filters.aiAssistant() != null) {
            spec = spec.and(aiAssistantEquals(filters.aiAssistant()));
        }

        if (Boolean.TRUE.equals(filters.availableOnly())) {
            spec = spec.and(availableTickets());
        }

        if (filters.startDate() != null || filters.endDate() != null) {
            spec = spec.and(dateBetween(filters.startDate(), filters.endDate()));
        }

        if (filters.latitude() != null && filters.longitude() != null && filters.radiusKm() != null) {
            spec = spec.and(withinRadius(filters.latitude(), filters.longitude(), filters.radiusKm()));
        }

        return spec;
    }

    private static Specification<Event> cityEquals(String city) {
        return (root, query, cb) -> {
            Join<?, ?> venue = root.join("venue");
            Join<?, ?> address = venue.join("address");
            return cb.equal(cb.lower(address.get("city")), city.toLowerCase());
        };
    }

   private static Specification<Event> categoryEquals(UUID categoryId) {
        return (root, query, cb) -> {
            Join<?, ?> categoryJoin = root.join("category");
            return cb.equal(categoryJoin.get("id"), categoryId);
        };
    }

    private static Specification<Event> organizerEquals(UUID organizerId) {
        return (root, query, cb) -> {
            Join<?, ?> organizerJoin = root.join("organizer");
            return cb.equal(organizerJoin.get("id"), organizerId);
        };
    }

    private static Specification<Event> statusEquals(EventStatus status) {
        return (root, query, cb) -> cb.equal(root.get("eventStatus"), status);
    }

    private static Specification<Event> typeEquals(EventType type) {
        return (root, query, cb) -> cb.equal(root.get("eventType"), type);
    }

    private static Specification<Event> featuredEquals(Boolean featured) {
        return (root, query, cb) -> cb.equal(root.get("featured"), featured);
    }

    private static Specification<Event> networkingEquals(Boolean networking) {
        return (root, query, cb) -> cb.equal(root.get("networking"), networking);
    }

    private static Specification<Event> gamificationEquals(Boolean gamification) {
        return (root, query, cb) -> cb.equal(root.get("gamification"), gamification);
    }

    private static Specification<Event> aiAssistantEquals(Boolean aiAssistant) {
        return (root, query, cb) -> cb.equal(root.get("aiAssistant"), aiAssistant);
    }

   private static Specification<Event> availableTickets() {
        return (root, query, cb) -> cb.greaterThan(root.get("availableTickets"), 0);
    }

    private static Specification<Event> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min != null && max != null) return cb.between(root.get("ticketPrice"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("ticketPrice"), min);
            return cb.lessThanOrEqualTo(root.get("ticketPrice"), max);
        };
    }

    private static Specification<Event> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start != null && end != null) return cb.between(root.get("startDate"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("startDate"), start);
            return cb.lessThanOrEqualTo(root.get("endDate"), end);
        };
    }

    public static Specification<Event> keyword(String keyword) {
        return (root, query, cb) -> {
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("shortDescription")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        };
    }

    private static Specification<Event> withinRadius(Double lat, Double lng, Double radiusKm) {
        return (root, query, cb) -> {
            Join<?, ?> venue = root.join("venue");
            Join<?, ?> address = venue.join("address");

            var addrLat = address.get("latitude").as(Double.class);
            var addrLng = address.get("longitude").as(Double.class);

            var distanceExpr = cb.prod(
                    cb.literal(6371.0),
                    cb.function("acos", Double.class,
                            cb.sum(
                                    cb.prod(
                                            cb.function("cos", Double.class, cb.function("radians", Double.class, cb.literal(lat))),
                                            cb.prod(
                                                    cb.function("cos", Double.class, cb.function("radians", Double.class, addrLat)),
                                                    cb.function("cos", Double.class,
                                                            cb.diff(
                                                                    cb.function("radians", Double.class, addrLng),
                                                                    cb.function("radians", Double.class, cb.literal(lng))
                                                            )
                                                    )
                                            )
                                    ),
                                    cb.prod(
                                            cb.function("sin", Double.class, cb.function("radians", Double.class, cb.literal(lat))),
                                            cb.function("sin", Double.class, cb.function("radians", Double.class, addrLat))
                                    )
                            )
                    )
            );

            Predicate hasCoords = cb.and(cb.isNotNull(address.get("latitude")), cb.isNotNull(address.get("longitude")));
            return cb.and(hasCoords, cb.lessThanOrEqualTo(distanceExpr, radiusKm));
        };
    }
}