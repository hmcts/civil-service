package uk.gov.hmcts.reform.civil.service.referencedata;

public class LocationRefDataException  extends RuntimeException {

    public LocationRefDataException(String message) {
        super(message);
    }

    public LocationRefDataException(Exception cause) {
        super(cause);
    }

}
