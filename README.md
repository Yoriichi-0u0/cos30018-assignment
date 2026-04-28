# COS30018 Intelligent Systems - Velvet Hammer Auto Auction

## Project Overview

Velvet Hammer Auto Auction is a JADE-based automated negotiation system developed for COS30018 Intelligent Systems.

The system simulates a broker-based car auction platform where dealer agents list vehicles, buyer agents search for matching vehicles, and agents negotiate prices automatically or manually. The platform uses a Java Swing dashboard to control the JADE agent system, display live negotiation messages, show dealer and buyer boards, and visualize negotiation analytics.

## Team Members

- Jonathan Zheng Li CHAI
- Yuen Kai CHIA
- Shan Kai LIU
- Stanley Kai Jie LEONG
- Edmund Dao Ann LIM

## Main Functions

### Agent Roles

- Broker Agent
  - Starts the auction floor.
  - Receives dealer listings.
  - Receives buyer requests.
  - Matches buyers with suitable dealers.
  - Tracks fixed fees, commissions, revenue, and deal outcomes.

- Dealer Agents
  - Register vehicle listings.
  - Set asking price and minimum margin.
  - Negotiate with buyer agents.
  - Use different negotiation strategies.

- Buyer Agents
  - Submit target vehicle requirements.
  - Set opening bid, maximum budget, and maximum rounds.
  - Negotiate with matching dealer agents.
  - Can use automated or manual negotiation mode.

## Implemented Features

- JADE multi-agent platform
- Java Swing dashboard
- Broker, dealer, and buyer agents
- Dealer listing form
- Buyer bidding form
- Agent deletion function
- Demo lineup function
- Live auction feed
- Market boards for dealers and buyers
- Automated negotiation
- Manual negotiation option
- Visual analytics dashboard
- Broker treasury tracking
- Success rate, revenue, commission, fixed fee, and latest gap display
- Negotiation gap chart
- Dealer strategies:
  - Stubborn
  - Desperate
  - Matcher

## Project Structure

```text
cos30018-assignment/
├── lib/
│   └── jade.jar
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
└── .gitignore
```

## Requirements

- Java JDK 8 or newer
- IntelliJ IDEA
- JADE 4.6
- `jade.jar` added as a project library

## Setup in IntelliJ IDEA

### 1. Open the project

Open the project folder in IntelliJ IDEA.

### 2. Set the Project SDK

Go to:

```text
File > Project Structure > Project
```

Set the SDK to Java JDK 8 or newer.

Recommended:

```text
JDK 8, JDK 11, or JDK 17
```

### 3. Mark `src` as Sources

Go to:

```text
File > Project Structure > Modules
```

Make sure the `src` folder is marked as:

```text
Sources
```

### 4. Add JADE library

Go to:

```text
File > Project Structure > Libraries
```

Add:

```text
lib/jade.jar
```

The project needs this library because it uses JADE classes such as:

```text
jade.core.Agent
jade.wrapper.AgentContainer
jade.lang.acl.ACLMessage
```

### 5. Create Run Configuration

Go to:

```text
Run > Edit Configurations
```

Create a new Application configuration.

Use:

```text
Name: Auction Dashboard
Main class: app.AuctionDashboardApp
Working directory: project root folder
```

The working directory should be the folder that contains:

```text
config.properties
README.md
src/
lib/
```

## Configuration

The system uses `config.properties` for default settings.

Example:

```properties
jade_gui=true
default_buyer_budget=125000
max_rounds=5
default_dealer_margin=0.10
```

| Setting | Description |
|---|---|
| `jade_gui=true` | Shows the JADE GUI |
| `default_buyer_budget=125000` | Default buyer maximum budget |
| `max_rounds=5` | Default maximum negotiation rounds |
| `default_dealer_margin=0.10` | Default dealer minimum margin |

## How to Run

