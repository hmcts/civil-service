#!/usr/bin/env bash

# Centralized organisational role assignment script.
# This is the single source of truth — consumer repos pull it via bin/pull-latest-civil-shared.sh.
#
# Password variables use fallbacks so the script works across all repo contexts:
#   civil-ccd-definition uses DEFAULT_PASSWORD / JUDGE_DEFAULT_PASSWORD
#   civil-citizen-ui uses CITIZEN_PASSWORD / JUDGE_PASSWORD / WLU_DEFAULT_PASSWORD

dir=$(dirname ${0})

JUDGE_PWD="${JUDGE_PASSWORD:-${JUDGE_DEFAULT_PASSWORD}}"
ADMIN_PWD="${DEFAULT_PASSWORD:-${CITIZEN_PASSWORD}}"
WLU_PWD="${WLU_DEFAULT_PASSWORD:-Password123}"

echo ""
echo "Setting up Users with role assignments..."

# --- CTSC ---

${dir}/organisational-role-assignment.sh "ctsc_admin@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-ctsc" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "CTSC"
${dir}/organisational-role-assignment.sh "ctsc_admin@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "CTSC"
${dir}/organisational-role-assignment.sh "ctsc_admin@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "ctsc-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"366774","workTypes":"routine_work"}' "CTSC"
${dir}/organisational-role-assignment.sh "ctsc_admin@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "ctsc" '{"jurisdiction":"CIVIL","primaryLocation":"366774","workTypes":"routine_work,query_work"}' "CTSC"

${dir}/organisational-role-assignment.sh "ga_ctsc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "ctsc" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,query_work"}' "CTSC"
${dir}/organisational-role-assignment.sh "ga_ctsc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-ctsc" '{"jurisdiction":"CIVIL","primaryLocation":"283922"}' "CTSC"
${dir}/organisational-role-assignment.sh "ga_ctsc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"283922"}' "CTSC"
${dir}/organisational-role-assignment.sh "ga_ctsc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,hearing_work,access_requests"}' "CTSC"
${dir}/organisational-role-assignment.sh "ga_ctsc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "case-allocator" '{"jurisdiction":"CIVIL","primaryLocation":"283922"}' "CTSC"
${dir}/organisational-role-assignment.sh "ga_ctsc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "ctsc-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "CTSC"

# --- JUDICIAL ---

${dir}/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "circuit-judge" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,decision_making_work,applications,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"access_requests,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "case-allocator" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL" '["294"]'
${dir}/organisational-role-assignment.sh "4917924EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "JUDICIAL"

${dir}/organisational-role-assignment.sh "DJ.Amy.Powell@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"739514"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "DJ.Amy.Powell@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"739514","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL" '["294"]'
${dir}/organisational-role-assignment.sh "DJ.Amy.Powell@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"739514"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "DJ.Amy.Powell@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "case-allocator" '{"jurisdiction":"CIVIL","primaryLocation":"739514"}' "JUDICIAL"

${dir}/organisational-role-assignment.sh "4924246EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"214320"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4924246EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"214320","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4924246EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"214320"}' "JUDICIAL"

${dir}/organisational-role-assignment.sh "4924221EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"455174"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4924221EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"455174"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "4924221EMP-@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"455174","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"

${dir}/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"231596"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "circuit-judge" '{"jurisdiction":"CIVIL","primaryLocation":"231596","workTypes":"hearing_work,decision_making_work,applications,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"231596","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"231596","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "leadership-judge" '{"jurisdiction":"CIVIL","primaryLocation":"231596","workTypes":"access_requests,multi_track_decision_making_work,intermediate_track_decision_making_work"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"231596"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "EMP42506@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "case-allocator" '{"jurisdiction":"CIVIL","primaryLocation":"231596"}' "JUDICIAL"

${dir}/organisational-role-assignment.sh "DJ.Angel.Morgan@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hmcts-judiciary" '{"jurisdiction":"CIVIL","primaryLocation":"177463"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "DJ.Angel.Morgan@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"177463"}' "JUDICIAL"
${dir}/organisational-role-assignment.sh "DJ.Angel.Morgan@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "judge" '{"jurisdiction":"CIVIL","primaryLocation":"177463","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL" '["294"]'
${dir}/organisational-role-assignment.sh "DJ.Angel.Morgan@ejudiciary.net" "${JUDGE_PWD}" "PUBLIC" "circuit-judge" '{"jurisdiction":"CIVIL","primaryLocation":"177463","workTypes":"hearing_work,decision_making_work,applications"}' "JUDICIAL"

# --- ADMIN (Hearing Centre) ---

${dir}/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"routine_work,hearing_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-centre-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,access_requests,routine_work"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,routine_work,multi_track_hearing_work,intermediate_track_hearing_work,query_work"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "case-allocator" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"

${dir}/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"routine_work,hearing_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-centre-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-centre-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262","workTypes":"hearing_work"}' "ADMIN"
${dir}/organisational-role-assignment.sh "hearing_center_admin_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"

