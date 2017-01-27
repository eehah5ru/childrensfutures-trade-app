pragma solidity ^0.4.2;

import "./Goal.sol";

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
