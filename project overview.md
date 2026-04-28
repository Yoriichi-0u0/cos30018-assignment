# COS30018-Automated-Negotiation-System
Design and implementation of a simple multi-agent automated negotiation platform involving agents representing car dealers, car buying agents and a broker agent.

## Project Overview
This project implements a platform for automated negotiation to trade automotive vehicles. The platform enables any number of car dealers and buyers to come in and sell or buy cars. To facilitate this, software agents are implemented to represent the dealers and the buyers. The objective of a dealer agent is to maximise his profit, while a buyer agent wants to buy her desired car at the lowest price. 

## Agent Roles
The system involves the following agent roles interacting within the platform: 

* **Broker Agent (KA) (1 agent)**: 
    * Receives listings from dealers. 
    * Sends relevant deals to buyers. 
    * Facilitates negotiations between a pair of buyer and dealer. 
    * Receives the commissions for each successful deal and a fixed fee for each negotiation. 
* **Dealer Agent (DA) (At least 3 agents)**: 
    * Lists available cars at retail prices with the broker agent. 
    * Sends listings to KA. 
    * Receives a list of potential buyers and their offers. 
    * Selects the buyers he wants to engage and returns this list to KA. 
    * Initiates the negotiations with the selected buyers. 
* **Buyer Agent (BA) (At least 5 agents)**: 
    * Contacts the broker agent to view listings before engaging with dealers. 
    * Contacts KA with the specifications of the cars she wants to buy. 
    * Receives a list of dealers whose cars match her specifications. 
    * Sends back to KA the cars she is willing to negotiate with her first offers. 
    * Receives the requests from dealers to negotiate. 

## Features & System Requirements
* **Basic Version 1 (Trading Platform)**: Provides the core trading platform via the Broker Agent KA.  It includes a GUI that allows DAs and BAs to input their information, such as dealer listings or buyer requirements. This version supports manual negotiation between a dealer and a buyer. 
* **Basic Version 2 (Automated Negotiation)**: Provides an automated negotiation capability for buyers. The buyer provides initial information (first offer and reserve price), and the agent automatically engages in a single negotiation with one dealer to get the best deal. 
* **Extensions (Research Component)**:
    * Extends the automated negotiation functionality to include multiple concurrent negotiations. 
    * Extends the automated negotiation functionality to allow for multi-attribute negotiation to achieve win-win outcomes. 

## Technical Details & Constraints
* **Interaction Protocols**: The protocols used can be FIPA predefined (e.g. CNP, iterated CNP), nested, or newly specified. 
* **Content Language**: The agents can use any standard content language. 
* **Interface**: A GUI is available for user input, parameter settings, and visualization, alongside a configuration file for defaults. 
* **Version Control**: Source code is maintained on a Git based VCS (Github/Bitbucket/GitLab/...). 
