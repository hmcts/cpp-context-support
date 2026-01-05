package uk.gov.moj.cpp.support.query.api;

import static uk.gov.justice.services.core.annotation.Component.QUERY_API;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.support.query.view.SupportQueryView;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(QUERY_API)
public class SupportQueryApiQueryHandler {

    @Inject
    private QueryParameterValidator queryParameterValidator;

    @Inject
    private SupportQueryView supportQueryView;

    @Handles("support.find-feedback")
    public JsonEnvelope findFeedback(final JsonEnvelope query) {

        final JsonObject payload = query.payloadAsJsonObject();

        if (!payload.containsKey("from") && !payload.containsKey("caseId")) {
            throw new BadRequestException(
                    "Query json must contain a 'from' field for serching by date " +
                            "and/or a 'caseId' field for searching by case"
            );
        }

        if (!hasValidFromDate(payload)) {
            throw new BadRequestException("Parameter 'from' is not a valid ISO date");
        }

        if (!hasValidToDate(payload)) {
            throw new BadRequestException("Parameter 'to' is not a valid ISO date");
        }

        if (!hasValidCaseId(payload)) {
            throw new BadRequestException("Parameter 'caseId' is not a valid UUID");
        }

        return supportQueryView.search(query);
    }

    private boolean hasValidFromDate(final JsonObject payload) {
        return (!payload.containsKey("from")) ||
                queryParameterValidator.isValidIsoDate(payload.getString("from"));
    }

    private boolean hasValidToDate(final JsonObject payload) {
        return (!payload.containsKey("to")) ||
                queryParameterValidator.isValidIsoDate(payload.getString("to"));
    }

    private boolean hasValidCaseId(final JsonObject payload) {
        return (!payload.containsKey("caseId")) ||
                queryParameterValidator.isValidUuid(payload.getString("caseId"));
    }
}
