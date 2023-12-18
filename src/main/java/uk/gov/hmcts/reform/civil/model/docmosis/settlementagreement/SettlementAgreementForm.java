package uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@EqualsAndHashCode
public class SettlementAgreementForm implements MappableObject {

    private final String claimReferenceNumber;
    private final LipFormParty claimant;
    private final LipFormParty defendant;
    private final Address claimantCorrespondenceAddress;
    private final Address defendantCorrespondenceAddress;
    private final String totalClaimAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate settlementAgreedDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy 'at' HH:mm a")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime settlementSubmittedDate;
}
