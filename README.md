# COS30018 - Intelligent Systems: Automated Negotiation System

## Team Members

* **Jonathan Zheng Li CHAI** (Team Lead / Core Architecture)
* **Yuen Kai CHIA** (Agent Communication / Protocols / Visual Analytics)
* **Shan Kai LIU** (Negotiation Strategies / GUI)
* **Stanley Kai Jie LEONG**
* **Edmund Dao Ann LIM**

---

## Project Overview

This project is a multi-agent trading platform developed using **JADE (Java Agent Development Framework)** to support automated negotiation and trading of automotive vehicles.

The system simulates a broker-based car marketplace where dealers list vehicles, buyers request vehicles based on their preferences, and intelligent agents negotiate prices to reach a possible agreement.

The objective of each agent is different:

* **Dealer Agents** aim to maximize profit.
* **Buyer Agents** aim to purchase the desired car at the lowest possible price.
* **Broker Agent (KA)** acts as the central facilitator and earns fixed negotiation fees and commissions from successful deals.

The system provides both **manual negotiation** and **automated negotiation**. The automated negotiation process uses multi-round buyer-dealer bargaining where buyer offers and dealer asking prices gradually move closer until a deal is accepted or the negotiation fails.

---

## System Architecture

The platform uses three main agent roles.

### 1. Broker Agent (KA)

The Broker Agent is the central coordinator of the trading platform.

Responsibilities:

* Receives vehicle listings from Dealer Agents.
* Receives car requests from Buyer Agents.
* Matches buyer requirements with available dealer listings.
* Sends relevant dealer matches back to buyers.
* Forwards buyer interests and first offers to selected dealers.
* Facilitates buyer-dealer negotiation connections.
* Collects a fixed fee for each negotiation connection.
* Collects commission from successful deals.

### 2. Dealer Agents (DA)

Dealer Agents represent car sellers in the system.

Responsibilities:

* Register vehicle listings with the Broker Agent.
* Receive buyer leads and first offers from the Broker Agent.
* Decide whether a buyer lead is reasonable.
* Initiate negotiation with selected buyers.
* Negotiate using a dealer strategy.
* Inform the Broker Agent when a successful deal is completed.

Supported dealer strategies:

* **Stubborn:** Concedes slowly.
* **Desperate:** Concedes quickly.
* **Matcher:** Reacts based on the buyer's concession pattern.

### 3. Buyer Agents (BA)

Buyer Agents represent car buyers in the system.

Responsibilities:

* Submit target car requirements to the Broker Agent.
* Receive matching dealers from the Broker Agent.
* Shortlist up to three dealers for negotiation.
* Send first offers to selected dealers through the broker.
* Negotiate manually or automatically.
* Attempt to secure the target car at the lowest possible price within the buyer's budget.

---

## Features Implemented

### Core Platform

* JADE-based multi-agent system.
* Embedded JADE main container launched from the Java Swing GUI.
* Broker Agent, Dealer Agents, and Buyer Agents.
* Demo lineup for spawning a working scenario.
* Dealer listing input.
* Buyer requirement input.
* Broker matching between buyer requests and dealer listings.
* Dealer lead acceptance and rejection logic.
* Buyer shortlisting of up to three matching dealers.
* Live auction feed for real-time communication updates.

### Negotiation Features

* Manual negotiation mode.
* Automated buyer negotiation.
* Multi-round negotiation.
* Buyer first offer and maximum budget.
* Dealer asking price and minimum acceptable price.
* Dealer concession strategies.
* Buyer opponent-model prediction logic.
* Success, rejection, and walkaway outcomes.

### Visual Analytics Features

* Real-time negotiation line chart.
* Buyer Offer vs Dealer Ask comparison.
* Price gap tracking across negotiation rounds.
* Broker Treasury Dashboard.
* Total negotiations counter.
* Successful deals counter.
* Failed deals counter.
* Success rate display.
* Fixed broker fee tracking.
* Broker commission tracking.
* Total broker revenue display.

### Extension Feature

* Concurrent negotiation support using JADE responder dispatching.
* Buyer Agents can engage with up to three dealers concurrently.

---

## Repository Structure

