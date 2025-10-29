# Notification template JSON definitions

Place JSON files in this directory to manage dashboard notification templates. Each file must contain a single template definition in the following format:

```
{
  "name": "Notice.AAA6.Example.Template",
  "role": "CLAIMANT",
  "titleEn": "Example title",
  "titleCy": "Enghraifft o deitl",
  "descriptionEn": "<p class=\"govuk-body\">English body text.</p>",
  "descriptionCy": "<p class=\"govuk-body\">Welsh body text.</p>",
  "timeToLive": "P7D",
  "deadlineParam": "exampleDeadline",
  "delete": false
}
```

Leave optional fields out or set them to `null` when they are not required. Setting `delete` to `true` removes the template from the database during synchronisation.
