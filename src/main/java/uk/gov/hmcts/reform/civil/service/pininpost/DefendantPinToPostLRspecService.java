package uk.gov.hmcts.reform.civil.service.pininpost;

import feign.FeignException;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.civil.utils.AccessCodeGenerator;
import uk.gov.hmcts.reform.cmc.model.DefendantLinkStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefendantPinToPostLRspecService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CUIIdamClientService cuiIdamClientService;
    private final ClaimStoreService claimStoreService;
    private static final int EXPIRY_PERIOD = 180;

    public void validatePin(CaseDetails caseDetails, String pin) {
        log.info("Validate Pin called..");
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        DefendantPinToPostLRspec pinInPostData = caseData.getRespondent1PinToPostLRspec();
        if (pinInPostData == null || pinInPostData.getAccessCode() == null
            || !pinInPostData.getAccessCode().equals(pin)
            || pinInPostData.getExpiryDate().isBefore(LocalDate.now())) {
            log.error("Pin does not match or expired for {}", caseData.getLegacyCaseReference());
            throw new PinNotMatchException();
        }
    }

    public Map<String, Object> removePinInPostData(CaseDetails caseDetails) {
        try {
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            DefendantPinToPostLRspec pinInPostData = caseData.getRespondent1PinToPostLRspec();
            DefendantPinToPostLRspec updatePinInPostData = DefendantPinToPostLRspec.builder()
                .citizenCaseRole(pinInPostData.getCitizenCaseRole())
                .respondentCaseRole(pinInPostData.getRespondentCaseRole())
                .expiryDate(pinInPostData.getExpiryDate()).build();

            Map<String, Object> data = new HashMap<>();
            data.put("respondent1PinToPostLRspec", updatePinInPostData);
            return data;
        } catch (FeignException e) {
            log.error(String.format("Updating case data failed: %s", e.contentUTF8()));
            throw e;
        }
    }

    public DefendantPinToPostLRspec buildDefendantPinToPost() {
        LocalDate expiryDate = LocalDate.now().plusDays(EXPIRY_PERIOD);
        return DefendantPinToPostLRspec.builder()
            .accessCode(AccessCodeGenerator.generateAccessCode())
            .respondentCaseRole(
                CaseRole.DEFENDANT.getFormattedName())
            .expiryDate(expiryDate)
            .build();
    }

    public DefendantPinToPostLRspec resetPinExpiryDate(DefendantPinToPostLRspec pinInPostData) {
        LocalDate expiryDate = LocalDate.now().plusDays(EXPIRY_PERIOD);
        return DefendantPinToPostLRspec.builder()
            .expiryDate(expiryDate)
            .citizenCaseRole(pinInPostData.getCitizenCaseRole())
            .respondentCaseRole(pinInPostData.getRespondentCaseRole())
            .accessCode(pinInPostData.getAccessCode()).build();
    }

    public String validateOcmcPin(String pin, String caseReference) {
        log.info("Validate ocmc Pin called..");
        Response response = cuiIdamClientService.authenticatePinUser(pin, caseReference);
        log.info("valid Pin : " + response.status());
        if (response.status() == HttpStatus.FOUND.value()) {
            log.info("It's a valid ocmc claim..");
            return response.headers().get("Location").stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing 'Location' header"));
        } else {
            log.error("Pin does not match or expired for {}, pin {}", caseReference, pin);
            throw new PinNotMatchException();
        }
    }

    public boolean isOcmcDefendantLinked(String caseReference) {
        DefendantLinkStatus status = claimStoreService.isOcmcDefendantLinked(caseReference);
        log.info("ocmc case reference {} defendent status is {}", caseReference, status.isLinked());
        return status.isLinked();
    }

    public boolean isDefendantLinked(CaseDetails caseDetails) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        IdamUserDetails defendantUserDetails = caseData.getDefendantUserDetails();
        return defendantUserDetails != null && defendantUserDetails.getId() != null && defendantUserDetails.getEmail() != null;
    }
}
