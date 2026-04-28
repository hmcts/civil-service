package uk.gov.hmcts.reform.civil.notification.handlers.translateddocumentuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class TranslatedDocumentUploadedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public TranslatedDocumentUploadedAllPartiesEmailGenerator(
        TranslatedDocumentUploadedAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
        TranslatedDocumentUploadedClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        TranslatedDocumentUploadedDefendantEmailDTOGenerator defendantEmailDTOGenerator) {

        super(List.of(appSolOneEmailGenerator,
                      claimantEmailDTOGenerator,
                      defendantEmailDTOGenerator));
    }
}
