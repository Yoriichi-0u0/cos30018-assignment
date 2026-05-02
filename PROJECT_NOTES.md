# PROJECT_NOTES

## Current Project Name And Purpose

Velvet Auto Broker Exchange is a COS30018 Intelligent Systems Java Swing + JADE assignment project. It demonstrates brokered automated vehicle negotiation between dealer agents and buyer agents.

## Current Concept/Theme

The visible UI concept is Velvet Auto Broker Exchange: a brokered vehicle negotiation platform with dealer listings, buyer requests, broker matching, negotiation analytics, and a live negotiation feed.

## Main Architecture

- JADE Broker Agent coordinates matching between dealer and buyer agents.
- Dealer Agents publish vehicle listings and respond to negotiation messages using strategies such as Stubborn, Desperate, and Matcher.
- Buyer Agents request matching dealers and negotiate automatically or through the manual negotiation UI.
- The Swing dashboard starts the JADE container, creates agents, shows market boards, displays analytics, and streams negotiation logs.

## Important UI Rules

- Read `PROJECT_NOTES.md` and `TODO.md` at the start of every future Codex session.
- Treat `PROJECT_NOTES.md` as the source of truth.
- Visible wording should describe a broker exchange, not an auction house.
- Do not rename Java classes such as `AuctionDashboard` unless there is a strong reason.
- Setup Agents must keep Broker Platform Control, Agent Management, Dealer Listing Booth, Buyer Bidding Desk, List Vehicle, Start Buyer Agent, and Delete Selected Agent visible.
- Run Demo Lineup must stay on Setup Agents and only instruct the user to open Live Negotiation Feed if they want to observe messages.
- Manual Negotiation UI must be visible and functional when enabled.
- Visual Analytics must keep the KPI cards and charts while supporting All Deals and individual deal inspection.
- Do not push to GitHub from this project unless explicitly requested.

## Important Windows Compatibility Rules

- Force the cross-platform Swing look and feel at startup.
- Do not rely on OS default Swing colors.
- Explicitly style buttons, combo boxes, text fields, check boxes, tables, scroll panes, text panes, text areas, and card panels for foreground, background, disabled foreground, selection colors, borders, opacity, and focus behavior.
- Combo boxes need custom renderers so dropdown items remain readable.
- Disabled buttons and combo boxes must still have readable text.

## What Was Changed

- Rebranded visible UI wording from Velvet Hammer Auto Auction to Velvet Auto Broker Exchange.
- Replaced auction-floor wording with broker platform, dealer listings, buyer requests, live negotiation feed, and negotiation dashboard wording.
- Forced cross-platform Swing look and feel and added explicit component styling for Windows/macOS contrast.
- Expanded demo presets to include Toyota Vios, Honda City, Perodua, Proton, Toyota Corolla Cross, Honda HR-V, Nissan, Mazda, and Ford sample vehicles.
- Run Demo Lineup now adds at most 5 dealer agents and 5 buyer agents per click from reusable preset data and stays on Setup Agents.
- Demo lineup can be clicked repeatedly; each vehicle model/variant is capped at 5 dealer appearances and 5 buyer appearances.
- Demo lineup logs `No more demo lineup presets available.` when every preset variant has reached its cap.
- Added structured deal records, View Deal dropdown, individual deal detail card, and hover/point detail text for the negotiation chart.
- Reworked Manual Negotiation UI into a clear dialog with buyer/dealer/vehicle/round details, log area, counter offer, accept, reject, and close controls.
- Added a no-match manual negotiation dialog and live feed message when no matching dealer exists.
- Fixed dashboard-created JADE dealer/buyer argument wiring so agents receive the expected model arguments.
- Added live feed subscription to `AuctionLog` so JADE agent logs appear in the dashboard.
- Increased the Visual Analytics deal detail panel height and simplified the Strategy label so bottom details are readable.

## Why It Was Changed

- The old auction-house framing did not match the brokered negotiation assignment concept.
- Windows teammates had contrast/readability problems because Swing native defaults can vary by OS and theme.
- The old demo data was too narrow and made analytics less meaningful.
- The first expanded demo pass created too many agents at once, so the lineup now grows in controlled 5 dealer / 5 buyer batches for easier classroom demos and Windows machines.
- The old manual negotiation checkbox did not make an obvious UI appear from the dashboard flow.
- Analytics needed an individual deal inspection path instead of only aggregate charts.

## Files Modified In This Session

- Latest demo-lineup fix changed `src/gui/AuctionDashboard.java`, `PROJECT_NOTES.md`, and `TODO.md`.
- The current local worktree also includes prior UI rebrand/manual analytics edits in:
- `PROJECT_NOTES.md`
- `TODO.md`
- `src/gui/AuctionDashboard.java`
- `src/gui/ManualNegotiationUI.java`
- `src/app/AuctionDashboardApp.java`
- `src/controller/AuctionPlatformController.java`
- `src/agents/DealerAgent.java`
- `src/analytics/AnalyticsStore.java`
- `src/gui/VisualAnalyticsPanel.java`

## UI Or Architecture Decisions

- Kept Java class and package names unchanged to avoid risky rename churn.
- Kept the main dashboard as the source of UI analytics because that is the active panel used by `AuctionDashboard`.
- Kept demo simulation records local to the dashboard while also starting JADE agents, so the UI remains immediately demonstrable and the live feed still receives JADE logs.
- Used `\u2194` in deal labels to match the requested buyer/dealer deal format without renaming agents.
- Disabled Delete Selected Agent when no removable agents exist and styled the disabled state for readability.
- Tracked demo preset usage from current dealer/buyer lists, so manually added agents are not reset and demo generation avoids overusing a vehicle variant.

## Current Known Issues / Limitations

- Full Windows visual validation still needs to be done by a teammate on Windows.
- JADE startup can fail when another running dashboard already owns port 1099; close the existing dashboard before testing Start Broker Platform.
- Scripted delete testing produced a JADE DF warning when a killed dealer attempted to deregister after not being registered; the dashboard removed the agent and counts updated correctly.
- The repository already had local changes in `APDescription.txt` and `src/.DS_Store` before this session. They were unrelated and were not reverted.
- `README.md` still contains older project wording; this session only required final updates to `PROJECT_NOTES.md` and `TODO.md`.

## Testing Notes

- Compiled all Java sources with `javac -cp lib/jade.jar -d out/test-compile $(find src -name '*.java')`.
- Launched `app.AuctionDashboardApp` locally and stopped it after confirming the GUI starts.
- Programmatically exercised Run Demo Lineup batching: 5 dealers/5 buyers after one click, 10 dealers/10 buyers after two clicks, 95 dealers/95 buyers at exhaustion, and Setup Agents stayed active.
- Verified no dealer or buyer vehicle variant exceeded 5 appearances and the no-more-presets message appeared after exhaustion.
- Programmatically selected an individual deal: View Deal had 26 options, selected deal detail status updated to Closed.
- Programmatically enabled Manual Negotiation UI with no matching dealer and confirmed a visible `Manual Negotiation - No Matching Dealer` dialog appeared.
- Programmatically confirmed Delete Selected Agent removed the selected agent after a demo/manual setup smoke test.
