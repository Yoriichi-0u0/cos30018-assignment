# TODO

## Completed Tasks

- Created `PROJECT_NOTES.md` and `TODO.md` before Java edits.
- Rebranded visible UI wording to Velvet Auto Broker Exchange.
- Forced cross-platform Swing look and feel at startup.
- Added explicit readable styling for buttons, combo boxes, text fields, check boxes, tables, scroll panes, text panes/text areas, and cards.
- Made disabled Delete Selected Agent state readable and disabled it when no removable agents exist.
- Updated Run Demo Lineup so it stays on Setup Agents and logs a message directing users to Live Negotiation Feed.
- Added richer reusable demo vehicle presets and generated clean dealer/buyer names.
- Added structured deal records, View Deal dropdown, individual deal detail card, and chart point inspection text.
- Rebuilt the Manual Negotiation UI into a visible functional dialog with counter, accept, reject, and close controls.
- Added a friendly no-match manual negotiation dialog and live feed message.
- Fixed dashboard-created JADE dealer/buyer argument arrays.
- Completed final Run Demo Lineup fix: each click adds only 5 dealers and 5 buyers unless preset capacity is nearly exhausted.
- Added per-variant dealer/buyer caps of 5 and a clear no-more-presets live feed message.
- Improved Visual Analytics Deal Detail panel readability.
- Compiled the whole project successfully.
- Ran automated GUI smoke checks for app launch, demo batching/exhaustion, individual deal selection, manual no-match UI, and delete-agent behavior.

## In-Progress Tasks

- None for this session.

## Remaining Tasks

- Have a Windows teammate run the GUI and confirm button/combo/table contrast in their environment.
- Manually inspect the visual layout at small and large window sizes on both macOS and Windows.
- Optionally update `README.md` to match the Velvet Auto Broker Exchange wording.
- Use the 5 dealer / 5 buyer demo batches during presentation rather than loading every preset at once.

## Bugs To Verify

- Confirm no Windows-specific look and feel color overrides leak through on JComboBox dropdowns.
- Confirm Windows teammates can run repeated 5/5 demo batches without UI clipping or slowdown.
- Confirm the JADE DF warning seen during scripted delete testing does not affect normal demo grading.
- Confirm real manual JADE negotiation prompts do not feel redundant when the dashboard-assisted manual flow is used.

## Final Push Checklist

- Confirm `javac -cp lib/jade.jar -d out/test-compile $(find src -name '*.java')` still passes.
- Confirm Run Demo Lineup no longer creates all demo agents at once.
- Confirm Windows teammate visual/readability smoke test passes.
- Confirm no unintended config files were changed.
- Do not push to GitHub until the above checks pass.

## Final Testing Checklist

- Start Broker Platform.
- Run Demo Lineup and confirm Setup Agents remains selected.
- Open Live Negotiation Feed and confirm logs are visible.
- Open Market Boards and confirm dealer/buyer rows are listed.
- Open Visual Analytics and confirm KPI cards and charts update.
- Select an individual deal and confirm chart/details update.
- Start a buyer with Enable Manual Negotiation UI checked and confirm the manual UI appears.
- Delete an agent and confirm tables/counts update.
- Test smaller and larger window sizes.
- Repeat visual contrast checks on Windows.
