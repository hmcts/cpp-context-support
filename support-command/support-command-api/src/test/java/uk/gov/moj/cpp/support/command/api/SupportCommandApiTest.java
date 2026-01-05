package uk.gov.moj.cpp.support.command.api;

import static com.jayway.jsonassert.JsonAssert.with;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SupportCommandApiTest {

    @Mock
    private Sender sender;

    @Spy
    @SuppressWarnings("unused")
    private Enveloper enveloper = new EnveloperFactory().create();

    @InjectMocks
    private SupportCommandApi supportCommandApi;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Test
    public void shouldPassThroughTheSendFeedbackCommandToTheCommandHandler() throws Exception {

        final JsonEnvelope feedbackCommand = buildAFeedbackCommand();

        supportCommandApi.sendFeedback(feedbackCommand);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final JsonEnvelope jsonEnvelope = envelopeArgumentCaptor.getValue();

        assertThat(jsonEnvelope.metadata().name(), is("support.command.handler.send-feedback"));

        final String json = jsonEnvelope.payload().toString();

        with(json)
                .assertThat("comment", is("The best web site I have ever seen."))
                .assertThat("refUrl", is("www.gerritt.com"))
                .assertThat("refService", is("ATCM"))
                .assertThat("rating", is(1))
        ;
    }

    private JsonEnvelope buildAFeedbackCommand() {
        return envelopeFrom(
                metadataWithRandomUUID("support.command.api.send-feedback"),
                createObjectBuilder()
                        .add("comment", "The best web site I have ever seen.")
                        .add("refUrl", "www.gerritt.com")
                        .add("refService", "ATCM")
                        .add("rating", 1)
                        .build());
    }
}
