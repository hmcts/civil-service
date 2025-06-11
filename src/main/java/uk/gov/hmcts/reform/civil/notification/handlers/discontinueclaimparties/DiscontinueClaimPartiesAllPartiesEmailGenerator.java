package uk.gov.hmcts.reform.civil.notification.handlers.discontinueclaimparties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class DiscontinueClaimPartiesAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    private final DiscontinueClaimPartiesAppSolOneEmailDTOGenerator discontinueClaimPartiesAppSolOneEmailDTOGenerator;
    private final DiscontinueClaimPartiesRespSolOneEmailDTOGenerator discontinueClaimPartiesRespSolOneEmailDTOGenerator;
    private final DiscontinueClaimPartiesRespSolTwoEmailDTOGenerator discontinueClaimPartiesRespSolTwoEmailDTOGenerator;

    public DiscontinueClaimPartiesAllPartiesEmailGenerator(
        DiscontinueClaimPartiesAppSolOneEmailDTOGenerator discontinueClaimPartiesAppSolOneEmailDTOGenerator,
        DiscontinueClaimPartiesRespSolOneEmailDTOGenerator discontinueClaimPartiesRespSolOneEmailDTOGenerator,
        DiscontinueClaimPartiesRespSolTwoEmailDTOGenerator discontinueClaimPartiesRespSolTwoEmailDTOGenerator
    ) {
        super(List.of(discontinueClaimPartiesAppSolOneEmailDTOGenerator,
                      discontinueClaimPartiesRespSolOneEmailDTOGenerator,
                      discontinueClaimPartiesRespSolTwoEmailDTOGenerator));
        this.discontinueClaimPartiesAppSolOneEmailDTOGenerator = discontinueClaimPartiesAppSolOneEmailDTOGenerator;
        this.discontinueClaimPartiesRespSolOneEmailDTOGenerator = discontinueClaimPartiesRespSolOneEmailDTOGenerator;
        this.discontinueClaimPartiesRespSolTwoEmailDTOGenerator = discontinueClaimPartiesRespSolTwoEmailDTOGenerator;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());

        // Notify Defendant 1
        partiesToEmail.add(discontinueClaimPartiesRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));

        // Check if Defendant 1 is legally represented
        if (caseData.getRespondent1Represented() == YesOrNo.YES) {
            partiesToEmail.add(discontinueClaimPartiesAppSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        }

        // Check if Defendant 2 exists and is legally represented
        if (caseData.getRespondent2() != null && caseData.getRespondent2Represented() == YesOrNo.YES) {
            partiesToEmail.add(discontinueClaimPartiesRespSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        }

        return partiesToEmail;
    }

}
