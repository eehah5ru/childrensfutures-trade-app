pragma solidity ^0.4.2;

import "./owned.sol";
import "./staged.sol";
import "./Stages.sol";
import "./Goal.sol";

//
//
// helpers
//
//


//
// with dreamer
// modifiers connected to dreamer logic
//
contract withDreamer {
  //
  // only owner of goal can do smth
  //
  modifier onlyDreamer(Goal.Goal _goal) {
    if (_goal.owner != msg.sender) {
      throw;
    }
    _;
  }

  //
  // any person except dremear can perform smth
  //
  modifier notDreamer(Goal.Goal _goal) {
    if (_goal.owner == msg.sender) {
      throw;
    }
    _;
  }
}


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
                     string description);

  event GoalCancelled(bytes32 goalId,
                      address owner,
                      string description);

  event BidPlaced(bytes32 goalId,
                  address goalOwner,
                  address bidId,
                  address bidOwner,
                  string description);

  event BidSelected(bytes32 goalId,
                    address goalOwner,
                    address bidId,
                    address bidOwner);
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


  function getGoal(bytes32 _goalId)
    notBeforeStage(Stages.Stage.Created, goals[_goalId])
    onlyExisted(_goalId)
    public
    constant
    returns(bytes32 goalId,
            address owner,
            string description)
  {

    Goal.Goal r = goals[_goalId];

    goalId = _goalId;
    owner = r.owner;
    description = r.description;
  }


  //
  // create new goal
  //
  function newGoal(string description)
    public
    returns (bool)
  {

    //
    // checks
    //
    if(!(Goal.isValidDescription(description))) {
      throw;
    }

    bytes32 goalId = Goal.mkGoalId(numGoals, msg.sender, description);

    // this goal already exists
    if(goals[goalId].exists()) {
      throw;
    }

    Goal.Goal g = goals[goalId];
    g.description = description;
    g.owner = msg.sender;
    g.stage = Stages.Stage.Created;

    numGoals = numGoals + 1;

    GoalAdded(goalId, msg.sender, description);

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

}
