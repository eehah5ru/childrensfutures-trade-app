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

  //
  // bid transfered to new owner
  //
  event BidSold(bytes32 goalId,
                address goalOwner,
                address bidId,
                address oldBidOwner,
                address newBidOwner);

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

  function isWorking()
    public
    constant
    returns(bool)
  {
    return true;
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

    Goal.Goal g = goals[goalId];

    // this goal already exists
    if(g.exists()) {
      throw;
    }


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
    notBeforeStage(Stages.Stage.Created, goals[_goalId].stage)
    notAtStage(Stages.Stage.Cancelled, goals[_goalId].stage)
    onlyExisted(_goalId)
    onlyDreamer(goals[_goalId])
    public
    returns(bool)
  {
    // if (goals[_goalId].owner != msg.sender) {
    //   throw;
    // }
    Goal.Goal g = goals[_goalId];

    g.stage = Stages.Stage.Cancelled;
    GoalCancelled(_goalId, g.owner, g.description);
    return true;
  }

  //
  // place bid
  //
  function placeBid(bytes32 _goalId, string _bidDescription)
    notBeforeStage(Stages.Stage.Created, goals[_goalId].stage)
    notAtStage(Stages.Stage.Cancelled, goals[_goalId].stage)
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
    Goal.Goal g = goals[_goalId];
    Goal.Bid b = g.bids[msg.sender];

    b.owner = msg.sender;
    b.description = _bidDescription;
    b.selected = false;

    //
    // change goal's stage to BidPlaced
    //
    g.stage = Stages.Stage.BidPlaced;

    //
    // fire event
    //
    BidPlaced(_goalId, g.owner, msg.sender, msg.sender, b.description);

    return true;
  }

  //
  // select bid
  //
  function selectBid(bytes32 _goalId, address _bidId)
    onlyStage(Stages.Stage.BidPlaced, goals[_goalId].stage)
    onlyExisted(_goalId)
    onlyExistingBid(_goalId, _bidId)
    onlyDreamer(goals[_goalId])
    public
    returns(bool)
  {
    Goal.Goal g = goals[_goalId];
    Goal.Bid b = g.bids[_bidId];
    //
    // change goal's and bid's state
    //
    g.selectedBidId = _bidId;
    b.selected = true;

    //
    // change goal's stage
    //
    g.stage = Stages.Stage.BidSelected;

    //
    // fire BidSelected event
    //
    BidSelected(_goalId, g.owner, _bidId, b.owner);

    return true;
  }

  //
  // send investment
  //
  function sendInvestment(bytes32 _goalId, address _bidId)
    onlyExisted(_goalId)
    onlyStage(Stages.Stage.BidSelected, goals[_goalId].stage)
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
    Goal.Goal g = goals[_goalId];
    g.stage = Stages.Stage.InvestmentSent;

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
    onlyStage(Stages.Stage.InvestmentSent, goals[_goalId].stage)
    onlySelectedBid(_goalId, _bidId)
    public
    returns(bool)
  {

    //
    // change goal's state
    //
    Goal.Goal g = goals[_goalId];
    g.stage = Stages.Stage.InvestmentReceived;

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
    onlyStage(Stages.Stage.InvestmentReceived, goals[_goalId].stage)
    public
    returns(bool)
  {
    //
    // change goal's state
    //
    Goal.Goal g = goals[_goalId];
    g.stage = Stages.Stage.GoalAchieved;

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
    onlyStage(Stages.Stage.GoalAchieved, goals[_goalId].stage)
    onlySelectedBid(_goalId, _bidId)
    onlyOwnerOfSelectedBid(_goalId, _bidId)
    public
    returns(bool)
  {
    //
    // change goal's state
    //
    Goal.Goal g = goals[_goalId];
    g.stage = Stages.Stage.BonusAsked;

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
    onlyStage(Stages.Stage.BonusAsked, goals[_goalId].stage)
    onlySelectedBid(_goalId, _bidId)

    public
    returns(bool)
  {
    //
    // change goal's stage
    //
    Goal.Goal g = goals[_goalId];
    g.stage = Stages.Stage.BonusSent;
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
    onlyStage(Stages.Stage.BonusSent, goals[_goalId].stage)
    onlySelectedBid(_goalId, _bidId)
    onlyOwnerOfSelectedBid(_goalId, _bidId)
    public
    returns(bool)
  {
    //
    // change goal's state
    //
    Goal.Goal g = goals[_goalId];
    g.stage = Stages.Stage.GoalCompleted;

    //
    // fire GoalCompleted event
    //
    GoalCompleted(_goalId);

    return true;
  }

  //
  // sell bid
  //
  function sellBid(bytes32 _goalId, address _bidId, address _newBidOwner)
    onlyExisted(_goalId)
    notBeforeStage(Stages.Stage.BidSelected, goals[_goalId].stage)
    // notAtStage(Stages.Stage.GoalCompleted, goals[_goalId].stage)
    onlySelectedBid(_goalId, _bidId)
    onlyOwnerOfSelectedBid(_goalId, _bidId)
    public
    returns(bool)
  {
    Goal.Goal g = goals[_goalId];
    Goal.Bid b = g.bids[_bidId];

    //
    // checks
    //
    // can not sell bid to goal owner
    if(g.owner == _newBidOwner) {
      throw;
    }

    if(b.owner == _newBidOwner) {
      throw;
    }

    //
    // change state
    //
    b.owner = _newBidOwner;

    //
    // fire BidSold event
    //

    BidSold(_goalId, g.owner, _bidId, msg.sender, _newBidOwner);

    return true;
  }
}