# --- LEGAL OPERATIONS ---

${dir}/organisational-role-assignment.sh "tribunal_legal_caseworker_reg4@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-legal-operations" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "tribunal_legal_caseworker_reg4@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "tribunal_legal_caseworker_reg4@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "tribunal_legal_caseworker_reg4@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"CIVIL","primaryLocation":"366774","workTypes":"decision_making_work"}' "LEGAL_OPERATIONS"

${dir}/organisational-role-assignment.sh "tribunal_legal_caseworker_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-legal-operations" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "tribunal_legal_caseworker_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "tribunal_legal_caseworker_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"366774"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "tribunal_legal_caseworker_reg2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"CIVIL","primaryLocation":"366774","workTypes":"decision_making_work"}' "LEGAL_OPERATIONS"

${dir}/organisational-role-assignment.sh "ga_tribunal_legal_caseworker_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-legal-operations" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "ga_tribunal_legal_caseworker_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "ga_tribunal_legal_caseworker_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "LEGAL_OPERATIONS"
${dir}/organisational-role-assignment.sh "ga_tribunal_legal_caseworker_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "tribunal-caseworker" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "LEGAL_OPERATIONS"

# --- ADMIN (NBC) ---

${dir}/organisational-role-assignment.sh "nbc_team_leader_region4@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "nbc-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"

${dir}/organisational-role-assignment.sh "nbc_admin_region1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"20262"}' "ADMIN"
${dir}/organisational-role-assignment.sh "nbc_admin_region1@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "national-business-centre" '{"jurisdiction":"CIVIL","primaryLocation":"20262","region":"1","workTypes":"routine_work,query_work"}' "ADMIN"

${dir}/organisational-role-assignment.sh "nbc_admin_region2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"424213","region":"2"}' "ADMIN"
${dir}/organisational-role-assignment.sh "nbc_admin_region2@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "national-business-centre" '{"jurisdiction":"CIVIL","primaryLocation":"424213","region":"2","workTypes":"routine_work,query_work"}' "ADMIN"

${dir}/organisational-role-assignment.sh "ga_nbc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "nbc-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "ga_nbc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "national-business-centre" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "ga_nbc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "ga_nbc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "case-allocator" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "ga_nbc_team_leader_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"

${dir}/organisational-role-assignment.sh "ga_nbc_admin_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "national-business-centre" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "ga_nbc_admin_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-manager" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "ga_nbc_admin_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hearing-viewer" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"
${dir}/organisational-role-assignment.sh "ga_nbc_admin_national@justice.gov.uk" "${ADMIN_PWD}" "PUBLIC" "hmcts-admin" '{"jurisdiction":"CIVIL","primaryLocation":"283922","workTypes":"routine_work,access_requests"}' "ADMIN"

# --- WLU ---

${dir}/organisational-role-assignment.sh "casewrokerWLU@justice.gov.uk" "${WLU_PWD}" "PUBLIC" "wlu-admin" '{"jurisdiction":"CIVIL","primaryLocation":"420219","workTypes":"welsh_translation_work,routine_work,query_work"}' "ADMIN"

${dir}/organisational-role-assignment.sh "caseworkerwlutl@justice.gov.uk" "${WLU_PWD}" "PUBLIC" "wlu-team-leader" '{"jurisdiction":"CIVIL","primaryLocation":"420219","workTypes":"welsh_translation_work,routine_work,query_work"}' "ADMIN"
${dir}/organisational-role-assignment.sh "caseworkerwlutl@justice.gov.uk" "${WLU_PWD}" "PUBLIC" "wlu-admin" '{"jurisdiction":"CIVIL","primaryLocation":"420219","workTypes":"welsh_translation_work,routine_work,query_work"}' "ADMIN"
${dir}/organisational-role-assignment.sh "caseworkerwlutl@justice.gov.uk" "${WLU_PWD}" "PUBLIC" "task-supervisor" '{"jurisdiction":"CIVIL","primaryLocation":"420219","workTypes":"welsh_translation_work,routine_work,query_work"}' "ADMIN"
