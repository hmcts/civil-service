import type WATask from '../../models/wa-task';

const task: WATask = {
  name: 'Order Made - Review case - Multi track',
  type: 'reviewOrder',
  task_system: 'SELF',
  security_classification: 'PUBLIC',
  task_title: 'Order Made - Review case - Multi track',
  location_name: 'Central London County Court',
  location: '20262',
  execution_type: 'Case Management Task',
  jurisdiction: 'CIVIL',
  region: '1',
  case_type_id: 'CIVIL',
  case_category: 'Civil',
  case_name: 'Test Inc',
  auto_assigned: false,
  warnings: false,
  case_management_category: 'Civil',
  work_type_id: 'routine_work',
  work_type_label: 'Routine work',
  description: '[Confirm order review](/cases/case-details/${[CASE_REFERENCE]}/trigger/CONFIRM_ORDER_REVIEW/CONFIRM_ORDER_REVIEW)',
  role_category: 'ADMIN',
  minor_priority: 500,
  major_priority: 5000
};

export default task;
