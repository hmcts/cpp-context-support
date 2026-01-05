package uk.gov.moj.cpp.support.command.handler;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.support.command.handler.aggregate.Feedback;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(COMMAND_HANDLER)
public class SupportCommandHandler {

    @Inject
    EventSource eventSource;

    @Inject
    Enveloper enveloper;

    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    AggregateService aggregateService;

    @Handles("support.command.handler.send-feedback")
    public void sendFeedback(final JsonEnvelope feedbackCommand) throws EventStreamException {

        final JsonObject payload = feedbackCommand.payloadAsJsonObject();

        final UUID feedbackId = fromString(payload.getString("feedbackId"));
        final Optional<String> comment = getOptionalValue("comment", payload);
        final Optional<String> refUrl = getOptionalValue("refUrl", payload);
        final Optional<String> refService = getOptionalValue("refService", payload);
        final Integer rating = payload.getInt("rating");
        final Optional<UUID> caseId = getOptionalValue("caseId", payload).map(UUID::fromString);

        final EventStream eventStream = eventSource.getStreamById(feedbackId);
        final Feedback feedback = aggregateService.get(eventStream, Feedback.class);

        final Stream<JsonEnvelope> events = feedback.receiveFeedback(
                feedbackId,
                comment,
                refUrl,
                refService,
                rating,
                caseId
        ).map(enveloper.withMetadataFrom(feedbackCommand));

        eventStream.append(events);
    }

    private Optional<String> getOptionalValue(final String key, final JsonObject jsonObject) {

        if (jsonObject.containsKey(key)) {
            return of(jsonObject.getString(key));
        }

        return empty();
    }
}
