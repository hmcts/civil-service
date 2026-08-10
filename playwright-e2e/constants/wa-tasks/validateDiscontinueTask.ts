import type WATask from '../../models/wa-task';

const task: WATask = {
  name: "Claim Discontinued - Validate discontinuance",
  type: "ValidateDiscontinuanceCTSC",
  task_system: "SELF",
  security_classification: "PUBLIC",
  task_title: "Claim Discontinued - Validate discontinuance",
  location_name: "Civil National Business Centre",
  location: "420219",
  execution_type: "Case Management Task",
  jurisdiction: "CIVIL",
  region: "2",
  case_type_id: "CIVIL",
  case_category: "Civil",
  case_name: "Test Inc & Claim 2",
  auto_assigned: false,
  warnings: false,
  case_management_category: "Civil",
  work_type_id: "routine_work",
  work_type_label: "Routine work",
  description:
    "[Validate Discontinuance](/cases/case-details/${[CASE_REFERENCE]}/trigger/VALIDATE_DISCONTINUE_CLAIM_CLAIMANT/VALIDATE_DISCONTINUE_CLAIM_CLAIMANT)",
  role_category: "CTSC",
  minor_priority: 500,
  major_priority: 3000
};
export default task;
