package uk.gov.moj.cpp.support.repository;

import uk.gov.moj.cpp.support.entity.Feedback;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

/**
 * Feedback view-store repository. Migrated from a DeltaSpike Data {@code @Repository}
 * ({@code EntityRepository<Feedback, UUID>}) to a concrete {@code @ApplicationScoped} JPA repository as
 * part of the Java 25 / Jakarta EE 11 upgrade — DeltaSpike is EOL and unavailable under CDI 4. Each
 * DeltaSpike derived-query method becomes an explicit JPQL {@code createQuery} preserving the original
 * semantics; {@code save} maps to {@code merge} (idempotent under event-listener replay), and
 * {@code findBy(id)} keeps returning the entity (or null) to match the callers.
 */
@ApplicationScoped
public class FeedbackRepository {

    @PersistenceContext(unitName = "support")
    EntityManager entityManager;

    public void save(final Feedback feedback) {
        entityManager.merge(feedback);
    }

    public Feedback findBy(final UUID feedbackId) {
        return entityManager.find(Feedback.class, feedbackId);
    }

    public List<Feedback> findBycaseId(final UUID caseId) {
        final TypedQuery<Feedback> query = entityManager.createQuery(
                "SELECT f FROM Feedback f WHERE f.caseId = :caseId", Feedback.class);
        query.setParameter("caseId", caseId);
        return query.getResultList();
    }

    public List<Feedback> findByDateReceivedBetween(final ZonedDateTime from, final ZonedDateTime to) {
        final TypedQuery<Feedback> query = entityManager.createQuery(
                "SELECT f FROM Feedback f WHERE f.dateReceived >= :from AND f.dateReceived <= :to", Feedback.class);
        query.setParameter("from", from);
        query.setParameter("to", to);
        return query.getResultList();
    }

    public List<Feedback> findByCaseIdAndDateReceivedBetween(final UUID caseId, final ZonedDateTime from, final ZonedDateTime to) {
        final TypedQuery<Feedback> query = entityManager.createQuery(
                "SELECT f FROM Feedback f WHERE f.caseId = :caseId AND f.dateReceived >= :from AND f.dateReceived <= :to", Feedback.class);
        query.setParameter("caseId", caseId);
        query.setParameter("from", from);
        query.setParameter("to", to);
        return query.getResultList();
    }
}
