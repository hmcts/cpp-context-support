package uk.gov.moj.cpp.support.command.handler.aggregate;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.moj.cpp.support.event.FeedbackReceived;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FeedbackTest {

    @InjectMocks
    private Feedback feedback;

    @Test
    public void shouldCreateAFeedbackReceivedEventAndApplyToTheEventStream() throws Exception {

        final UUID feedbackId = randomUUID();
        final Optional<String> comment = of("comment");
        final Optional<String> refUrl = of("refUrl");
        final Optional<String> refService = of("refService");
        final Integer rating = 23;
        final Optional<UUID> caseId = of(randomUUID());

        final Stream<Object> objectStream = feedback.receiveFeedback(
                feedbackId,
                comment,
                refUrl,
                refService,
                rating,
                caseId
        );

        final List<Object> events = objectStream.collect(toList());

        assertThat(events.size(), is(1));

        final FeedbackReceived feedbackReceived = (FeedbackReceived) events.get(0);

        assertThat(feedbackReceived.getFeedbackId(), is(feedbackId));
        assertThat(feedbackReceived.getComment(), is(comment));
        assertThat(feedbackReceived.getRefUrl(), is(refUrl));
        assertThat(feedbackReceived.getRefService(), is(refService));
        assertThat(feedbackReceived.getRating(), is(rating));
        assertThat(feedbackReceived.getCaseId(), is(of(caseId.get())));
    }

    @Test
    public void shouldHandleAbsentOptionalParameters() throws Exception {

        final UUID feedbackId = randomUUID();
        final Integer rating = 23;

        final Stream<Object> objectStream = feedback.receiveFeedback(
                feedbackId,
                empty(),
                empty(),
                empty(),
                rating,
                empty()
        );

        final List<Object> events = objectStream.collect(toList());

        assertThat(events.size(), is(1));

        final FeedbackReceived feedbackReceived = (FeedbackReceived) events.get(0);

        assertThat(feedbackReceived.getFeedbackId(), is(feedbackId));
        assertThat(feedbackReceived.getComment(), is(empty()));
        assertThat(feedbackReceived.getRefUrl(), is(empty()));
        assertThat(feedbackReceived.getRefService(), is(empty()));
        assertThat(feedbackReceived.getRating(), is(rating));
        assertThat(feedbackReceived.getCaseId(), is(empty()));
    }
}
