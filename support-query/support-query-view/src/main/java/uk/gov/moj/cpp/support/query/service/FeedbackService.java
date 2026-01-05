package uk.gov.moj.cpp.support.query.service;

import static java.util.stream.Collectors.toList;

import uk.gov.moj.cpp.support.entity.Feedback;
import uk.gov.moj.cpp.support.query.reponse.FeedbackView;
import uk.gov.moj.cpp.support.query.reponse.ResponseView;
import uk.gov.moj.cpp.support.repository.FeedbackRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

public class FeedbackService {

    @Inject
    FeedbackRepository feedbackRepository;

    public ResponseView searchByDateRange(final ZonedDateTime from, final ZonedDateTime to) {
        final List<Feedback> feedbackSearchResults = feedbackRepository.findByDateReceivedBetween(from, to);

        final List<FeedbackView> feedbackViews = feedbackSearchResults.stream()
                .map(this::toFeedbackView)
                .collect(toList());

        return new ResponseView(feedbackViews);
    }

    public ResponseView searchByCaseId(final UUID caseId) {
        final List<Feedback> feedbackSearchResults = feedbackRepository.findBycaseId(caseId);

        final List<FeedbackView> feedbackViews = feedbackSearchResults.stream()
                .map(this::toFeedbackView)
                .collect(toList());

        return new ResponseView(feedbackViews);
    }

    public ResponseView searchByDateRangeAndCaseId(final ZonedDateTime from, final ZonedDateTime to, final UUID caseId) {
        final List<Feedback> feedbackSearchResults = feedbackRepository.findByCaseIdAndDateReceivedBetween(caseId, from, to);

        final List<FeedbackView> feedbackViews = feedbackSearchResults.stream()
                .map(this::toFeedbackView)
                .collect(toList());

        return new ResponseView(feedbackViews);
    }

    private FeedbackView toFeedbackView(final Feedback feedback) {
        return new FeedbackView(
                feedback.getFeedbackId(),
                feedback.getComment(),
                feedback.getRefUrl(),
                feedback.getRefService(),
                feedback.getRating(),
                feedback.getDateReceived(),
                feedback.getCaseId()
        );
    }
}
