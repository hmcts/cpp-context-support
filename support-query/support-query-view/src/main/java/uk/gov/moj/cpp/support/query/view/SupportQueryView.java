package uk.gov.moj.cpp.support.query.view;

import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.UUID.fromString;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.support.query.reponse.ResponseView;
import uk.gov.moj.cpp.support.query.service.FeedbackService;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class SupportQueryView {

    private static final String FROM_PARAM = "from";
    private static final String TO_PARAM = "to";
    private static final String CASE_ID_PARAM = "caseId";

    @Inject
    private Enveloper enveloper;

    @Inject
    private UtcClock clock;

    @Inject
    private FeedbackService feedbackService;

    public JsonEnvelope search(final JsonEnvelope query) {

        final JsonObject payload = query.payloadAsJsonObject();

        final ResponseView responseView;
        if(payload.containsKey(FROM_PARAM)) {
            if(payload.containsKey(CASE_ID_PARAM)) {
                responseView = searchByDateRangeAndCaseId(payload);
            } else {
                responseView = searchByDateRange(payload);
            }
        } else {
            responseView = searchByCaseId(payload);
        }

        return enveloper.withMetadataFrom(query, "support.feedback-search-results").apply(responseView);
    }

    private ResponseView searchByDateRange(final JsonObject payload) {
        final ZonedDateTime from = parse(payload.getString(FROM_PARAM), ISO_OFFSET_DATE_TIME);
        final ZonedDateTime to = getToDate(payload);

        return feedbackService.searchByDateRange(from, to);
    }

    private ResponseView searchByCaseId(final JsonObject payload) {
        final UUID caseId = fromString(payload.getString(CASE_ID_PARAM));

        return feedbackService.searchByCaseId(caseId);
    }

    private ResponseView searchByDateRangeAndCaseId(final JsonObject payload) {
        final ZonedDateTime from = parse(payload.getString(FROM_PARAM), ISO_OFFSET_DATE_TIME);
        final ZonedDateTime to = getToDate(payload);
        final UUID caseId = fromString(payload.getString(CASE_ID_PARAM));

        return feedbackService.searchByDateRangeAndCaseId(from, to, caseId);
    }

    private ZonedDateTime getToDate(final JsonObject payload) {

        if (payload.containsKey(TO_PARAM)) {
            return parse(payload.getString(TO_PARAM), ISO_OFFSET_DATE_TIME);
        }

        return clock.now();
    }
}
