import type WATask from '../../models/wa-task';

const task: WATask = {
  name: 'Confirm Case Offline',
  type: 'transferCaseOfflineNotSuitableSDO',
  task_title: 'Confirm Case Offline',
  location_name: 'Central London County Court',
  location: '20262',
  execution_type: 'Case Management Task',
  jurisdiction: 'CIVIL',
  region: '1',
  case_type_id: 'CIVIL',
  case_category: 'Civil',
  auto_assigned: false,
  case_management_category: 'Civil',
  work_type_id: 'hearing_work',
  work_type_label: 'Hearing work',
  description: '[Case is Offline]',
  role_category: 'ADMIN',
  minor_priority: 500,
  major_priority: 5000
};

export default task;
