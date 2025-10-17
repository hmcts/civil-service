package uk.gov.hmcts.reform.civil.ga.model;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;

public interface GaLipData {

    YesOrNo getIsGaApplicantLip();

    YesOrNo getIsGaRespondentOneLip();

    YesOrNo getIsGaRespondentTwoLip();

    YesOrNo getIsMultiParty();

    boolean isApplicantBilingual();

    boolean isRespondentBilingual();

    Long getCcdCaseReference();
}
