package uk.gov.moj.cpp.support.integration.test;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.moj.cpp.support.integration.helpers.UsersAndGroupsWiremockStub;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("Duplicates")
public class SendFeedbackIT {

    private static final String GROUP_ONLINE_PLEA_SYSTEM_USERS = "Online Plea System Users";
    private static final String SYSTEM_USERS = "System Users";

    private static final UUID USER_ID_COMMAND = randomUUID();
    private static final UUID USER_ID_QUERY = randomUUID();

    private static final String COMMAND_PATH = "/support-command-api/command/api/rest/support/feedback/%s";
    private static final String COMMAND_URL = getBaseUri() + COMMAND_PATH;
    private static final String COMMAND = "support.command.api.send-feedback";
    private static final String COMMAND_CONTENT_TYPE = "application/vnd." + COMMAND + "+json";

    private static final String CONTEXT_NAME = "support";

    private static final String QUERY_PATH = "/support-query-api/query/api/rest/support/feedback?from=%S&to=%s";
    private static final String QUERY_URL = getBaseUri() + QUERY_PATH;
    private static final String QUERY_RESPONSE_TYPE = "application/vnd.support.feedback-search-results+json";

    private final RestClient restClient = new RestClient();

    private final UsersAndGroupsWiremockStub usersAndGroupsWiremockStub = new UsersAndGroupsWiremockStub();
    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();

    private final Poller poller = new Poller();

    @BeforeEach
    public void setupUsersAndGroupsStub() throws Exception {
        usersAndGroupsWiremockStub
                .configure()
                .stubIsSystemUserCallFor(USER_ID_COMMAND, GROUP_ONLINE_PLEA_SYSTEM_USERS)
                .stubIsSystemUserCallFor(USER_ID_QUERY, SYSTEM_USERS);
    }

    @BeforeEach
    public void cleanEventLogTable() {
        databaseCleaner.cleanEventLogTable(CONTEXT_NAME);
        databaseCleaner.cleanProcessedEventTable(CONTEXT_NAME);
        databaseCleaner.cleanViewStoreTables(CONTEXT_NAME, "feedback");
    }

    @Test
    public void shouldSuccessfullySendASendFeedbackCommand() throws Exception {

        final UUID feedbackId = randomUUID();

        final String comment = "The best web site I have ever seen.";
        final String refUrl = "www.gerritt.com";
        final String refService = "ATCM";
        final int rating = 5;
        final UUID caseId = randomUUID();

        final String command = buildACommand(comment, refUrl, refService, rating, caseId);

        final Map<String, Object> commandHeaders = of(HeaderConstants.USER_ID, USER_ID_COMMAND.toString());

        final Response response = restClient.postCommand(
                format(COMMAND_URL, feedbackId),
                COMMAND_CONTENT_TYPE,
                command,
                new MultivaluedHashMap<>(commandHeaders));

        assertThat(response.getStatus(), is(ACCEPTED.getStatusCode()));

        final ZonedDateTime from = now(UTC).minusHours(1);
        final ZonedDateTime to = now(UTC).plusHours(1);
        final String queryUrl = format(QUERY_URL, from, to);

        final Map<String, Object> queryHeaders = of(HeaderConstants.USER_ID, USER_ID_QUERY.toString());

        final Optional<String> payloadAsString = poller.pollUntilFound(() -> {
            final Response queryResponse = restClient.query(
                    queryUrl,
                    QUERY_RESPONSE_TYPE,
                    new MultivaluedHashMap<>(queryHeaders));

            assertThat(queryResponse.getStatus(), is(OK.getStatusCode()));

            final String responsePayload = queryResponse.readEntity(String.class);

            if (responsePayload.contains(feedbackId.toString())) {
                return Optional.of(responsePayload);
            }

            return Optional.empty();
        });

        if(payloadAsString.isPresent()) {
            final JsonPath jsonPathPayload = new JsonPath(payloadAsString.get());
            final List<Map> results = jsonPathPayload.getJsonObject("searchResults");
            assertThat(results.size(), is(1));

            final Map feedback = results.get(0);
            assertThat(feedback.get("feedbackId"), is(feedbackId.toString()));
            assertThat(feedback.get("comment"), is(comment));
            assertThat(feedback.get("refUrl"), is(refUrl));
            assertThat(feedback.get("refService"), is(refService));
            assertThat(feedback.get("rating"), is(rating));
            assertThat(feedback.get("caseId"), is(caseId.toString()));
        } else {
            fail();
        }
    }

    @Test
    public void shouldSuccessfullySendASendFeedbackCommandWithOnlyTheMandatoryFields() throws Exception {
        final UUID feedbackId = randomUUID();
        final int rating = 5;

        final String command = buildACommandWithOnlyMandatoryFields(rating);

        final Map<String, Object> headers = of(HeaderConstants.USER_ID, USER_ID_COMMAND.toString());

        final Response response = restClient.postCommand(format(COMMAND_URL, feedbackId), COMMAND_CONTENT_TYPE, command, new MultivaluedHashMap<>(headers));
        assertThat(response.getStatus(), is(ACCEPTED.getStatusCode()));

        final ZonedDateTime from = now(UTC).minusHours(1);
        final ZonedDateTime to = now(UTC).plusHours(1);
        final String queryUrl = format(QUERY_URL, from, to);

        final Map<String, Object> queryHeaders = of(HeaderConstants.USER_ID, USER_ID_QUERY.toString());

        final Optional<String> payloadAsString = poller.pollUntilFound(() -> {
            final Response queryResponse = restClient.query(
                    queryUrl,
                    QUERY_RESPONSE_TYPE,
                    new MultivaluedHashMap<>(queryHeaders));

            assertThat(queryResponse.getStatus(), is(OK.getStatusCode()));

            final String responsePayload = queryResponse.readEntity(String.class);

            if (responsePayload.contains(feedbackId.toString())) {
                return Optional.of(responsePayload);
            }

            return Optional.empty();
        });

        if(payloadAsString.isPresent()) {
            final JsonPath jsonPathPayload = new JsonPath(payloadAsString.get());
            final List<Map> results = jsonPathPayload.getJsonObject("searchResults");
            assertThat(results.size(), is(1));

            final Map feedback = results.get(0);
            assertThat(feedback.get("feedbackId"), is(feedbackId.toString()));
            assertThat(feedback.get("rating"), is(rating));
            assertThat(feedback.containsKey("comment"), is(false));
            assertThat(feedback.containsKey("refUrl"), is(false));
            assertThat(feedback.containsKey("refService"), is(false));
            assertThat(feedback.containsKey("caseId"), is(false));
        } else {
            fail();
        }
    }

    @SuppressWarnings("deprecation")
    private String buildACommand(
            final String comment,
            final String refUrl,
            final String refService,
            final int rating,
            final UUID caseId) {


        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataWithRandomUUID(COMMAND)
                        .withUserId(USER_ID_COMMAND.toString()),
                createObjectBuilder()
                        .add("comment", comment)
                        .add("refUrl", refUrl)
                        .add("refService", refService)
                        .add("rating", rating)
                        .add("caseId", caseId.toString())
                        .build());

        return jsonEnvelope.toDebugStringPrettyPrint();
    }

    @SuppressWarnings("deprecation")
    private String buildACommandWithOnlyMandatoryFields(final int rating) {

        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataWithRandomUUID(COMMAND)
                        .withUserId(USER_ID_COMMAND.toString()),
                createObjectBuilder()
                        .add("rating", rating)
                        .build());

        return jsonEnvelope.toDebugStringPrettyPrint();
    }
}
