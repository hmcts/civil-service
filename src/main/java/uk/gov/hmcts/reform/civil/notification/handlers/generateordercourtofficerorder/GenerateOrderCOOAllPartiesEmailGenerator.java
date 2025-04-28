package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import lombok.Setter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import javax.annotation.PostConstruct;

@Setter
@Component
public class GenerateOrderCOOAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    protected String taskInfo;

    private final GenerateOrderCOOClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    private final GenerateOrderCOODefendantEmailDTOGenerator defendantEmailDTOGenerator;

    public GenerateOrderCOOAllPartiesEmailGenerator(
        GenerateOrderCOOAppSolEmailDTOGenerator appSolEmailDTOGenerator,
        GenerateOrderCOOResp1EmailDTOGenerator resp1EmailDTOGenerator,
        GenerateOrderCOOResp2EmailDTOGenerator resp2EmailDTOGenerator,
        GenerateOrderCOOClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        GenerateOrderCOODefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(appSolEmailDTOGenerator,
              resp1EmailDTOGenerator,
              resp2EmailDTOGenerator,
              claimantEmailDTOGenerator,
              defendantEmailDTOGenerator);
        this.claimantEmailDTOGenerator = claimantEmailDTOGenerator;
        this.defendantEmailDTOGenerator = defendantEmailDTOGenerator;
    }

    @PostConstruct
    public void init() {
        System.out.println("Initializing with taskInfo: " + taskInfo);
        if (taskInfo != null) {
            claimantEmailDTOGenerator.setTaskInfo(taskInfo);
            defendantEmailDTOGenerator.setTaskInfo(taskInfo);
        }
    }
}
