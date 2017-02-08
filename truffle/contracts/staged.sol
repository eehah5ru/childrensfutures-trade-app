pragma solidity ^0.4.2;

import "./Stages.sol";
import "./Goal.sol";

//
// staged
// restrictions related to stage
// generic modifires:
// - notAtStage(stage, goalId)
// - onlyStage(stage, goalId)
// - beforeStage(stage, goalId)
// - notAfterStage(stage, goalId)
// - afterStage(stage, goalId)
// - notBeforeStage(stage, goalId)
contract staged {
  //
  // GENERIC
  //
  modifier notAtStage(Stages.Stage _expectedStage, Stages.Stage _stage) {
    if (_stage == _expectedStage) {
      throw;
    }
    _;
  }

  modifier onlyStage(Stages.Stage _expectedStage, Stages.Stage _stage) {
    if (_stage != _expectedStage) {
      throw;
    }
    _;
  }

  modifier beforeStage(Stages.Stage _expectedStage, Stages.Stage _stage) {
    if (uint(_expectedStage) <= uint(_stage)) {
      throw;
    }
    _;
  }

  modifier notAfterStage(Stages.Stage _expectedStage, Stages.Stage _stage) {
    if (uint(_expectedStage) < uint(_stage)) {
      throw;
    }
    _;
  }

  modifier afterStage(Stages.Stage _expectedStage, Stages.Stage _stage) {
    if (uint(_expectedStage) >= uint(_stage)) {
      throw;
    }
    _;
  }

  modifier notBeforeStage(Stages.Stage _expectedStage, Stages.Stage _stage) {
    if (uint(_expectedStage) > uint(_stage)) {
      throw;
    }
    _;
  }
}
