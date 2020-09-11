package uk.gov.hmcts.reform.unspec.sampledata;

import uk.gov.hmcts.reform.unspec.enums.ServiceMethodType;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;

import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.DOCUMENT_EXCHANGE;
import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.EMAIL;
import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.FAX;
import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.OTHER;
import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.POST;

public class ServiceMethodBuilder {

    private ServiceMethodType type;
    private String dxNumber;
    private String faxNumber;
    private String email;
    private String other;

    public static ServiceMethodBuilder builder() {
        return new ServiceMethodBuilder();
    }

    public ServiceMethodBuilder post() {
        type = POST;
        return this;
    }

    public ServiceMethodBuilder email() {
        type = EMAIL;
        email = "service@email.com";
        return this;
    }

    public ServiceMethodBuilder documentExchange() {
        type = DOCUMENT_EXCHANGE;
        dxNumber = "DX123";
        return this;
    }

    public ServiceMethodBuilder fax() {
        type = FAX;
        faxNumber = "020123456789";
        return this;
    }

    public ServiceMethodBuilder other() {
        type = OTHER;
        other = "My other service method name";
        return this;
    }

    public ServiceMethod build() {
        return ServiceMethod.builder()
            .type(type)
            .dxNumber(dxNumber)
            .faxNumber(faxNumber)
            .email(email)
            .other(other)
            .build();
    }
}
