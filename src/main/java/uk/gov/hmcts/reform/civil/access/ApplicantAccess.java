package uk.gov.hmcts.reform.civil.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.ccd.sdk.api.Permission.CRU;
import static uk.gov.hmcts.reform.civil.enums.UserRole.SOLICITOR;
import static uk.gov.hmcts.reform.civil.enums.UserRole.CASE_WORKER;

public class ApplicantAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(SOLICITOR, CRU);
        grants.putAll(CASE_WORKER, CRU);
        return grants;
    }
}
