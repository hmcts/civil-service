const {assert} = require('chai');
const taskFieldsToBeValidated = {
    taskInitiationFields: [
        'name',
        'type',
        'task_title',
    ],
    taskConfigurationFields: [
        'location_name',
        'location',
        'execution_type',
        'jurisdiction',
        'region',
        'case_type_id',
        'case_category',
        'auto_assigned',
        'case_management_category',
        'work_type_id',
        'work_type_label',
        'description',
        'role_category'
    ],
    taskPermissionFields: [
        'permissions'
    ],
    taskPriorityFields: [
        'minor_priority',
        'major_priority'
    ]
};

module.exports = {
    validateTaskInfo: (createdTask, expectedTaskInfo) => {
        if(expectedTaskInfo && createdTask) {
            for (let taskDMN of Object.keys(taskFieldsToBeValidated)) {
                console.log(`asserting dmn info: ${taskDMN} has valid data`);
                taskFieldsToBeValidated[taskDMN].forEach(
                    fieldsToBeValidated  => {
                        assert.deepEqual(createdTask[fieldsToBeValidated], expectedTaskInfo[fieldsToBeValidated]);
                    }
                );
            }
        }
    }
};
