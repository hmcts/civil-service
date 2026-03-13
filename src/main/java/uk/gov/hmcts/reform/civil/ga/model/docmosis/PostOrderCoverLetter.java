package uk.gov.hmcts.reform.civil.ga.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class PostOrderCoverLetter implements MappableObject {

    private String caseNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate receivedDate;
    private String partyName;
    private String partyAddressAddressLine1;
    private String partyAddressAddressLine2;
    private String partyAddressAddressLine3;
    private String partyAddressPostTown;
    private String partyAddressPostCode;
}
