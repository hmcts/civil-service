package uk.gov.hmcts.reform.civil.model.docmosis.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class HearingForm implements MappableObject {

    private final String claimReferenceNumber;
    private final CorrectEmail emailAddress;
    private final String hearingFee;
    private final LocalDate hearingDate;
    private final String hearingTime;
    private final String deadlineDate;
    private final String claimantReferenceNumber;
    private final String defendantReferenceNumber;

}
