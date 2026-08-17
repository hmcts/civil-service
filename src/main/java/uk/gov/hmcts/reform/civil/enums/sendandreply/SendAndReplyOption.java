package uk.gov.hmcts.reform.civil.enums.sendandreply;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SendAndReplyOptionsList", generate = true)
public enum SendAndReplyOption {
    @CCD(label = "Send a message")
    SEND,
    @CCD(label = "Reply to a message")
    REPLY
}
