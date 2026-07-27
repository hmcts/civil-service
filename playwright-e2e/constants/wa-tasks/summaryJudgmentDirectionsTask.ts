import type WATask from '../../models/wa-task';

const task: WATask = {
  name: 'Directions after Judgment (Damages)',
  type: 'summaryJudgmentDirections',
  task_system: 'SELF',
  security_classification: 'PUBLIC',
  task_title: 'Directions after Judgment (Damages)',
  location_name: 'Central London County Court',
  location: '20262',
  execution_type: 'Case Management Task',
  jurisdiction: 'CIVIL',
  region: '1',
  case_type_id: 'CIVIL',
  case_category: 'Civil',
  auto_assigned: false,
  warnings: false,
  case_management_category: 'Civil',
  work_type_id: 'decision_making_work',
  work_type_label: 'Decision-making work',
  description:
    '[Directions after Judgment (Damages)](/cases/case-details/${[CASE_REFERENCE]}/trigger/STANDARD_DIRECTION_ORDER_DJ/STANDARD_DIRECTION_ORDER_DJCaseManagementOrder)<br /><br />[Not Suitable for SDO](/cases/case-details/${[CASE_REFERENCE]}/trigger/NotSuitable_SDO/NotSuitable_SDONotSuitableSDO)',
  role_category: 'JUDICIAL',
  minor_priority: 500,
  major_priority: 5000,
};

export default task;
