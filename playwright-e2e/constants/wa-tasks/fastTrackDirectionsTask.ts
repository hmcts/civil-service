import type WATask from '../../models/wa-task';

const task: WATask = {
  name: 'Fast Track Directions',
  type: 'FastTrackDirections',
  task_system: 'SELF',
  security_classification: 'PUBLIC',
  task_title: 'Fast Track Directions',
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
    '[Directions - Fast Track](/cases/case-details/${[CASE_REFERENCE]}/trigger/CREATE_SDO/CREATE_SDOFastTrack)<br /><br />[Not Suitable for SDO](/cases/case-details/${[CASE_REFERENCE]}/trigger/NotSuitable_SDO/NotSuitable_SDONotSuitableSDO)',
  role_category: 'JUDICIAL',
  minor_priority: 500,
  major_priority: 5000,
};

export default task;
