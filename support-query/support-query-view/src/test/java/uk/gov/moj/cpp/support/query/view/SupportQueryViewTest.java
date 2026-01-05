package uk.gov.moj.cpp.support.query.view;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.moj.cpp.support.query.reponse.FeedbackView;
import uk.gov.moj.cpp.support.query.reponse.ResponseView;
import uk.gov.moj.cpp.support.query.service.FeedbackService;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SupportQueryViewTest {

    @Spy
    @SuppressWarnings("unused")
    private Enveloper enveloper = new EnveloperFactory().create();

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private SupportQueryView supportQueryView;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFindFeedbackByDateRange() throws Exception {

        final ZonedDateTime from = of(2018, 2, 23, 0, 0, 0, 0, UTC);
        final ZonedDateTime to = of(2018, 2, 24, 0, 0, 0, 0, UTC);

        final ZonedDateTime dateRecieved = of(2018, 2, 23, 12, 0, 0, 0, UTC);
        final FeedbackView feedbackView = aFeedbackView(dateRecieved, randomUUID());

        final ResponseView responseView = new ResponseView(singletonList(feedbackView));

        final JsonEnvelope query = envelope().with(metadataWithDefaults())
                .withPayloadOf("2018-02-23T00:00:00.000Z", "from")
                .withPayloadOf("2018-02-24T00:00:00.000Z", "to")
                .build();

        when(feedbackService.searchByDateRange(from, to)).thenReturn(responseView);

        final JsonEnvelope response = supportQueryView.search(query);

        assertThat(response, jsonEnvelope(
                metadata().withName("support.feedback-search-results"),
                payloadIsJson(
                        allOf(
                                withJsonPath("$.searchResults[0].feedbackId", equalTo(feedbackView.getFeedbackId().toString())),
                                withJsonPath("$.searchResults[0].dateReceived", equalTo("2018-02-23T12:00:00.000Z")),
                                withJsonPath("$.searchResults[0].caseId", equalTo(feedbackView.getCaseId().toString())),
                                withJsonPath("$.searchResults[0].comment", equalTo(feedbackView.getComment())),
                                withJsonPath("$.searchResults[0].rating", equalTo(feedbackView.getRating())),
                                withJsonPath("$.searchResults[0].refUrl", equalTo(feedbackView.getRefUrl())),
                                withJsonPath("$.searchResults[0].refService", equalTo(feedbackView.getRefService()))
                        ))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldUseCurrentTimeIfSearchingByDateRangeDoesNotHaveAToField() throws Exception {

        final ZonedDateTime from = of(2018, 2, 23, 0, 0, 0, 0, UTC);
        final ZonedDateTime to = of(2018, 2, 24, 0, 0, 0, 0, UTC);

        final ZonedDateTime dateRecieved = of(2018, 2, 23, 12, 0, 0, 0, UTC);
        final FeedbackView feedbackView = aFeedbackView(dateRecieved, randomUUID());

        final ResponseView responseView = new ResponseView(singletonList(feedbackView));

        final JsonEnvelope query = envelope().with(metadataWithDefaults())
                .withPayloadOf("2018-02-23T00:00:00.000Z", "from")
                .build();

        when(clock.now()).thenReturn(to);
        when(feedbackService.searchByDateRange(from, to)).thenReturn(responseView);

        final JsonEnvelope response = supportQueryView.search(query);

        assertThat(response, jsonEnvelope(
                metadata().withName("support.feedback-search-results"),
                payloadIsJson(
                        allOf(
                                withJsonPath("$.searchResults[0].feedbackId", equalTo(feedbackView.getFeedbackId().toString())),
                                withJsonPath("$.searchResults[0].dateReceived", equalTo("2018-02-23T12:00:00.000Z")),
                                withJsonPath("$.searchResults[0].caseId", equalTo(feedbackView.getCaseId().toString())),
                                withJsonPath("$.searchResults[0].comment", equalTo(feedbackView.getComment())),
                                withJsonPath("$.searchResults[0].rating", equalTo(feedbackView.getRating())),
                                withJsonPath("$.searchResults[0].refUrl", equalTo(feedbackView.getRefUrl())),
                                withJsonPath("$.searchResults[0].refService", equalTo(feedbackView.getRefService()))
                        ))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFindFeedbackByCaseId() throws Exception {

        final UUID caseId = randomUUID();

        final ZonedDateTime dateRecieved = of(2018, 2, 23, 12, 0, 0, 0, UTC);
        final FeedbackView feedbackView = aFeedbackView(dateRecieved, caseId);

        final ResponseView responseView = new ResponseView(singletonList(feedbackView));

        final JsonEnvelope query = envelope().with(metadataWithDefaults())
                .withPayloadOf(caseId.toString(), "caseId")
                .build();

        when(feedbackService.searchByCaseId(caseId)).thenReturn(responseView);

        final JsonEnvelope response = supportQueryView.search(query);

        assertThat(response, jsonEnvelope(
                metadata().withName("support.feedback-search-results"),
                payloadIsJson(
                        allOf(
                                withJsonPath("$.searchResults[0].feedbackId", equalTo(feedbackView.getFeedbackId().toString())),
                                withJsonPath("$.searchResults[0].dateReceived", equalTo("2018-02-23T12:00:00.000Z")),
                                withJsonPath("$.searchResults[0].caseId", equalTo(feedbackView.getCaseId().toString())),
                                withJsonPath("$.searchResults[0].comment", equalTo(feedbackView.getComment())),
                                withJsonPath("$.searchResults[0].rating", equalTo(feedbackView.getRating())),
                                withJsonPath("$.searchResults[0].refUrl", equalTo(feedbackView.getRefUrl())),
                                withJsonPath("$.searchResults[0].refService", equalTo(feedbackView.getRefService()))
                        ))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFindFeedbackByDateRangeAndCaseId() throws Exception {

        final ZonedDateTime from = of(2018, 2, 23, 0, 0, 0, 0, UTC);
        final ZonedDateTime to = of(2018, 2, 24, 0, 0, 0, 0, UTC);
        final UUID caseId = randomUUID();

        final ZonedDateTime dateRecieved = of(2018, 2, 23, 12, 0, 0, 0, UTC);
        final FeedbackView feedbackView = aFeedbackView(dateRecieved, randomUUID());

        final ResponseView responseView = new ResponseView(singletonList(feedbackView));

        final JsonEnvelope query = envelope().with(metadataWithDefaults())
                .withPayloadOf("2018-02-23T00:00:00.000Z", "from")
                .withPayloadOf("2018-02-24T00:00:00.000Z", "to")
                .withPayloadOf(caseId.toString(), "caseId")
                .build();

        when(feedbackService.searchByDateRangeAndCaseId(from, to, caseId)).thenReturn(responseView);

        final JsonEnvelope response = supportQueryView.search(query);

        assertThat(response, jsonEnvelope(
                metadata().withName("support.feedback-search-results"),
                payloadIsJson(
                        allOf(
                                withJsonPath("$.searchResults[0].feedbackId", equalTo(feedbackView.getFeedbackId().toString())),
                                withJsonPath("$.searchResults[0].dateReceived", equalTo("2018-02-23T12:00:00.000Z")),
                                withJsonPath("$.searchResults[0].caseId", equalTo(feedbackView.getCaseId().toString())),
                                withJsonPath("$.searchResults[0].comment", equalTo(feedbackView.getComment())),
                                withJsonPath("$.searchResults[0].rating", equalTo(feedbackView.getRating())),
                                withJsonPath("$.searchResults[0].refUrl", equalTo(feedbackView.getRefUrl())),
                                withJsonPath("$.searchResults[0].refService", equalTo(feedbackView.getRefService()))
                        ))));
    }


    private FeedbackView aFeedbackView(final ZonedDateTime dateRecieved, final UUID caseId) {
        return new FeedbackView(
                randomUUID(),
                "a comment",
                "www.gerritt.com",
                "TechPod",
                1,
                dateRecieved,
                caseId
        );
    }
}
