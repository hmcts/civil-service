package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ConsentOrderForm implements MappableObject {

    private final String claimNumber;
    private final YesOrNo isMultiParty;
    private final String defendant1Name;
    private final String defendant2Name;
    private final String claimant1Name;
    private final String claimant2Name;
    private final String consentOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate orderDate;
    private final String courtName;
    private final String locationName;
    private final String siteName;
    private final String address;
    private final String postcode;

}
