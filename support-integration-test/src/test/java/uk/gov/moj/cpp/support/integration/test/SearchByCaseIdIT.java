package uk.gov.moj.cpp.support.integration.test;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
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
public class SearchByCaseIdIT {

    private static final String GROUP_ONLINE_PLEA_SYSTEM_USERS = "Online Plea System Users";
    private static final String SYSTEM_USERS = "System Users";

    private static final UUID USER_ID_COMMAND = randomUUID();
    private static final UUID USER_ID_QUERY = randomUUID();

    private static final String COMMAND_PATH = "/support-command-api/command/api/rest/support/feedback/%s";
    private static final String COMMAND_URL = getBaseUri() + COMMAND_PATH;
    private static final String COMMAND = "support.command.api.send-feedback";
    private static final String COMMAND_CONTENT_TYPE = "application/vnd." + COMMAND + "+json";

    private static final String CONTEXT_NAME = "support";

    private static final String QUERY_PATH = "/support-query-api/query/api/rest/support/feedback?caseId=%s";
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

        final UUID caseId = randomUUID();
        final UUID otherCaseId = randomUUID();

        final UUID feedbackId_1 = randomUUID();
        final String comment_1 = "comment 1";
        final String refUrl_1 = "refUrl_1";
        final String refService_1 = "refService_1";
        final int rating_1 = 1;

        final String command_1 = buildACommand(comment_1, refUrl_1, refService_1, rating_1, caseId);

        final UUID feedbackId_2 = randomUUID();
        final String comment_2 = "comment 2";
        final String refUrl_2 = "refUrl_2";
        final String refService_2 = "refService_2";
        final int rating_2 = 2;

        final String command_2 = buildACommand(comment_2, refUrl_2, refService_2, rating_2, otherCaseId);

        final UUID feedbackId_3 = randomUUID();
        final String comment_3 = "comment 3";
        final String refUrl_3 = "refUrl_3";
        final String refService_3 = "refService_3";
        final int rating_3 = 3;

        final String command_3 = buildACommand(comment_3, refUrl_3, refService_3, rating_3, caseId);

        final Map<String, Object> commandHeaders = of(HeaderConstants.USER_ID, USER_ID_COMMAND.toString());

        assertThat(restClient.postCommand(
                format(COMMAND_URL, feedbackId_1),
                COMMAND_CONTENT_TYPE,
                command_1,
                new MultivaluedHashMap<>(commandHeaders)).getStatus(), is(ACCEPTED.getStatusCode()));

        assertThat(restClient.postCommand(
                format(COMMAND_URL, feedbackId_2),
                COMMAND_CONTENT_TYPE,
                command_2,
                new MultivaluedHashMap<>(commandHeaders)).getStatus(), is(ACCEPTED.getStatusCode()));

        assertThat(restClient.postCommand(
                format(COMMAND_URL, feedbackId_3),
                COMMAND_CONTENT_TYPE,
                command_3,
                new MultivaluedHashMap<>(commandHeaders)).getStatus(), is(ACCEPTED.getStatusCode()));

        final String queryUrl = format(QUERY_URL, caseId);

        final Map<String, Object> queryHeaders = of(HeaderConstants.USER_ID, USER_ID_QUERY.toString());

        final Optional<String> payloadAsString = poller.pollUntilFound(() -> {
            final Response queryResponse = restClient.query(
                    queryUrl,
                    QUERY_RESPONSE_TYPE,
                    new MultivaluedHashMap<>(queryHeaders));

            assertThat(queryResponse.getStatus(), is(OK.getStatusCode()));

            final String responsePayload = queryResponse.readEntity(String.class);

            if (responsePayload.contains(feedbackId_1.toString()) && responsePayload.contains(feedbackId_3.toString())) {
                return Optional.of(responsePayload);
            }

            return Optional.empty();
        });

        if(payloadAsString.isPresent()) {
            final JsonPath jsonPathPayload = new JsonPath(payloadAsString.get());
            final List<Map<String, String>> results = jsonPathPayload.getJsonObject("searchResults");
            assertThat(results.size(), is(2));

            final Map event_1  = findResult(feedbackId_1, results);
            final Map event_3  = findResult(feedbackId_3, results);

            assertThat(event_1.get("feedbackId"), is(feedbackId_1.toString()));
            assertThat(event_1.get("comment"), is(comment_1));
            assertThat(event_1.get("refUrl"), is(refUrl_1));
            assertThat(event_1.get("refService"), is(refService_1));
            assertThat(event_1.get("rating"), is(rating_1));
            assertThat(event_1.get("caseId"), is(caseId.toString()));

            assertThat(event_3.get("feedbackId"), is(feedbackId_3.toString()));
            assertThat(event_3.get("comment"), is(comment_3));
            assertThat(event_3.get("refUrl"), is(refUrl_3));
            assertThat(event_3.get("refService"), is(refService_3));
            assertThat(event_3.get("rating"), is(rating_3));
            assertThat(event_3.get("caseId"), is(caseId.toString()));
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

    private Map<String, String> findResult(final UUID feedbackId, final List<Map<String, String>> results) {

        for(final Map<String, String> result: results) {
            if (result. get("feedbackId").equals(feedbackId.toString())) {
                return result;
            }
        }
        
        throw new AssertionError(format("Failed to get event with feedbackId '%s'", feedbackId));
    }
}
