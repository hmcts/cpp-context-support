package uk.gov.moj.cpp.support.event.listener;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder;
import uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory;
import uk.gov.moj.cpp.support.entity.Feedback;
import uk.gov.moj.cpp.support.repository.FeedbackRepository;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SupportEventListenerTest {

    private static final UUID FEEDBACK_ID = randomUUID();

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackFactory feedbackFactory;

    @InjectMocks
    private SupportEventListener supportEventListener;

    @Captor
    private ArgumentCaptor<Feedback> feedbackArgumentCaptor;

    @Test
    public void shouldStoreFeedbackInRepository() {
        final int rating = 5;
        final String comment = "what a lovely experience, I am tempted to do more crime just to use this service again";
        final ZonedDateTime dateReceived = ZonedDateTime.of(2021, 7, 11, 13, 23, 45, 0, UTC);
        final String refUrl = "ReferenceUrl";
        final String refService = "The service over there";
        final UUID caseId = randomUUID();

        final Feedback feedback = mock(Feedback.class);

        final JsonEnvelope event = JsonEnvelopeBuilder.envelope()
                .with(MetadataBuilderFactory.metadataWithDefaults().createdAt(dateReceived))
                .withPayloadOf(FEEDBACK_ID, "feedbackId")
                .withPayloadOf(rating, "rating")
                .withPayloadOf(comment, "comment")
                .withPayloadOf(refService, "refService")
                .withPayloadOf(refUrl, "refUrl")
                .withPayloadOf(caseId.toString(), "caseId")
                .withPayloadOf(ZonedDateTimes.toString(dateReceived), "dateReceived")
                .build();

        when(feedbackFactory.create(event.payloadAsJsonObject(), FEEDBACK_ID, rating, dateReceived)).thenReturn(feedback);

        supportEventListener.feedbackReceived(event);

        verify(feedbackRepository).save(feedbackArgumentCaptor.capture());

        assertThat(feedbackArgumentCaptor.getValue(), is(feedback));
    }
}