```text
COS30018-Automated-Negotiation-System/
├── lib/
│   └── jade.jar
├── out/
├── src/
│   ├── agents/
│   │   ├── BrokerAgent.java
│   │   ├── BuyerAgent.java
│   │   └── DealerAgent.java
│   ├── analytics/
│   │   ├── AnalyticsStore.java
│   │   ├── NegotiationRoundRecord.java
│   │   └── TreasurySnapshot.java
│   ├── app/
│   │   └── AuctionDashboardApp.java
│   ├── controller/
│   │   └── AuctionPlatformController.java
│   ├── gui/
│   │   ├── AuctionDashboard.java
│   │   ├── ManualNegotiationUI.java
│   │   └── VisualAnalyticsPanel.java
│   ├── logging/
│   │   ├── AuctionEvent.java
│   │   └── AuctionLog.java
│   └── models/
│       ├── BuyerProfile.java
│       ├── Car.java
│       └── DealerListing.java
├── config.properties
├── README.md
└── project overview.md
```

### Important Files

| File | Purpose |
|---|---|
| `src/app/AuctionDashboardApp.java` | Main entry point of the program |
| `src/gui/AuctionDashboard.java` | Main Swing dashboard |
| `src/controller/AuctionPlatformController.java` | Starts JADE and spawns agents |
| `src/agents/BrokerAgent.java` | Broker Agent logic |
| `src/agents/DealerAgent.java` | Dealer Agent logic |
| `src/agents/BuyerAgent.java` | Buyer Agent logic |
| `src/gui/VisualAnalyticsPanel.java` | Negotiation graph and treasury dashboard |
| `src/analytics/AnalyticsStore.java` | Stores structured analytics data |
| `config.properties` | Stores default configuration values |
| `lib/jade.jar` | JADE library required to run the project |

---

## Requirements

### Software Requirements

* IntelliJ IDEA
* Java JDK 8 or newer
* JADE 4.6
* macOS, Windows, or Linux

### Tested Environment

This project has been tested with:

```text
Operating System: macOS
IDE: IntelliJ IDEA
JDK: Oracle OpenJDK 25.0.2 - aarch64
JADE: JADE 4.6
GUI Framework: Java Swing
```

Although JADE is commonly used with older Java versions, this project can run on newer JDK versions if the JADE library is correctly added.

If your machine has compatibility problems with Java 25, use JDK 8, JDK 11, or JDK 17.

---

## IntelliJ IDEA Configuration Guide

Before running the system, make sure the project is configured correctly in IntelliJ IDEA.

---

### 1. Project SDK Setup

Open:

```text
File > Project Structure > Project
```

Use the following settings:

```text
Project Name:
COS30018-Automated-Negotiation-System

SDK:
JDK 8 or newer

Language Level:
SDK default

Compiler Output:
<project-folder>/out
```

Example compiler output:

```text
~/COS30018/COS30018-Automated-Negotiation-System/out
```

The `out` folder is where IntelliJ stores compiled `.class` files.

---

### 2. Module Source Folder Setup

Open:

```text
File > Project Structure > Modules
```

Select the module:

```text
COS30018-Automated-Negotiation-System
```

Then check that:

* The project root is added as the module content root.
* The `src` folder is marked as **Sources**.

Expected structure:

```text
COS30018-Automated-Negotiation-System/
├── .idea/
├── lib/
├── out/
├── src/        <-- mark this as Sources
├── config.properties
└── README.md
```

If `src` is not marked as Sources:

1. Click the `src` folder.
2. Click **Mark as: Sources**.
3. Click **Apply**.
4. Click **OK**.

This step is required so IntelliJ can correctly detect Java packages such as:

```text
app
agents
analytics
controller
gui
logging
models
```

Without this step, IntelliJ may not be able to find the main class or package files.

---

### 3. JADE Library Setup

Open:

```text
File > Project Structure > Libraries
```

Add the JADE library:

```text
lib/jade.jar
```

Expected JADE library path example:

```text
/Users/chiayuenkai/COS30018/COS30018-Automated-Negotiation-System/lib/jade.jar
```

After adding the library, confirm that it appears under:

```text
Libraries > jade > Classes > lib/jade.jar
```

This step is required because the project uses JADE classes such as:

```java
jade.core.Agent
jade.core.Profile
jade.core.ProfileImpl
jade.core.Runtime
jade.wrapper.AgentContainer
jade.wrapper.AgentController
jade.lang.acl.ACLMessage
jade.proto.ContractNetInitiator
jade.proto.SSResponderDispatcher
```

Without `jade.jar`, the project will show errors such as:

```text
package jade.core does not exist
package jade.lang.acl does not exist
cannot find symbol Agent
```

---

### 4. Run Configuration Setup

Open:

```text
Run > Edit Configurations
```

Create a new **Application** configuration.

Use the following settings:

```text
Name:
Auction Dashboard

Run on:
Local machine

JDK:
Same as Project SDK

Main class:
app.AuctionDashboardApp

Program arguments:
Leave empty

Working directory:
Project root folder
```

