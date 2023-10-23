package uk.gov.hmcts.reform.civil.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.bulkclaims.CaseWorkerSearchCaseParams;

import java.util.Arrays;
import java.util.List;
import com.google.common.collect.Lists;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class CaseSdtRequestSearchServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    AuthTokenGenerator authTokenGenerator;
    @InjectMocks
    private CaseSdtRequestSearchService caseSdtRequestSearchService;

    @Test
    void shouldReturnCaseDetailsListSuccessfully_whenCaseExits() {

        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        given(coreCaseDataApi.searchForCaseworker(any(), any(), any(), any(),
                                                  any(), any()))
            .willReturn(Arrays.asList(caseDetails));

        given(authTokenGenerator.generate()).willReturn("some random token");
        List<CaseDetails> result = caseSdtRequestSearchService
            .searchCaseForSdtRequest(CaseWorkerSearchCaseParams.builder().build());

        assertThat(result).isNotNull();

    }

    @Test
    void shouldReturnEmptyCaseDetailsListSuccessfully_whenCaseNotExits() {

        given(coreCaseDataApi.searchForCaseworker(any(), any(), any(), any(),
                                                  any(), any()))
            .willReturn(Lists.newArrayList());

        given(authTokenGenerator.generate()).willReturn("some random token");
        List<CaseDetails> result = caseSdtRequestSearchService
            .searchCaseForSdtRequest(CaseWorkerSearchCaseParams.builder().build());

        assertThat(result.isEmpty());

    }
}
