package uk.gov.moj.cpp.support.query.service;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.of;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.support.entity.Feedback;
import uk.gov.moj.cpp.support.query.reponse.FeedbackView;
import uk.gov.moj.cpp.support.query.reponse.ResponseView;
import uk.gov.moj.cpp.support.repository.FeedbackRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    public void shouldSearchTheRepositoryByDateRangeAndReturnAsAListOfFeedbackViews() throws Exception {

        final ZonedDateTime from = of(2018, 2, 23, 0, 0, 0, 0, UTC);
        final ZonedDateTime to = of(2018, 2, 24, 0, 0, 0, 0, UTC);

        final Feedback feedback = new Feedback(
                randomUUID(),
                "a comment",
                "www.gerritt.com",
                "TechPod",
                1,
                now(),
                randomUUID()
        );

        when(feedbackRepository.findByDateReceivedBetween(from, to)).thenReturn(singletonList(feedback));

        final ResponseView responseView = feedbackService.searchByDateRange(from, to);

        final List<FeedbackView> feedbackViews = responseView.getSearchResults();

        assertThat(feedbackViews.size(), is(1));

        assertThat(feedbackViews.get(0).getFeedbackId(), is(feedback.getFeedbackId()));
        assertThat(feedbackViews.get(0).getComment(), is(feedback.getComment()));
        assertThat(feedbackViews.get(0).getRefService(), is(feedback.getRefService()));
        assertThat(feedbackViews.get(0).getRefUrl(), is(feedback.getRefUrl()));
        assertThat(feedbackViews.get(0).getRating(), is(feedback.getRating()));
        assertThat(feedbackViews.get(0).getDateReceived(), is(feedback.getDateReceived()));
        assertThat(feedbackViews.get(0).getCaseId(), is(feedback.getCaseId()));
    }

    @Test
    public void shouldSearchTheRepositoryByCaseIdAndReturnAsAListOfFeedbackViews() throws Exception {

        final UUID caseId = randomUUID();
        final Feedback feedback = new Feedback(
                randomUUID(),
                "a comment",
                "www.gerritt.com",
                "TechPod",
                1,
                now(),
                caseId
        );

        when(feedbackRepository.findBycaseId(caseId)).thenReturn(singletonList(feedback));

        final ResponseView responseView = feedbackService.searchByCaseId(caseId);

        final List<FeedbackView> feedbackViews = responseView.getSearchResults();

        assertThat(feedbackViews.size(), is(1));

        assertThat(feedbackViews.get(0).getFeedbackId(), is(feedback.getFeedbackId()));
        assertThat(feedbackViews.get(0).getComment(), is(feedback.getComment()));
        assertThat(feedbackViews.get(0).getRefService(), is(feedback.getRefService()));
        assertThat(feedbackViews.get(0).getRefUrl(), is(feedback.getRefUrl()));
        assertThat(feedbackViews.get(0).getRating(), is(feedback.getRating()));
        assertThat(feedbackViews.get(0).getDateReceived(), is(feedback.getDateReceived()));
        assertThat(feedbackViews.get(0).getCaseId(), is(feedback.getCaseId()));
    }
}
