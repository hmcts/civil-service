package uk.gov.hmcts.reform.civil.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.ccd.sdk.api.Permission.CRUD;
import static uk.gov.hmcts.reform.civil.enums.UserRole.CASE_WORKER_CAA;

public class CaseworkerCaaAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CASE_WORKER_CAA, CRUD);
        return grants;
    }
}
