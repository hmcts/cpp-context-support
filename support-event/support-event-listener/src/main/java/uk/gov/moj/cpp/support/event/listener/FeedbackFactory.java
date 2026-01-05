package uk.gov.moj.cpp.support.event.listener;

import static java.util.UUID.fromString;

import uk.gov.moj.cpp.support.entity.Feedback;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;

public class FeedbackFactory {

    public Feedback create(
            final JsonObject payload,
            final UUID feedbackId,
            final Integer rating,
            final ZonedDateTime dateReceived) {

        final Feedback feedback = new Feedback(feedbackId, rating, dateReceived);

        if (payload.containsKey("comment")) {
            feedback.setComment(payload.getString("comment"));
        }

        if (payload.containsKey("refUrl")) {
            feedback.setRefUrl(payload.getString("refUrl"));
        }

        if (payload.containsKey("refService")) {
            feedback.setRefService(payload.getString("refService"));
        }

        if (payload.containsKey("caseId")) {
            feedback.setCaseId(fromString(payload.getString("caseId")));
        }

        return feedback;
    }
}