1. Run `app.AuctionDashboardApp`.
2. Click `Start Auction Floor`.
3. Add dealer agents manually or click `Run Demo Lineup`.
4. Add buyer agents manually or use the demo lineup.
5. Open `Live Feed` to view real-time agent communication.
6. Open `Market Boards` to view dealer and buyer agents.
7. Open `Visual Analytics` to view negotiation performance.
8. Use `Agent Management` to delete selected dealer or buyer agents without resetting the full platform.

## Demo Scenario

The demo lineup uses Toyota Vios and Honda City examples.

Example dealer agents:

```text
ViosDealer01
ViosDealer02
ViosDealer03
CityDealer01
CityDealer02
CityDealer03
```

Example buyer agents:

```text
ViosBuyer01
ViosBuyer02
ViosBuyer03
CityBuyer01
CityBuyer02
CityBuyer03
```

The demo shows:

- Dealer listing registration
- Buyer request submission
- Broker matching
- Buyer and dealer negotiation
- Successful deal recording
- Broker fee and commission tracking
- Visual analytics update

## Dealer Strategies

### Stubborn

The dealer reduces the asking price slowly. This strategy tries to protect profit and is less willing to concede.

### Desperate

The dealer reduces the asking price quickly. This strategy increases the chance of closing a deal but may reduce profit.

### Matcher

The dealer adjusts based on the buyer's offer behavior. This strategy reacts more flexibly to the buyer's negotiation pattern.

## Manual Negotiation Mode

The `Enable Manual Negotiation UI` checkbox allows the buyer negotiation to be handled manually.

When enabled:

1. The buyer agent starts the negotiation.
2. A manual negotiation window opens.
3. The user can review the dealer's response.
4. The user can decide the next buyer offer.
5. The negotiation continues until accepted, rejected, or the maximum round limit is reached.

When disabled:

1. The buyer agent negotiates automatically.
2. The system calculates buyer counter-offers.
3. The live feed shows each negotiation round.

## Visual Analytics

The Visual Analytics tab shows:

- Total negotiations
- Successful deals
- Failed deals
- Success rate
- Fixed broker fees
- Broker commissions
- Total broker revenue
- Latest negotiation gap
- Buyer offer vs dealer ask chart
- Broker treasury chart

This helps show how the negotiation process performs after agents complete deals.

## JADE Sniffer Demo

To show JADE message passing:

1. Make sure `jade_gui=true` in `config.properties`.
2. Run the program.
3. Click `Start Auction Floor`.
4. In the JADE GUI, right-click the main container.
5. Select:

```text
Start Stat. Service > Sniffer
```

6. Add the broker, dealer, and buyer agents into the Sniffer.
7. Run the demo lineup.
8. Observe the ACL messages between agents.

## Common Problems

### `package jade.core does not exist`

The JADE library is not added correctly.

Fix:

```text
File > Project Structure > Libraries > Add lib/jade.jar
```

### Main class not found

Make sure `src` is marked as Sources.

Fix:

```text
File > Project Structure > Modules > src > Mark as Sources
```

### `config.properties` not loaded

Make sure the working directory is set to the project root folder.

### Port 1099 already in use

A previous JADE platform may still be running.

Fix:

1. Stop the previous run.
2. Close the JADE GUI.
3. Run the program again.

## Git Notes

Files that should not be committed:

```text
.DS_Store
out/
*.class
.idea/workspace.xml
.idea/tasks.xml
```

Important files that should be committed:

```text
src/
lib/jade.jar
config.properties
README.md
.gitignore
APDescription.txt
MTPs-Main-Container.txt
```

## Current Status

- Broker Agent implemented
- Dealer Agent implemented
- Buyer Agent implemented
- Java Swing dashboard implemented
- Demo lineup implemented
- Automated negotiation implemented
- Manual negotiation mode implemented
- Agent deletion implemented
- Market boards implemented
- Live auction feed implemented
- Visual analytics implemented

## Academic Use

This project is developed for COS30018 Intelligent Systems at Swinburne University of Technology Sarawak.

It is intended for academic demonstration and submission purposes.