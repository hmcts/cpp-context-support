package uk.gov.moj.cpp.support.query.reponse;

import java.time.ZonedDateTime;
import java.util.UUID;

public class FeedbackView {

    private final UUID feedbackId;
    private final String comment;
    private final String refUrl;
    private final String refService;
    private final Integer rating;
    private final ZonedDateTime dateReceived;
    private final UUID caseId;

    public FeedbackView(
            final UUID feedbackId,
            final String comment,
            final String refUrl,
            final String refService,
            final Integer rating,
            final ZonedDateTime dateReceived,
            final UUID caseId) {
        this.feedbackId = feedbackId;
        this.comment = comment;
        this.refUrl = refUrl;
        this.refService = refService;
        this.rating = rating;
        this.dateReceived = dateReceived;
        this.caseId = caseId;
    }

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public String getComment() {
        return comment;
    }

    public String getRefUrl() {
        return refUrl;
    }

    public String getRefService() {
        return refService;
    }

    public Integer getRating() {
        return rating;
    }

    public ZonedDateTime getDateReceived() {
        return dateReceived;
    }

    public UUID getCaseId() {
        return caseId;
    }
}
