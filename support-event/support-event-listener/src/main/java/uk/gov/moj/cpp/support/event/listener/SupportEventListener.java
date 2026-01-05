package uk.gov.moj.cpp.support.event.listener;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.support.entity.Feedback;
import uk.gov.moj.cpp.support.repository.FeedbackRepository;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(EVENT_LISTENER)
public class SupportEventListener {

    @Inject
    private FeedbackRepository feedbackRepository;

    @Inject
    private FeedbackFactory feedbackFactory;

    @Handles("support.event.feedback-received")
    public void feedbackReceived(final JsonEnvelope event) {
        final Feedback feedback = buildFeedbackFrom(event);

        feedbackRepository.save(feedback);
    }

    private Feedback buildFeedbackFrom(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();

        final UUID feedbackId = fromString(payload.getString("feedbackId"));
        final Integer rating = payload.getInt("rating");
        final ZonedDateTime dateReceived = event.metadata().createdAt().get();

        return feedbackFactory.create(payload, feedbackId, rating, dateReceived);
    }
}
