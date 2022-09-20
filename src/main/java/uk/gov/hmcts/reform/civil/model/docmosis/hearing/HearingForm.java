package uk.gov.hmcts.reform.civil.model.docmosis.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class HearingForm implements MappableObject {

    private final String caseNumber;

}
