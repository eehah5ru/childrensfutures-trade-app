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
    string giveInReturn;
    Stages.Stage stage;
    address selectedBidId;

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
    bytes32 id;
    address owner;
    string description;
    bool selected;
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
  //
  //
  //

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
  // check giveInReturn validity
  //
  function isValidGiveInreturn(string _giveInReturn)
    returns(bool)
  {
    if(bytes(_giveInReturn).length != 0) {
      return true;
    }
    return false;
  }

  //
  //
  // utils
  //
  //

  //
  // make goal id
  //
  function mkGoalId(uint salt, address owner, string description, string giveInReturn)
    returns(bytes32)
  {
    return sha3(salt, now, owner, description, giveInReturn);
  }

  //
  // make bid id
  //
  // function mkBidId(address bidOwner, string description)
  //   returns(address)
  // {
  //   return ;
  // }



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

  modifier onlyExistingBid(bytes32 _goalId, address _bidId) {
    if(goals[_goalId].bids[_bidId].owner == address(0x0)) {
      throw;
    }
    _;
  }

  modifier onlySelectedBid(bytes32 _goalId, address _bidId) {
    if (goals[_goalId].bids[_bidId].selected != true) {
      throw;
    }
    _;
  }

  modifier onlyOwnerOfSelectedBid(bytes32 _goalId, address _bidId) {
    if (goals[_goalId].bids[_bidId].selected != true) {
      throw;
    }

    if (goals[_goalId].bids[_bidId].owner != msg.sender) {
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
