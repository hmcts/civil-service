package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;

@Component
public class GenerateDJFormAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {

    public GenerateDJFormAllLegalRepsEmailGenerator(
        GenerateDJFormAppSolOneEmailDTOGenerator generateDJFormAppSolOneEmailDTOGenerator,
        GenerateDJFormRespSolOneEmailDTOGenerator generateDJFormRespSolOneEmailDTOGenerator,
        GenerateDJFormRespSolTwoEmailDTOGenerator generateDJFormRespSolTwoEmailDTOGenerator
    ) {
        super(generateDJFormAppSolOneEmailDTOGenerator,
              generateDJFormRespSolOneEmailDTOGenerator,
              generateDJFormRespSolTwoEmailDTOGenerator);
    }

}
