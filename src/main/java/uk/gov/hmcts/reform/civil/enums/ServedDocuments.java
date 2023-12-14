package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServedDocuments {
    CLAIM_FORM("Claim form"),
    PARTICULARS_OF_CLAIM("Particulars of claim"),
    RESPONSE_PACK("Response pack"),
    MEDICAL_REPORTS("Medical reports"),
    SCHEDULE_LOSS("Schedule of loss"),
    CERTIFICATE_SUITABILITY("Certificate of suitability"),
    OTHER("Other documents");

    private final String label;

}
