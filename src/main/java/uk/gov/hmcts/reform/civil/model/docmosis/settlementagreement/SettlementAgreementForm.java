package uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SettlementAgreementForm implements MappableObject {

    private String claimReferenceNumber;
    private LipFormParty claimant;
    private LipFormParty defendant;
    private Address claimantCorrespondenceAddress;
    private Address defendantCorrespondenceAddress;
    private String totalClaimAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate settlementAgreedDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy 'at' HH:mm a")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime settlementSubmittedDate;
}
