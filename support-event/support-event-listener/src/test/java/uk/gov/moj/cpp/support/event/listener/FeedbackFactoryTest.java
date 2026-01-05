package uk.gov.moj.cpp.support.event.listener;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.support.entity.Feedback;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FeedbackFactoryTest {

    @InjectMocks
    private FeedbackFactory feedbackFactory;

    @Test
    public void shouldCreateAFeedbackWithNoNonRequiredFields() throws Exception {

        final UUID feedbackId = randomUUID();
        final Integer rating = 23;
        final ZonedDateTime dateReceived = now();

        final JsonObject payload = mock(JsonObject.class);

        when(payload.containsKey("comment")).thenReturn(false);
        when(payload.containsKey("refUrl")).thenReturn(false);
        when(payload.containsKey("refService")).thenReturn(false);
        when(payload.containsKey("caseId")).thenReturn(false);

        final Feedback feedback = feedbackFactory.create(payload, feedbackId, rating, dateReceived);

        assertThat(feedback.getFeedbackId(), is(feedbackId));
        assertThat(feedback.getRating(), is(rating));
        assertThat(feedback.getDateReceived(), is(dateReceived));

        assertThat(feedback.getComment(), is(nullValue()));
        assertThat(feedback.getRefUrl(), is(nullValue()));
        assertThat(feedback.getRefService(), is(nullValue()));
        assertThat(feedback.getCaseId(), is(nullValue()));
    }

    @Test
    public void shouldCreateAFeedbackWithNonRequiredFields() throws Exception {

        final UUID feedbackId = randomUUID();
        final Integer rating = 23;
        final ZonedDateTime dateReceived = now();
        final String comment = "comment";
        final String refUrl = "refUrl";
        final String refService = "refService";
        final UUID caseId = randomUUID();

        final JsonObject payload = mock(JsonObject.class);

        when(payload.containsKey("comment")).thenReturn(true);
        when(payload.containsKey("refUrl")).thenReturn(true);
        when(payload.containsKey("refService")).thenReturn(true);
        when(payload.containsKey("caseId")).thenReturn(true);

        when(payload.getString("comment")).thenReturn(comment);
        when(payload.getString("refUrl")).thenReturn(refUrl);
        when(payload.getString("refService")).thenReturn(refService);
        when(payload.getString("caseId")).thenReturn(caseId.toString());

        final Feedback feedback = feedbackFactory.create(payload, feedbackId, rating, dateReceived);

        assertThat(feedback.getFeedbackId(), is(feedbackId));
        assertThat(feedback.getRating(), is(rating));
        assertThat(feedback.getDateReceived(), is(dateReceived));

        assertThat(feedback.getComment(), is(comment));
        assertThat(feedback.getRefUrl(), is(refUrl));
        assertThat(feedback.getRefService(), is(refService));
        assertThat(feedback.getCaseId(), is(caseId));
    }
}
