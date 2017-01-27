pragma solidity ^0.4.2;

import "./owned.sol";
import "./staged.sol";
import "./Stages.sol";
import "./Goal.sol";
import "./withDreamer.sol";



/*

  contract for exchange
  entry point for all participants
  stores all goals. managing their CRUD actions

 */
contract GoalsStockExchange is owned, staged, withDreamer, withGoals {
  /*
    contract variables
  */



  /*
   *
   *
   * contract events
   *
   *
  */

  event GoalAdded(bytes32 goalId,
                     address owner,
                  string description,
                  string giveInReturn);

  event GoalCancelled(bytes32 goalId,
                      address owner,
                      string description); //FIXME: add rest of goal data?

  event BidPlaced(bytes32 goalId,
                  address goalOwner,
                  address bidId,
                  address bidOwner,
                  string description);

  event BidSelected(bytes32 goalId,
                    address goalOwner,
                    address bidId,
                    address bidOwner);

  event InvestmentSent(bytes32 goalId,
                       address bidId);

  event InvestmentReceived(bytes32 goalId,
                           address bidId);

  event GoalAchieved(bytes32 goalId);

  event BonusAsked(bytes32 goalId,
                   address bidId);

  event BonusSent(bytes32 goalId,
                  address bidId);

  event GoalCompleted(bytes32 goalId);

  /*
   *
   * functions
   *
   *
  */

  //
  // first time setup
  //
  function GoalsStockExchange()
    public
    payable
  {
    // FIXME: move to WithGoals
    numGoals = 0;
  }


  // function getGoal(bytes32 _goalId)
  //   notBeforeStage(Stages.Stage.Created, goals[_goalId])
  //   onlyExisted(_goalId)
  //   public
  //   constant
  //   returns(bytes32 goalId,
  //           address owner,
  //           string description)
  // {

  //   Goal.Goal r = goals[_goalId];

  //   goalId = _goalId;
  //   owner = r.owner;
  //   description = r.description;
  // }


  //
  // create new goal
  //
  function newGoal(string description, string giveInReturn)
    public
    returns (bool)
  {

    //
    // checks
    //
    if(!(Goal.isValidDescription(description))) {
      throw;
    }

    if(!(Goal.isValidGiveInreturn(giveInReturn))) {
      throw;
    }

    bytes32 goalId = Goal.mkGoalId(numGoals, msg.sender, description, giveInReturn);

    // this goal already exists
    if(goals[goalId].exists()) {
      throw;
    }

    Goal.Goal g = goals[goalId];
    g.description = description;
    g.giveInReturn = giveInReturn;
    g.owner = msg.sender;
    g.stage = Stages.Stage.Created;

    numGoals = numGoals + 1;

    GoalAdded(goalId, msg.sender, description, giveInReturn);

    return true;
  }

  //
  // cancel goal
  //
  function cancelGoal(bytes32 _goalId)
    notBeforeStage(Stages.Stage.Created, goals[_goalId])
    notAtStage(Stages.Stage.Cancelled, goals[_goalId])
    onlyExisted(_goalId)
    onlyDreamer(goals[_goalId])
    public
    returns(bool)
  {
    // if (goals[_goalId].owner != msg.sender) {
    //   throw;
    // }

    goals[_goalId].stage = Stages.Stage.Cancelled;
    GoalCancelled(_goalId, goals[_goalId].owner, goals[_goalId].description);
    return true;
  }

  //
  // place bid
  //
  function placeBid(bytes32 _goalId, string _bidDescription)
    notBeforeStage(Stages.Stage.Created, goals[_goalId])
    notAtStage(Stages.Stage.Cancelled, goals[_goalId])
    onlyExisted(_goalId)
    notDreamer(goals[_goalId])
    notBidded(_goalId)
    public
    returns(bool)
  {

    if(!(Goal.isValidDescription(_bidDescription))) {
      throw;
    }

    //
    // add bid to goal
    //
    Goal.Bid b = goals[_goalId].bids[msg.sender];

    b.owner = msg.sender;
    b.description = _bidDescription;
    b.selected = false;

    //
    // change goal's stage to BidPlaced
    //
    goals[_goalId].stage = Stages.Stage.BidPlaced;

    //
    // fire event
    //
    BidPlaced(_goalId, goals[_goalId].owner, msg.sender, msg.sender, b.description);

    return true;
  }

  //
  // select bid
  //
  function selectBid(bytes32 _goalId, address _bidId)
    onlyStage(Stages.Stage.BidPlaced, goals[_goalId])
    onlyExisted(_goalId)
    onlyExistingBid(_goalId, _bidId)
    onlyDreamer(goals[_goalId])
    public
    returns(bool)
  {

    //
    // change goal's and bid's state
    //
    goals[_goalId].selectedBidId = _bidId;
    goals[_goalId].bids[_bidId].selected = true;

    //
    // change goal's stage
    //
    goals[_goalId].stage = Stages.Stage.BidSelected;

    //
    // fire BidSelected event
    //
    BidSelected(_goalId, goals[_goalId].owner, _bidId, goals[_goalId].bids[_bidId].owner);

    return true;
  }

  //
  // send investment
  //
  function sendInvestment(bytes32 _goalId, address _bidId)
    onlyExisted(_goalId)
    onlyStage(Stages.Stage.BidSelected, goals[_goalId])
    onlyExistingBid(_goalId, _bidId)
    onlySelectedBid(_goalId, _bidId)
    onlyOwnerOfSelectedBid(_goalId, _bidId)
    public
    returns(bool)
  {
    //
    // checks
    //

    //
    // change goal's stage
    //
    goals[_goalId].stage = Stages.Stage.InvestmentSent;

    //
    // fire InvestmentSent event
    //
    InvestmentSent(_goalId, _bidId);

    return true;
  }

  //
  // receive investment
  //
  function receiveInvestment(bytes32 _goalId, address _bidId)
    onlyExisted(_goalId)
    onlyDreamer(goals[_goalId])
    onlyStage(Stages.Stage.InvestmentSent, goals[_goalId])
    onlySelectedBid(_goalId, _bidId)
    public
    returns(bool)
  {

    //
    // change goal's state
    //
    goals[_goalId].stage = Stages.Stage.InvestmentReceived;

    //
    // fire InvestmentReceived event
    //
    InvestmentReceived(_goalId, _bidId);

    return true;
  }

  //
  // achieve goal
  //
  function achieveGoal(bytes32 _goalId)
    onlyExisted(_goalId)
    onlyDreamer(goals[_goalId])
    onlyStage(Stages.Stage.InvestmentReceived, goals[_goalId])
    public
    returns(bool)
  {
    //
    // change goal's state
    //
    goals[_goalId].stage = Stages.Stage.GoalAchieved;

    //
    // fire GoalAchieved event
    //
    GoalAchieved(_goalId);

    return true;
  }

  //
  // ask for bonus
  //
  function askBonus(bytes32 _goalId, address _bidId)
    onlyExisted(_goalId)
    onlyStage(Stages.Stage.GoalAchieved, goals[_goalId])
    onlySelectedBid(_goalId, _bidId)
    onlyOwnerOfSelectedBid(_goalId, _bidId)
    public
    returns(bool)
  {
    //
    // change goal's state
    //
    goals[_goalId].stage = Stages.Stage.BonusAsked;

    //
    // fire BonusAsked event
    //
    BonusAsked(_goalId, _bidId);

    return true;
  }

  //
  // send bonus
  //
  function sendBonus(bytes32 _goalId, address _bidId)
    onlyExisted(_goalId)
    onlyDreamer(goals[_goalId])
    onlyStage(Stages.Stage.BonusAsked, goals[_goalId])
    onlySelectedBid(_goalId, _bidId)

    public
    returns(bool)
  {
    //
    // change goal's stage
    //
    goals[_goalId].stage = Stages.Stage.BonusSent;
    //
    // fire BonusSent event
    //
    BonusSent(_goalId, _bidId);

    return true;
  }

  //
  // complete goal
  //
  function completeGoal(bytes32 _goalId, address _bidId)
    onlyExisted(_goalId)
    onlyStage(Stages.Stage.BonusSent, goals[_goalId])
    onlySelectedBid(_goalId, _bidId)
    onlyOwnerOfSelectedBid(_goalId, _bidId)
    public
    returns(bool)
  {
    //
    // change goal's state
    //
    goals[_goalId].stage = Stages.Stage.GoalCompleted;

    //
    // fire GoalCompleted event
    //
    GoalCompleted(_goalId);

    return true;
  }

}
