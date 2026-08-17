package uk.gov.hmcts.reform.civil.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

public final class PiiRedactingConverter extends CompositeConverter<ILoggingEvent> {

    @Override
    protected String transform(ILoggingEvent event, String message) {
        return PiiRedactor.redact(message);
    }
}
