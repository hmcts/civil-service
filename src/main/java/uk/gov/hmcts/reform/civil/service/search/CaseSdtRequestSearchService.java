package uk.gov.hmcts.reform.civil.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.bulkclaims.CaseWorkerSearchCaseParams;

import java.util.List;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSdtRequestSearchService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    public List<CaseDetails> searchCaseForSdtRequest(CaseWorkerSearchCaseParams params) {

        return coreCaseDataApi.searchForCaseworker(params.getAuthorisation(),
                                                   authTokenGenerator.generate(),
                                                   params.getUserId(),
                                                   JURISDICTION,
                                                   CASE_TYPE,
                                                   params.getSearchCriteria());
    }
}
