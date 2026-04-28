# COS30018 - Intelligent Systems: Automated Negotiation System

## Team Members
* **Jonathan Zheng Li CHAI** (Team Lead / Core Architecture)
* **Yuen Kai CHIA** (Agent Communication / Protocols)
* **Shan Kai LIU** (Negotiation Strategies / GUI)
* **Stanley Kai Jie LEONG**
* **Edmund Dao Ann LIM**

## Project Overview
This project is a multi-agent trading platform developed in JADE (Java Agent Development Framework) to facilitate the automated negotiation and trading of automotive vehicles. 

The system implements automated, multi-round negotiations where agents negotiate to achieve their specific goals: maximizing profit for sellers and minimizing cost for buyers.

### System Architecture
The platform operates using three distinct agent roles:
* **Broker Agent (KA) (1 Agent):** The central facilitator that receives dealer listings, routes relevant deals to buyers, and takes a commission for successful transactions.
* **Dealer Agents (DA) (3+ Agents):** Represent car sellers. They provide listings, evaluate buyer offers, and negotiate to maximize profit.
* **Buyer Agents (BA) (5+ Agents):** Represent car buyers. They specify car requirements, receive matched listings, and deploy automated negotiation strategies to secure the lowest price.

## Features Implemented
* **Interactive GUI:** A central interface for users to instantiate agents, input car specifications, set reserve prices, and visualize the negotiation process.
* **Automated Negotiation:** Buyer and Dealer agents negotiate autonomously based on defined parameters (e.g., first offer, reserve price) using Iterated Contract Net Protocols.
* **Extension 1 (Concurrent Negotiations):** Handled via `SSResponderDispatcher`, Buyer Agents seamlessly negotiate with up to 3 dealers concurrently down to the exact millisecond without thread blocking.
* **Prediction Algorithm:** Buyer agents utilize an Opponent-Model Prediction Algorithm to analyze a dealer's concession rate and mathematically predict their absolute bottom-line price to formulate optimal counter-offers.

## Presentation & Demo
🎥 **[Insert YouTube/Google Drive Link Here]**
Please view our 10-minute video presentation demonstrating a working prototype and a well-designed realistic trading scenario.

## JADE GUI & Sniffer Instructions
For the assignment demo, you can enable the JADE Management Console to sniff agent communication:
1. Ensure `jade_gui=true` is set in `config.properties`.
2. Run the `AuctionDashboardApp`.
3. In the JADE Remote Agent Management GUI, right-click **"Main-Container"** and select **"Start Stat. Service" -> "Sniffer"**.
4. In the Sniffer window, right-click the canvas and select **"Add Agents"**.
5. Move the **Broker (KA)**, **Dealers (DA)**, and **Buyers (BA)** to the right-side list and click OK.
6. Click **"Run Demo Lineup"** in our Dashboard and watch the ACL message flow (CFP, PROPOSE, ACCEPT) in the Sniffer.

## Repository Structure
* `src/`: Contains all Java source code for the JADE agents, GUI, and controllers.
* `docs/`: Contains our final 8-10 page project report detailing the sequence diagrams, strategies, and performance analysis.
* `config.properties`: External configuration file to pre-load baseline default parameters into the application.

## How to Run the System
1. Open the project in IntelliJ IDEA (Ensure JDK 8 is selected).
2. Run the `app.AuctionDashboardApp` class to launch the master GUI.
3. Click **"Start Auction Floor"** to initialize the underlying JADE Main Container.
4. Click **"Run Demo Lineup"** to automatically satisfy the assignment baseline (Spawns 1 Broker, 3 Dealers, and 5 Buyers) and watch the prediction algorithms and contract net protocols execute in real-time on the Live Auction Feed!
