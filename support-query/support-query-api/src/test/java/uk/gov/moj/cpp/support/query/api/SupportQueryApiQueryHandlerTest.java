package uk.gov.moj.cpp.support.query.api;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.support.query.view.SupportQueryView;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SupportQueryApiQueryHandlerTest {

    @Mock
    private QueryParameterValidator queryParameterValidator;

    @Mock
    private SupportQueryView supportQueryView;

    @InjectMocks
    private SupportQueryApiQueryHandler supportQueryApiQueryHandler;

    @Test
    public void shouldPassThroughToViewIfTheQueryJsonContainsAFromField() throws Exception {

        final String fromDate = "2018-02-23T17:00:43.511Z";
        final JsonObject payload = createObjectBuilder()
                .add("from", fromDate)
                .build();

        final JsonEnvelope query = mock(JsonEnvelope.class);

        when(query.payloadAsJsonObject()).thenReturn(payload);
        when(queryParameterValidator.isValidIsoDate(fromDate)).thenReturn(true);

        supportQueryApiQueryHandler.findFeedback(query);

        verify(supportQueryView).search(query);
    }

    @Test
    public void shouldPassThroughToViewIfTheQueryJsonContainsACaseIdField() throws Exception {

        final String caseId = randomUUID().toString();
        final JsonObject payload = createObjectBuilder()
                .add("caseId", caseId)
                .build();

        final JsonEnvelope query = mock(JsonEnvelope.class);

        when(query.payloadAsJsonObject()).thenReturn(payload);
        when(queryParameterValidator.isValidUuid(caseId)).thenReturn(true);

        supportQueryApiQueryHandler.findFeedback(query);

        verify(supportQueryView).search(query);
    }

    @Test
    public void shouldThrowABadRequestExceptionIfTheQueryHasNeitherAFromFieldNorACaseIdField() throws Exception {

        final JsonObject payload = createObjectBuilder()
                .build();

        final JsonEnvelope query = mock(JsonEnvelope.class);

        when(query.payloadAsJsonObject()).thenReturn(payload);

        try {
            supportQueryApiQueryHandler.findFeedback(query);
            fail();
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("Query json must contain a 'from' field for serching by date " +
                    "and/or a 'caseId' field for searching by case"));
        }
    }

    @Test
    public void shouldThrowABadRequestExceptionIfTheFromFieldIsNotAnIsoDate() throws Exception {

        final String from = "not a valid iso date";
        final JsonObject payload = createObjectBuilder()
                .add("from", from)
                .build();

        final JsonEnvelope query = mock(JsonEnvelope.class);

        when(query.payloadAsJsonObject()).thenReturn(payload);
        when(queryParameterValidator.isValidIsoDate(from)).thenReturn(false);

        try {
            supportQueryApiQueryHandler.findFeedback(query);
            fail();
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("Parameter 'from' is not a valid ISO date"));
        }
    }

    @Test
    public void shouldThrowABadRequestExceptionIfTheToFieldIsNotAnIsoDate() throws Exception {

        final String from = "2018-02-23T17:00:43.511Z";
        final String to = "not a valid iso date";
        final JsonObject payload = createObjectBuilder()
                .add("from", from)
                .add("to", to)
                .build();

        final JsonEnvelope query = mock(JsonEnvelope.class);

        when(query.payloadAsJsonObject()).thenReturn(payload);
        when(queryParameterValidator.isValidIsoDate(from)).thenReturn(true);
        when(queryParameterValidator.isValidIsoDate(to)).thenReturn(false);

        try {
            supportQueryApiQueryHandler.findFeedback(query);
            fail();
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("Parameter 'to' is not a valid ISO date"));
        }
    }

    @Test
    public void shouldThrowABadRequestExceptionIfTheCaseIdFieldIsNotAUuid() throws Exception {

        final String caseId = "not a uuid";
        final JsonObject payload = createObjectBuilder()
                .add("caseId", caseId)
                .build();

        final JsonEnvelope query = mock(JsonEnvelope.class);

        when(query.payloadAsJsonObject()).thenReturn(payload);
        when(queryParameterValidator.isValidUuid(caseId)).thenReturn(false);

        try {
            supportQueryApiQueryHandler.findFeedback(query);
            fail();
        } catch (final BadRequestException expected) {
            assertThat(expected.getMessage(), is("Parameter 'caseId' is not a valid UUID"));
        }
    }
}