Example working directory:

```text
/Users/chiayuenkai/COS30018/COS30018-Automated-Negotiation-System
```

Make sure **Store as project file** is enabled if the team wants to share the run configuration through Git.

The working directory must point to the project root because the program loads:

```text
config.properties
```

from the working directory.

---

## Running the System

After the IntelliJ configuration is complete:

1. Select the run configuration:

```text
Auction Dashboard
```

2. Click **Run**.
3. The main GUI should open with the title:

```text
Velvet Hammer Auto Auction
```

4. Click **Start Auction Floor** to start the embedded JADE main container.
5. Click **Run Demo Lineup** to spawn demo agents.
6. Watch the negotiation process in the Live Auction Feed.
7. View negotiation graphs and broker earnings in the Visual Analytics section.

The demo lineup starts:

* 1 Broker Agent
* At least 3 Dealer Agents
* At least 5 Buyer Agents

The Live Auction Feed will show:

* broker startup
* dealer listings
* buyer requests
* broker matching
* buyer shortlisting
* dealer lead acceptance
* negotiation rounds
* accepted deals
* failed negotiations
* broker fee and commission updates

---

## Configuration File

The project uses:

```text
config.properties
```

to store default system settings.

Example configuration:

```properties
jade_gui=true
default_buyer_budget=128000
max_rounds=5
default_dealer_margin=0.10
```

### Configuration Explanation

| Setting | Purpose |
|---|---|
| `jade_gui=true` | Shows the JADE Remote Agent Management GUI |
| `default_buyer_budget=128000` | Default maximum buyer budget shown in the GUI |
| `max_rounds=5` | Default maximum negotiation rounds |
| `default_dealer_margin=0.10` | Default dealer minimum profit margin |

If `config.properties` is missing or cannot be loaded, the system will use hardcoded default values.

---

## JADE GUI and Sniffer Instructions

For the assignment demo, the JADE Sniffer can be used to visualize ACL message passing between agents.

Make sure this setting exists in `config.properties`:

```properties
jade_gui=true
```

Then follow these steps:

1. Run `AuctionDashboardApp`.
2. Click **Start Auction Floor**.
3. In the JADE Remote Agent Management GUI, right-click:

```text
Main-Container
```

4. Select:

```text
Start Stat. Service > Sniffer
```

5. In the Sniffer window, right-click the canvas.
6. Select **Add Agents**.
7. Move the Broker Agent, Dealer Agents, and Buyer Agents to the right-side list.
8. Click **OK**.
9. Click **Run Demo Lineup** in the dashboard.
10. Observe the ACL message flow.

Example ACL messages:

```text
REQUEST
INFORM
CFP
PROPOSE
ACCEPT_PROPOSAL
REJECT_PROPOSAL
REFUSE
```

This helps demonstrate the implemented interaction protocols during the project presentation.

---

## Negotiation Workflow

The system follows this general workflow:

```text
1. Broker Agent starts.
2. Dealer Agents join the platform.
3. Dealer Agents send vehicle catalogs to the Broker Agent.
4. Buyer Agents join the platform.
5. Buyer Agents send car requirements to the Broker Agent.
6. Broker Agent matches buyer requirements with dealer listings.
7. Broker Agent returns matching dealers to the Buyer Agent.
8. Buyer Agent shortlists up to three dealers.
9. Broker Agent forwards buyer interest and first offer to selected dealers.
10. Dealer Agent decides whether the buyer lead is reasonable.
11. Dealer Agent initiates negotiation with the Buyer Agent.
12. Buyer Agent responds manually or automatically.
13. Negotiation continues for several rounds.
14. Deal is accepted or negotiation fails.
15. Broker Agent records fee and commission.
16. GUI updates the Live Auction Feed and Visual Analytics section.
```

---

## Automated Negotiation Strategy

The automated negotiation uses multi-round bargaining.

### Buyer Agent Strategy

The Buyer Agent considers:

* first offer
* maximum budget
* maximum negotiation rounds
* previous dealer asking price
* dealer concession rate

The buyer attempts to predict the dealer's future lower price and generate a counter-offer that is still within the buyer's budget.

### Dealer Agent Strategy

The Dealer Agent considers:

* list price
* minimum acceptable price
* buyer offer
* dealer strategy type

Supported dealer strategies:

| Strategy | Behavior |
|---|---|
| Stubborn | Reduces price slowly |
| Desperate | Reduces price quickly |
| Matcher | Adjusts based on the buyer's offer movement |

### Negotiation Outcome

A negotiation can end in three ways:

