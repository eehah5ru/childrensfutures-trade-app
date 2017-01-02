pragma solidity ^0.4.2;

import "./Stages.sol";

library Goal {
  //
  //
  // GOAL STRUCTURE
  //
  //
  struct Goal {
    address owner;
    string description;
    Stages.Stage stage;

    //
    // BIDS on goal
    // bid-owner-address => Bid struct
    //
    mapping (address => Bid) bids;
  }

  //
  //
  // BID STRUCTURE
  //
  //
  struct Bid {
    address owner;
    string description;
    // TODO: accepted?
  }

  //
  //
  // predicates
  //
  //

  //
  // check goal is not empty
  //
  function exists(Goal _goal)
    internal
    returns(bool)
  {
    if (_goal.owner == address(0x0)) {
      return false;
    }
    // TODO: add more checks
    return true;
  }

  //
  // check description for validity
  //
  function isValidDescription(string description)
    returns(bool)
  {
    if (bytes(description).length != 0) {
      return true;
    }
    return false;
  }

  //
  //
  // utils
  //
  //
  function mkGoalId(address owner, string description)
    returns(bytes32)
  {
    return sha3(now, msg.sender, description);
  }



}

contract withGoals {
  using Goal for Goal.Goal;

  /* FIXME: make it private? */
  mapping (bytes32 => Goal.Goal) goals;

  uint numGoals;


  //
  //
  // modifiers
  //
  //
  // modifier onlyDreamer(Goal.Goal _goal) {
  //   if(_goal.owner != msg.sender) {
  //     throw;
  //   }
  //   _;
  // }

  modifier onlyExisted(bytes32 _goalId) {
    if(!(goals[_goalId].exists())) {
      throw;
    }
    _;
  }

  modifier notBidded(bytes32 _goalId) {
    // if exists in mapping then bid already placed
    if(goals[_goalId].bids[msg.sender].owner == msg.sender) {
      throw;
    }
    _;
  }


  //
  //
  // public functions
  //
  //
  function hasGoals() public constant returns (bool) {
    return (numGoals != 0);
  }

  function getNumGoals()
    public
    constant
    returns (uint)
  {
    return numGoals;

  }


}
