package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PostOrderCoverLetter implements MappableObject {

    private final String caseNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate receivedDate;
    private final String partyName;
    private final String partyAddressAddressLine1;
    private final String partyAddressAddressLine2;
    private final String partyAddressAddressLine3;
    private final String partyAddressPostTown;
    private final String partyAddressPostCode;
}
