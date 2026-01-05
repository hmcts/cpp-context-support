package uk.gov.moj.cpp.support.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class SupportCommandApi {

    @Inject
    Sender sender;

    @Inject
    Enveloper enveloper;

    @Handles("support.command.api.send-feedback")
    public void sendFeedback(final JsonEnvelope feedbackCommand) {

        final JsonEnvelope envelope = enveloper
                .withMetadataFrom(feedbackCommand, "support.command.handler.send-feedback")
                .apply(feedbackCommand.payload());

        sender.send(envelope);
    }
}