| Outcome | Meaning |
|---|---|
| Accepted | Buyer offer reaches or exceeds dealer minimum acceptable price |
| Rejected | Buyer or dealer rejects the negotiation |
| Walkaway | Maximum rounds reached without agreement |

---

## Visual Analytics

The Visual Analytics feature was added to make the negotiation process easier to understand and explain.

### Negotiation Graph

The negotiation graph shows:

* Buyer Offer
* Dealer Ask
* Negotiation round number
* Price gap between buyer and dealer

The graph demonstrates whether the negotiation gap is narrowing over time.

Example:

```text
Round 0:
Buyer Offer = RM108,000
Dealer Ask = RM128,000
Gap = RM20,000

Round 1:
Buyer Offer = RM112,000
Dealer Ask = RM126,000
Gap = RM14,000

Round 2:
Buyer Offer = RM116,000
Dealer Ask = RM124,000
Gap = RM8,000
```

This visualization helps show how automated negotiation behaves over multiple rounds.

### Broker Treasury Dashboard

The Broker Treasury Dashboard shows:

* total negotiations
* successful deals
* failed deals
* success rate
* total fixed fees
* total commissions
* total broker revenue

Broker revenue is calculated as:

```text
Total Broker Revenue = Fixed Negotiation Fees + Successful Deal Commissions
```

The default commission formula is:

```text
Commission = Final Deal Price × Commission Rate
```

Example:

```text
Final Deal Price = RM120,000
Commission Rate = 2%
Broker Commission = RM2,400
```

This dashboard supports the report's critical analysis section by showing the broker's performance and revenue.

---

## How to Explain the Visual Analytics Contribution

The Visual Analytics section was added because the original Live Auction Feed only showed text-based events. Text logs are useful for debugging, but they do not clearly show negotiation behavior.

The new analytics layer records structured negotiation data, including buyer offer, dealer ask, round number, buyer name, dealer name, session ID, and negotiation status. This data is then used to draw a real-time line chart showing how the buyer offer and dealer ask change across rounds.

The Broker Treasury Dashboard also records fixed negotiation fees, successful deals, failed deals, commissions, total revenue, and success rate. This makes it easier to evaluate how well the broker platform performs.

This improves the system because it turns raw agent messages into understandable visual evidence for the project demo and report.

---

## Presentation and Demo

Video link:

```text
[Insert YouTube or Google Drive Link Here]
```

The 10-minute presentation video should demonstrate:

1. Starting the JADE platform.
2. Spawning the demo lineup.
3. Dealer Agents submitting listings.
4. Buyer Agents submitting car requirements.
5. Broker Agent matching buyers and dealers.
6. Buyer shortlist and dealer lead selection.
7. Automated negotiation process.
8. Manual negotiation mode.
9. Visual Analytics graph.
10. Broker Treasury Dashboard.
11. JADE Sniffer message flow.

---

## Common Setup Problems

### Problem 1: `package jade.core does not exist`

Cause:

```text
jade.jar was not added correctly.
```

Fix:

```text
File > Project Structure > Libraries > + > Java > select lib/jade.jar
```

---

### Problem 2: IntelliJ cannot find `app.AuctionDashboardApp`

Cause:

```text
src is not marked as a source folder.
```

Fix:

```text
File > Project Structure > Modules > select src > Mark as Sources
```

---

### Problem 3: `config.properties` is not loaded

Cause:

```text
The working directory is not set to the project root.
```

Fix:

```text
Run > Edit Configurations > Working directory > select project root folder
```

The project root should contain:

```text
config.properties
README.md
src/
lib/
```

---

### Problem 4: JADE GUI does not appear

Cause:

```text
jade_gui is false or config.properties is not loaded.
```

Fix:

Check `config.properties`:

```properties
jade_gui=true
```

Then restart the application.

---

### Problem 5: Port 1099 is already in use

Cause:

```text
A previous JADE container may still be running.
```

Fix:

1. Stop the previous run in IntelliJ.
2. Close any JADE GUI windows.
3. Run the program again.

If the issue continues, restart IntelliJ or restart the computer.

---

### Problem 6: GUI opens but no agents appear

Possible causes:

* JADE library is not loaded.
* `Start Auction Floor` was not clicked.
* The platform failed to start.
* The Live Auction Feed contains an error message.

Fix:

1. Check the Live Auction Feed.
2. Check IntelliJ console output.
3. Confirm `lib/jade.jar` is added.
4. Confirm the working directory is the project root.
5. Restart the application.

---

## Git and Repository Notes

The source code is maintained on GitHub as required by the assignment.

Repository:

```text
Yoriichi-0u0/cos30018-assignment
```

