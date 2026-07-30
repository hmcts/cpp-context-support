package uk.gov.moj.cpp.support.repository;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.support.entity.Feedback;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Real-JPA test for {@link FeedbackRepository} (post DeltaSpike Data → plain JPA migration). Runs at build
 * time (Surefire) against an in-memory H2 via {@link HibernateTestEntityManagerProvider} — the JPQL is
 * exercised against a real database (schema generated from the entity), each test rolled back. No docker
 * required, so it stays a {@code …Test} rather than a {@code …IT}.
 */
class FeedbackRepositoryTest {

    private static final String PERSISTENCE_UNIT = "support-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private FeedbackRepository feedbackRepository;

    @BeforeEach
    void createRepositoryWithInjectedEntityManager() {
        feedbackRepository = new FeedbackRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(feedbackRepository);
    }

    @Test
    void shouldSaveAndFindAFeedback() {
        final UUID feedbackId = randomUUID();
        final Feedback feedback = aFeedback(feedbackId, now(UTC), randomUUID());

        feedbackRepository.save(feedback);

        final Feedback found = feedbackRepository.findBy(feedbackId);
        assertThat(found.getFeedbackId(), is(feedback.getFeedbackId()));
        assertThat(found.getComment(), is(feedback.getComment()));
        assertThat(found.getRefUrl(), is(feedback.getRefUrl()));
        assertThat(found.getRefService(), is(feedback.getRefService()));
        assertThat(found.getCaseId(), is(feedback.getCaseId()));
        assertThat(found.getDateReceived().toInstant(), is(feedback.getDateReceived().toInstant()));
    }

    @Test
    void shouldSaveAndFindAFeedbackWithOnlyRequiredValues() {
        final UUID feedbackId = randomUUID();
        final Feedback feedback = aFeedbackWithDefaultValues(feedbackId, now(UTC));

        feedbackRepository.save(feedback);

        final Feedback found = feedbackRepository.findBy(feedbackId);
        assertThat(found.getFeedbackId(), is(feedback.getFeedbackId()));
        assertThat(found.getComment(), is(nullValue()));
        assertThat(found.getRefUrl(), is(nullValue()));
        assertThat(found.getRefService(), is(nullValue()));
        assertThat(found.getCaseId(), is(nullValue()));
    }

    @Test
    void shouldFindByDateReceivedBetween() {
        feedbackRepository.save(aFeedback(randomUUID(), of(2018, 2, 23, 6, 6, 6, 0, UTC), randomUUID()));
        final UUID inRangeId = randomUUID();
        feedbackRepository.save(aFeedback(inRangeId, of(2018, 2, 23, 10, 10, 10, 0, UTC), randomUUID()));
        feedbackRepository.save(aFeedback(randomUUID(), of(2018, 2, 23, 23, 23, 23, 0, UTC), randomUUID()));

        final List<Feedback> found = feedbackRepository.findByDateReceivedBetween(
                of(2018, 2, 23, 7, 7, 7, 0, UTC), of(2018, 2, 23, 22, 22, 22, 0, UTC));

        assertThat(found.size(), is(1));
        assertThat(found.get(0).getFeedbackId(), is(inRangeId));
    }

    @Test
    void shouldFindByCaseId() {
        final UUID caseId = randomUUID();
        final UUID otherCaseId = randomUUID();
        final UUID id1 = randomUUID();
        final UUID id3 = randomUUID();

        feedbackRepository.save(aFeedback(id1, now(UTC), caseId));
        feedbackRepository.save(aFeedback(randomUUID(), now(UTC), otherCaseId));
        feedbackRepository.save(aFeedback(id3, now(UTC), caseId));

        final List<Feedback> found = feedbackRepository.findBycaseId(caseId);

        assertThat(found.size(), is(2));
        assertThat(found.stream().allMatch(f -> f.getCaseId().equals(caseId)), is(true));
        assertThat(found.stream().anyMatch(f -> f.getFeedbackId().equals(id1)), is(true));
        assertThat(found.stream().anyMatch(f -> f.getFeedbackId().equals(id3)), is(true));
    }

    @Test
    void shouldFindByCaseIdAndDateReceivedBetween() {
        final UUID caseId = randomUUID();
        final UUID otherCaseId = randomUUID();
        final UUID inRangeId = randomUUID();

        feedbackRepository.save(aFeedback(randomUUID(), of(2018, 2, 23, 6, 6, 6, 0, UTC), caseId));
        feedbackRepository.save(aFeedback(inRangeId, of(2018, 2, 23, 10, 10, 10, 0, UTC), caseId));
        feedbackRepository.save(aFeedback(randomUUID(), of(2018, 2, 23, 10, 10, 10, 0, UTC), otherCaseId));

        final List<Feedback> found = feedbackRepository.findByCaseIdAndDateReceivedBetween(
                caseId, of(2018, 2, 23, 7, 7, 7, 0, UTC), of(2018, 2, 23, 22, 22, 22, 0, UTC));

        assertThat(found.size(), is(1));
        assertThat(found.get(0).getFeedbackId(), is(inRangeId));
        assertThat(found.get(0).getCaseId(), is(caseId));
    }

    private Feedback aFeedback(final UUID feedbackId, final ZonedDateTime dateReceived, final UUID caseId) {
        return new Feedback(feedbackId, "a comment", "www.gerritt.com", "TechPod", 1, dateReceived, caseId);
    }

    private Feedback aFeedbackWithDefaultValues(final UUID feedbackId, final ZonedDateTime dateReceived) {
        return new Feedback(feedbackId, null, null, null, 1, dateReceived, null);
    }
}
