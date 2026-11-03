# Recovering GA initiation incidents

For `GA_INITIATE_PROCESS_ID`, `caseId` is the parent claim ID and
`generalApplicationCaseId` must be the GA case ID.

1. Identify the stuck process instance and its parent claim.
2. Find the submitted application in the claim's `generalApplications` collection.
   Match its `businessProcess.processInstanceId` to the stuck instance. If it does
   not match (for example, after a manual restart), establish which application
   belongs to the instance before proceeding; do not select an arbitrary link.
3. Check the application's `caseLink.CaseReference` and verify the referenced GA
   case belongs to this application and parent claim.
4. If a GA exists, set the process variable `generalApplicationCaseId` to its ID
   in Cockpit, then retry the failed Link task.
5. If no GA can be found, investigate creation history before arranging recovery
   with the applicant. Do not retry Link using the parent claim ID.

When manually restarting an instance, set `generalApplicationCaseId` before moving
past `CreateGeneralApplicationCase`. Re-running the create step can recover the
link only when the application is FINISHED, has a caseLink and belongs to the
same process instance. Missing or ambiguous matches raise an incident.

This change does not repair existing production instances automatically.
