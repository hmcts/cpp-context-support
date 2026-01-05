package uk.gov.moj.cpp.support.integration.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils.stubPingFor;
import static uk.gov.justice.services.common.http.HeaderConstants.ID;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;

import uk.gov.justice.services.test.utils.core.http.RequestParams;

import java.util.UUID;

public class UsersAndGroupsWiremockStub {

    private static final String USERS_GROUPS_PATH = "/usersgroups-service/query/api/rest/usersgroups/users/%s/groups";

    private static final String MIME_TYPE = "application/vnd.usersgroups.groups+json";

    private static final String HOST = getHost();
    private static final int PORT = 8080;
    private static final String USERS_GROUPS_BASE_URL = "http://" + HOST + ":" + PORT;

    private static final String USERS_GROUPS_RESPONSE_BODY =
    "{" +
    "  \"groups\": [" +
    "    {" +
    "      \"groupId\": \"1e2f843e-d639-40b3-8611-8015f3a18958\"," +
    "      \"groupName\": \"%s\"" +
    "    }" +
    "  ]" +
    "}";

    public UsersAndGroupsWiremockStub configure() {
        configureFor(HOST, PORT);
        reset();
        stubPingFor("usersgroups-service");
        return this;
    }

    public UsersAndGroupsWiremockStub stubIsSystemUserCallFor(final UUID userId, final String group) {

        final String url = format(USERS_GROUPS_PATH, userId);
        stubFor(get(urlEqualTo(url))
                .withHeader("Accept", equalTo(MIME_TYPE))
                .willReturn(aResponse()
                        .withStatus(OK.getStatusCode())
                        .withHeader("Content-Type", MIME_TYPE)
                        .withHeader(ID, randomUUID().toString())
                        .withBody(format(USERS_GROUPS_RESPONSE_BODY, group))));

        pollUntilRespondingCorrectly(userId);

        return this;
    }

    private void pollUntilRespondingCorrectly(final UUID userId) {

        final String path = format(USERS_GROUPS_PATH, userId);
        final String url = USERS_GROUPS_BASE_URL + path;
        final RequestParams requestParams = requestParams(
                url,
                MIME_TYPE)
                .build();

        poll(requestParams).until(status().is(OK));
    }
}
