package uk.gov.hmcts.reform.civil.model;


import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.DefenceType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;

@Getter
public class Response {
    public RespondentResponseType responseType;
    public PaymentDeclaration paymentDeclaration;
    public DefenceType defenceType;
}
