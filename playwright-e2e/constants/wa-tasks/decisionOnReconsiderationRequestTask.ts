import type WATask from '../../models/wa-task';

const task: WATask = {
  name: "Decision on Reconsideration Request",
  type: "JudgeDecideOnReconsiderRequest",
  task_system: "SELF",
  security_classification: "PUBLIC",
  task_title: "Decision on Reconsideration Request",
  location_name: "Central London County Court",
  location: "20262",
  execution_type: "Case Management Task",
  jurisdiction: "CIVIL",
  region: "1",
  case_type_id: "CIVIL",
  case_category: "Civil",
  auto_assigned: false,
  warnings: false,
  case_management_category: "Civil",
  work_type_id: "routine_work",
  work_type_label: "Routine work",
  description:
    "[Decision on Reconsideration Request](/cases/case-details/${[CASE_REFERENCE]}/trigger/DECISION_ON_RECONSIDERATION_REQUEST/DECISION_ON_RECONSIDERATION_REQUEST)",
  role_category: "JUDICIAL",
  minor_priority: 500,
  major_priority: 5000
}
export default task;
