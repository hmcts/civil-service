import type WATask from '../../models/wa-task';

const task: WATask = {
  name: "Set Aside - Take Case Offline",
  type: "JudgmentOnlineSetAsideTakeCaseOffline",
  task_system: "SELF",
  security_classification: "PUBLIC",
  task_title: "Set Aside - Take Case Offline",
  location_name: "Civil National Business Centre",
  location: "420219",
  execution_type: "Case Management Task",
  jurisdiction: "CIVIL",
  region: "2",
  case_type_id: "CIVIL",
  case_category: "Civil",
  auto_assigned: false,
  warnings: false,
  case_management_category: "Civil",
  work_type_id: "routine_work",
  work_type_label: "Routine work",
  description: "[SetAside - Take Case Offline](/cases/case-details/${[CASE_REFERENCE]}/trigger/CASE_PROCEEDS_IN_CASEMAN/CASE_PROCEEDS_IN_CASEMAN)",
  role_category: "ADMIN",
  minor_priority: 500,
  major_priority: 3000,
};

export default task;