Recommended `.gitignore` entries:

```gitignore
out/
*.class
.idea/workspace.xml
.idea/tasks.xml
.DS_Store
```

The `out/` folder is generated by IntelliJ and usually should not be committed.

However, project-level IntelliJ run configurations may be committed if the team wants to share the same run setup.

---

## Assignment Mapping

This project addresses the main assignment requirements as follows:

| Assignment Requirement | Project Implementation |
|---|---|
| Multi-agent trading platform | Implemented using JADE |
| Broker Agent (KA) | `BrokerAgent.java` |
| At least 3 Dealer Agents | Demo lineup spawns multiple dealers |
| At least 5 Buyer Agents | Demo lineup spawns multiple buyers |
| Dealer listings | Dealer listing input and catalog messages |
| Buyer requirements | Buyer input and broker match request |
| Broker matching | Broker matches buyer requirements to dealer catalogs |
| Buyer shortlist | Buyer selects up to three matching dealers |
| Dealer selection | Dealer evaluates buyer leads before negotiating |
| Manual negotiation | Manual negotiation mode available |
| Automated negotiation | Buyer and dealer autonomous negotiation |
| Interaction protocols | JADE ACL messages and Contract Net style negotiation |
| GUI | Java Swing dashboard |
| Visualization | Live feed, negotiation graph, treasury dashboard |
| Config file | `config.properties` |
| Working demo | `Run Demo Lineup` button |
| Git repository | GitHub repository maintained |

---

## Main Classes

### `AuctionDashboardApp.java`

Main entry point of the application.

Starts the Swing GUI.

```text
src/app/AuctionDashboardApp.java
```

### `AuctionDashboard.java`

Main GUI dashboard.

Contains:

* control buttons
* dealer form
* buyer form
* showroom table
* buyer table
* live auction feed
* visual analytics section

```text
src/gui/AuctionDashboard.java
```

### `AuctionPlatformController.java`

Controls the JADE platform.

Responsibilities:

* start JADE main container
* launch broker
* launch dealers
* launch buyers
* run demo scenario
* reset platform

```text
src/controller/AuctionPlatformController.java
```

### `BrokerAgent.java`

Main broker agent.

Responsibilities:

* receive dealer catalogs
* receive buyer requests
* match buyers with dealers
* forward buyer interests
* collect fees and commissions

```text
src/agents/BrokerAgent.java
```

### `DealerAgent.java`

Dealer agent.

Responsibilities:

* send catalog to broker
* receive buyer leads
* decide whether to engage buyers
* negotiate with buyers
* inform broker of successful deals

```text
src/agents/DealerAgent.java
```

### `BuyerAgent.java`

Buyer agent.

Responsibilities:

* request matching dealers
* receive broker matches
* shortlist dealers
* negotiate manually or automatically
* use prediction-based counter-offer logic

```text
src/agents/BuyerAgent.java
```

### `VisualAnalyticsPanel.java`

Visual analytics GUI panel.

Responsibilities:

* display negotiation chart
* display broker treasury metrics
* show successful and failed deal counts
* show broker revenue

```text
src/gui/VisualAnalyticsPanel.java
```

### `AnalyticsStore.java`

Central analytics data store.

Responsibilities:

* record negotiation starts
* record negotiation rounds
* record successful deals
* record failed deals
* notify GUI when analytics data changes

```text
src/analytics/AnalyticsStore.java
```

---

## Development Notes

When modifying the project:

1. Commit before making major changes.
2. Keep agent logic separate from GUI logic.
3. Do not parse Live Auction Feed text for important calculations.
4. Store important values in structured model or analytics classes.
5. Use clear commit messages.
6. Test the demo lineup after every major change.
7. Keep the README updated when setup steps change.

Recommended commit message examples:

```text
Add visual analytics data store
Add negotiation line chart panel
Connect dealer negotiation to analytics store
Update README with IntelliJ setup guide
Fix broker treasury revenue tracking
```

---

## Current Status

Current project status:

* JADE platform startup implemented.
* Broker Agent implemented.
* Dealer Agents implemented.
* Buyer Agents implemented.
* Dealer listing input implemented.
* Buyer requirement input implemented.
* Broker matching implemented.
* Automated negotiation implemented.
* Manual negotiation mode implemented.
* Live Auction Feed implemented.
* Visual Analytics planned or implemented depending on branch status.
* Report and presentation video still need to be finalized.

---

## License and Academic Use

This project is developed for:

```text
COS30018 Intelligent Systems
Swinburne University of Technology Sarawak
Semester 1, 2026
```

The project is intended for academic submission and demonstration purposes only.