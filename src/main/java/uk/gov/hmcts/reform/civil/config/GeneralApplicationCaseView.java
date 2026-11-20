package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.reform.civil.enums.CaseState;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

/**
 * Returns decentralised GENERALAPPLICATION cases to CCD as they were stored, as CCD returns the cases it keeps. Typing the view as
 * a map keeps case data out of this service's models, which do not round-trip every field.
 */
@Component
@ConditionalOnProperty(name = "civil.decentralised.enabled", havingValue = "true")
public class GeneralApplicationCaseView implements CaseView<Map<String, Object>, CaseState> {

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(GENERALAPPLICATION_CASE_TYPE);
    }

    @Override
    public Map<String, Object> getCase(CaseViewRequest<CaseState> request, Map<String, Object> storedCase) {
        return storedCase;
    }
}
