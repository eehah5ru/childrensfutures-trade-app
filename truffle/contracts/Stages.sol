pragma solidity ^0.4.2;

library Stages {
  /*
    stages of Contract
   */
  enum Stage {
    Unknown,
    Created,
    BidPlaced,
    BidSelected,
    InvestmentSent,
    InvestmentReceived,
    GoalAchieved,
    BonusAsked,
    BonusSent,
    GoalCompleted,
    Cancelled
  }

}
