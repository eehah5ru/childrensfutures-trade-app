pragma solidity ^0.4.2;

import "./owned.sol";

contract Chat is owned {
  event MessageAdded(bytes32 channelId, address sender, string message);
}
