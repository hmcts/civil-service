package uk.gov.hmcts.reform.migration.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.migration.domain.common.Party;

@Data
@Builder
@AllArgsConstructor
@SuppressWarnings("ClassTypeParameterName")
public class Respondent<P extends Party> {
    private final P party;
    private final String leadRespondentIndicator;
}
