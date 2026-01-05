package uk.gov.moj.cpp.support.command.handler.aggregate;

import static java.util.stream.Stream.of;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.support.event.FeedbackReceived;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class Feedback implements Aggregate {

    public Stream<Object> receiveFeedback(final UUID feedbackId,
                                          final Optional<String> comment,
                                          final Optional<String> refUrl,
                                          final Optional<String> refService,
                                          final Integer rating,
                                          final Optional<UUID> caseId) {

        final FeedbackReceived feedbackReceived = new FeedbackReceived(caseId,
                comment, feedbackId, rating, refService, refUrl);

        return apply(of(feedbackReceived));
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(when(FeedbackReceived.class).apply(e -> {
        }));
    }
}
