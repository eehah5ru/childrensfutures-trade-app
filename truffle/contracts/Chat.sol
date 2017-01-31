pragma solidity ^0.4.2;

import "./owned.sol";

contract Chat is owned {
  uint msgId;

  event MessageSent(bytes32 channelId, uint msgId, address sender, string message);


  //
  // setup
  //
  function Chat()
    public
    payable
  {
    msgId = 0;
  }

  //
  // send message
  //
  function sendMessage(bytes32 _channelId, string _message)
    public
    returns(bool)
  {
    msgId++;

    MessageSent(_channelId, msgId, msg.sender, _message);
    return true;
  }
}
