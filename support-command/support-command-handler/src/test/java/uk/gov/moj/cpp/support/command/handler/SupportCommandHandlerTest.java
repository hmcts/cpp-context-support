package uk.gov.moj.cpp.support.command.handler;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Stream.of;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.moj.cpp.support.command.handler.aggregate.Feedback;
import uk.gov.moj.cpp.support.event.FeedbackReceived;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SupportCommandHandlerTest {

    @Mock
    private EventSource eventSource;

    @Spy
    @SuppressWarnings("unused")
    private Enveloper enveloper = new EnveloperFactory().createWithEvents(FeedbackReceived.class);

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private SupportCommandHandler supportCommandHandler;

    @Test
    public void shouldParseAJsonObjectIntoASendFeedbackEventAndAddToTheEventStream() throws Exception {

        final UUID feedbackId = randomUUID();
        final Optional<String> comment = Optional.of("comment");
        final Optional<String> refUrl = Optional.of("refUrl");
        final Optional<String> refService = Optional.of("refService");
        final Integer rating = 23;
        final Optional<UUID> caseId = Optional.of(fromString("ed6c8d33-bd2f-4e59-83b7-673de68b3981"));

        final String commandName = "support.command.handler.send-feedback";
        final String eventName = "support.event.feedback-received";

        final JsonEnvelope commandJson = envelopeFrom(
                metadataWithRandomUUID(commandName),
                createObjectBuilder()
                        .add("feedbackId", feedbackId.toString())
                        .add("comment", comment.get())
                        .add("refUrl", refUrl.get())
                        .add("refService", refService.get())
                        .add("rating", rating)
                        .add("caseId", caseId.get().toString())
                        .build()
        );

        final FeedbackReceived feedbackReceived = new FeedbackReceived(caseId,
                comment, feedbackId, rating, refService, refUrl);

        final EventStream eventStream = mock(EventStream.class);
        final Feedback feedback = mock(Feedback.class);

        when(eventSource.getStreamById(feedbackId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Feedback.class)).thenReturn(feedback);

        when(feedback.receiveFeedback(
                feedbackId,
                comment,
                refUrl,
                refService,
                rating,
                caseId
        )).thenReturn(of(feedbackReceived));

        supportCommandHandler.sendFeedback(commandJson);

        final JsonEnvelope jsonEnvelope = getJsonEnvelopeFrom(eventStream);
        assertThat(jsonEnvelope.metadata().name(), is(eventName));

        assertThat(jsonEnvelope, is(notNullValue()));
        final String json = jsonEnvelope.toDebugStringPrettyPrint();

        with(json)
                .assertThat("$.feedbackId", is(feedbackId.toString()))
                .assertThat("$.comment", is(comment.get()))
                .assertThat("$.refUrl", is(refUrl.get()))
                .assertThat("$.refService", is(refService.get()))
                .assertThat("$.rating", is(rating))
        ;
    }

    @Test
    public void shouldHandleASendCommentEventWithOptionaParametersAbsent() throws Exception {

        final UUID feedbackId = randomUUID();
        final Optional<String> comment = Optional.empty();
        final Optional<String> refUrl = Optional.empty();
        final Optional<String> refService = Optional.empty();
        final Integer rating = 23;
        final Optional<UUID> caseId = Optional.empty();

        final String commandName = "support.command.handler.send-feedback";
        final String eventName = "support.event.feedback-received";

        final JsonEnvelope commandJson = envelopeFrom(
                metadataWithRandomUUID(commandName),
                createObjectBuilder()
                        .add("feedbackId", feedbackId.toString())
                        .add("rating", rating)
                        .build()
        );

        final FeedbackReceived feedbackReceived = new FeedbackReceived(caseId,
                comment, feedbackId, rating, refService, refUrl);

        final EventStream eventStream = mock(EventStream.class);
        final Feedback feedback = mock(Feedback.class);

        when(eventSource.getStreamById(feedbackId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Feedback.class)).thenReturn(feedback);

        when(feedback.receiveFeedback(
                feedbackId,
                comment,
                refUrl,
                refService,
                rating,
                caseId
        )).thenReturn(of(feedbackReceived));

        supportCommandHandler.sendFeedback(commandJson);

        final JsonEnvelope jsonEnvelope = getJsonEnvelopeFrom(eventStream);
        assertThat(jsonEnvelope.metadata().name(), is(eventName));

        assertThat(jsonEnvelope, is(notNullValue()));
        final String json = jsonEnvelope.toDebugStringPrettyPrint();

        with(json)
                .assertThat("$.feedbackId", is(feedbackId.toString()))
                .assertNotDefined("$.comment")
                .assertNotDefined("$.refUrl")
                .assertNotDefined("$.refService")
                .assertThat("$.rating", is(rating))
        ;
    }

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent", "ConstantConditions"})
    private JsonEnvelope getJsonEnvelopeFrom(final EventStream eventStream) throws EventStreamException {
        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);
        verify(eventStream).append(argumentCaptor.capture());
        final Stream<JsonEnvelope> envelopeStream = argumentCaptor.getValue();

        return envelopeStream.findFirst().get();
    }
}
