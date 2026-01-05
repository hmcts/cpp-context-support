package uk.gov.moj.cpp.support.repository;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalJunit4Test;
import uk.gov.moj.cpp.support.entity.Feedback;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class FeedbackRepositoryTest extends BaseTransactionalJunit4Test {

    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    private FeedbackRepository feedbackRepository;

    @Test
    public void shouldSaveAndFindAFeedback() throws Exception {

        final UUID feedbackId = randomUUID();
        final ZonedDateTime dateReceived = now();
        final Feedback feedback = aFeedback(feedbackId, dateReceived, randomUUID());

        feedbackRepository.save(feedback);

        final Feedback foundFeedback = feedbackRepository.findBy(feedbackId);

        assertThat(foundFeedback.getFeedbackId(), is(feedback.getFeedbackId()));
        assertThat(foundFeedback.getComment(), is(feedback.getComment()));
        assertThat(foundFeedback.getRefUrl(), is(feedback.getRefUrl()));
        assertThat(foundFeedback.getRefService(), is(feedback.getRefService()));
        assertThat(foundFeedback.getDateReceived(), is(feedback.getDateReceived()));
        assertThat(foundFeedback.getCaseId(), is(feedback.getCaseId()));
    }

    @Test
    public void shouldSaveAndFindAFeedbackWithOnlyRequiredValues() throws Exception {

        final UUID feedbackId = randomUUID();
        final ZonedDateTime dateReceived = now();
        final Feedback feedback = aFeedbackWithDefaultValues(feedbackId, dateReceived);

        feedbackRepository.save(feedback);

        final Feedback foundFeedback = feedbackRepository.findBy(feedbackId);

        assertThat(foundFeedback.getFeedbackId(), is(feedback.getFeedbackId()));
        assertThat(foundFeedback.getComment(), is(nullValue()));
        assertThat(foundFeedback.getRefUrl(), is(nullValue()));
        assertThat(foundFeedback.getRefService(), is(nullValue()));
        assertThat(foundFeedback.getDateReceived(), is(feedback.getDateReceived()));
        assertThat(foundFeedback.getCaseId(), is(nullValue()));
    }

    @Test
    public void shouldFindByDateRange() throws Exception {

        final UUID feedbackId_1 = randomUUID();
        final UUID feedbackId_2 = randomUUID();
        final UUID feedbackId_3 = randomUUID();

        final ZonedDateTime dateReceived_1 = of(2018, 2, 23, 6, 6, 6, 6, UTC);
        final ZonedDateTime dateReceived_2 = of(2018, 2, 23, 10, 10, 10, 10, UTC);
        final ZonedDateTime dateReceived_3 = of(2018, 2, 23, 23, 23, 23, 23, UTC);

        feedbackRepository.save(aFeedback(feedbackId_1, dateReceived_1, randomUUID()));
        feedbackRepository.save(aFeedback(feedbackId_2, dateReceived_2, randomUUID()));
        feedbackRepository.save(aFeedback(feedbackId_3, dateReceived_3, randomUUID()));

        final ZonedDateTime from = of(2018, 2, 23, 7, 7, 7, 7, UTC);
        final ZonedDateTime to = of(2018, 2, 23, 22, 22, 22, 22, UTC);

        final List<Feedback> feedbackList = feedbackRepository.findByDateReceivedBetween(from, to);

        assertThat(feedbackList.size(), is(1));
        assertThat(feedbackList.get(0).getFeedbackId(), is(feedbackId_2));
        assertThat(feedbackList.get(0).getDateReceived(), is(dateReceived_2));
    }

    @Test
    public void shouldFindByCaseId() throws Exception {

        final UUID caseId = randomUUID();
        final UUID otherCaseId = randomUUID();

        final UUID feedbackId_1 = randomUUID();
        final UUID feedbackId_2 = randomUUID();
        final UUID feedbackId_3 = randomUUID();

        feedbackRepository.save(aFeedback(feedbackId_1, now(UTC), caseId));
        feedbackRepository.save(aFeedback(feedbackId_2, now(UTC), otherCaseId));
        feedbackRepository.save(aFeedback(feedbackId_3, now(UTC), caseId));

        final List<Feedback> feedbackList = feedbackRepository.findBycaseId(caseId);

        assertThat(feedbackList.size(), is(2));
        assertThat(feedbackList.get(0).getFeedbackId(), is(feedbackId_1));
        assertThat(feedbackList.get(0).getCaseId(), is(caseId));
        assertThat(feedbackList.get(1).getFeedbackId(), is(feedbackId_3));
        assertThat(feedbackList.get(1).getCaseId(), is(caseId));
    }

    @Test
    public void shouldFindByCaseIdAndDateRange() throws Exception {

        final UUID caseId = randomUUID();
        final UUID otherCaseId = randomUUID();

        final UUID feedbackId_1 = randomUUID();
        final UUID feedbackId_2 = randomUUID();
        final UUID feedbackId_3 = randomUUID();
        final ZonedDateTime dateReceived_1 = of(2018, 2, 23, 6, 6, 6, 6, UTC);
        final ZonedDateTime dateReceived_2 = of(2018, 2, 23, 10, 10, 10, 10, UTC);

        feedbackRepository.save(aFeedback(feedbackId_1, dateReceived_1, caseId));
        feedbackRepository.save(aFeedback(feedbackId_2,dateReceived_2, caseId));
        feedbackRepository.save(aFeedback(feedbackId_3, dateReceived_2, otherCaseId));

        final ZonedDateTime from = of(2018, 2, 23, 7, 7, 7, 7, UTC);
        final ZonedDateTime to = of(2018, 2, 23, 22, 22, 22, 22, UTC);

        final List<Feedback> feedbackList = feedbackRepository.findByCaseIdAndDateReceivedBetween(caseId, from, to);

        assertThat(feedbackList.size(), is(1));
        assertThat(feedbackList.get(0).getFeedbackId(), is(feedbackId_2));
        assertThat(feedbackList.get(0).getCaseId(), is(caseId));
    }

    private Feedback aFeedback(final UUID feedbackId, final ZonedDateTime dateReceived, final UUID caseId) {
        final String comment = "a comment";
        final String refUrl = "www.gerritt.com";
        final String refService = "TechPod";
        
        return new Feedback(
                feedbackId,
                comment,
                refUrl,
                refService,
                1,
                dateReceived,
                caseId
        );
    }

    private Feedback aFeedbackWithDefaultValues(final UUID feedbackId, final ZonedDateTime dateReceived) {
        final String comment = null;
        final String refUrl = null;
        final String refService = null;
        final UUID caseId = null;
        return new Feedback(
                feedbackId,
                comment,
                refUrl,
                refService,
                1,
                dateReceived,
                caseId
        );
    }
}
