package uk.gov.moj.cpp.support.query.api;

import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.UUID.fromString;

import java.time.format.DateTimeParseException;

public class QueryParameterValidator {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean isValidUuid(final String queryParam) {
        try {
            fromString(queryParam);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    public boolean isValidIsoDate(final String queryParam) {

        try {
            parse(queryParam, ISO_OFFSET_DATE_TIME);
        } catch (final DateTimeParseException e) {
            return false;
        }

        return true;
    }
}
