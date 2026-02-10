#!/usr/bin/env bash

# Setting up existing Users with role assignments
echo ""
echo "Setting up Users with role assignments..."
./bin/utils/organisational-role-assignment.sh "ctsc_admin@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hmcts-ctsc" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "ctsc_admin@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "ctsc_admin@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "ctsc-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"366774","workTypes":"routine_work"}' "CTSC"
./bin/utils/organisational-role-assignment.sh "ctsc_admin@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "ctsc" '{"jurisdiction":"CIVIL","primaryLocation":"366774","workTypes":"routine_work,query_work"}' "CTSC"

./bin/utils/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "circuit-judge" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,decision_making_work,applications,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"access_requests,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "JUDICIAL"

./bin/utils/organisational-role-assignment.sh "DJ.Amy.Powell@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "DJ.Amy.Powell@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,decision_making_work,applications,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "DJ.Amy.Powell@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "JUDICIAL"

./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"routine_work,hearing_work,access_requests"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hmcts-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-centre-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,access_requests,routine_work"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,routine_work,multi_track_hearing_work,intermediate_track_hearing_work,query_work"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "4924246EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"214320"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "4924246EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"214320","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "4924246EMP-@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"214320"}' "JUDICIAL"

./bin/utils/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"231596"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "circuit-judge" '{"jurisdiction":"CIVIL","primaryLocation":"231596","workTypes":"hearing_work,decision_making_work,applications,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"231596","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"231596","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"CIVIL","primaryLocation":"231596","workTypes":"access_requests,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"231596"}' "JUDICIAL"
./bin/utils/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_DEFAULT_PASSWORD}" "PUBLIC" "case-allocator" '{"jurisdiction":"CIVIL","primaryLocation":"231596"}' "JUDICIAL"

./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"routine_work,hearing_work,access_requests"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-centre-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,access_requests"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"


./bin/utils/organisational-role-assignment.sh "tribunal_legal_caseworker_reg4@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hmcts-legal-operations" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "tribunal_legal_caseworker_reg4@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "tribunal_legal_caseworker_reg4@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
./bin/utils/organisational-role-assignment.sh "tribunal_legal_caseworker_reg4@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"CIVIL","primaryLocation":"366774","workTypes":"decision_making_work"}' "LEGAL_OPERATIONS"

./bin/utils/organisational-role-assignment.sh "nbc_team_leader_region4@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "nbc-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "nbc_admin_region1@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "nbc_admin_region1@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "national-business-centre" '{"jurisdiction":"CIVIL","primaryLocation":"20262","region":"1","workTypes":"routine_work,query_work"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "nbc_admin_region2@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"424213","region":"2"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "nbc_admin_region2@justice.gov.uk" "${DEFAULT_PASSWORD}" "PUBLIC" "national-business-centre" '{"jurisdiction":"CIVIL","primaryLocation":"424213","region":"2","workTypes":"routine_work,query_work"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "casewrokerWLU@justice.gov.uk" "Password123" "PUBLIC" "wlu-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"welsh_translation_work"}' "ADMIN"

./bin/utils/organisational-role-assignment.sh "caseworkerwlutl@justice.gov.uk" "Password123" "PUBLIC" "wlu-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"420219","workTypes":"welsh_translation_work"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "caseworkerwlutl@justice.gov.uk" "Password123" "PUBLIC" "wlu-admin" '{"jurisdiction":"CIVIL","primaryLocation":"420219","workTypes":"welsh_translation_work"}' "ADMIN"
./bin/utils/organisational-role-assignment.sh "caseworkerwlutl@justice.gov.uk" "Password123" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"420219","workTypes":"welsh_translation_work"}' "ADMIN"
