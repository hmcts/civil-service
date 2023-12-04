package uk.gov.hmcts.reform.civil.service.pininpost;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.civil.utils.AccessCodeGenerator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefendantPinToPostLRspecService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CUIIdamClientService cuiIdamClientService;
    private static final int EXPIRY_PERIOD = 180;
    private static final int OCMC_PIN_LENGTH = 8;

    public void validatePin(CaseDetails caseDetails, String pin, String... legacyCaseRef) {
        log.info("Validate Pin called..");
        if (!pin.isEmpty() && pin.length() == OCMC_PIN_LENGTH) {
            log.info("Its a OCMC claim");
            int response = cuiIdamClientService.authenticatePinUser(pin, legacyCaseRef[0]);
            log.info("Valid Pin : " + response);
            if (response != HttpStatus.OK.value()) {
                log.error("Pin does not match or expired for {}", legacyCaseRef[0]);
                throw new PinNotMatchException();
            }
            log.info("Valid OCMC claim..");
        } else {
            log.info("Its a CUI claim..");
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
            DefendantPinToPostLRspec pinInPostData = caseData.getRespondent1PinToPostLRspec();
            if (pinInPostData == null || pinInPostData.getAccessCode() == null
                || !pinInPostData.getAccessCode().equals(pin)
                || pinInPostData.getExpiryDate().isBefore(LocalDate.now())) {
                log.error("Pin does not match or expired for {}", caseData.getLegacyCaseReference());
                throw new PinNotMatchException();
            }
            log.info("Valid CUI Pin");
        }
        log.info("Pin is valid hence moving fwd");
    }

    public void removePinInPostData(Long caseId, DefendantPinToPostLRspec pinInPostData) {
        try {
            DefendantPinToPostLRspec updatePinInPostData = DefendantPinToPostLRspec.builder()
                .citizenCaseRole(pinInPostData.getCitizenCaseRole())
                .respondentCaseRole(pinInPostData.getRespondentCaseRole())
                .expiryDate(pinInPostData.getExpiryDate()).build();

            Map<String, Object> data = new HashMap<>();
            data.put("respondent1PinToPostLRspec", updatePinInPostData);
            coreCaseDataService.triggerEvent(caseId, UPDATE_CASE_DATA, data);
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
}
