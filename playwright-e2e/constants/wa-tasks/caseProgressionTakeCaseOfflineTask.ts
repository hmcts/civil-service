import type WATask from '../../models/wa-task';

const task: WATask = {
    name: 'Transfer Case Offline',
    type: 'transferCaseOffline',
    task_title: 'Transfer Case Offline',
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
    description: '[Take case offline](/cases/case-details/${[CASE_REFERENCE]}/trigger/TAKE_CASE_OFFLINE/TAKE_CASE_OFFLINE)',
    role_category: 'ADMIN',
    minor_priority: 500,
    major_priority: 5000
};

export default task;
