var gas_limit = 0xd7e7c4;

var options = {
  // gas: gas_limit
};

// var options = {};

module.exports = function(deployer) {
  deployer.deploy(Stages, options);
  deployer.autolink(Goal, options);
  deployer.deploy(Goal, options);
  deployer.autolink(withGoals, options);
  deployer.deploy(withGoals, options);
  deployer.deploy(owned, options);
  deployer.autolink(staged, options);
  deployer.deploy(staged, options);
  deployer.deploy(withDreamer, options);
  // deployer.autolink(GoalsStockExchange, options);
  // deployer.deploy(GoalsStockExchange, options);
  // deployer.autolink();
  // deployer.deploy(Goal);
  // deployer.autolink();
  // deployer.deploy(Stages);
  // deployer.autolink();
  // deployer.deploy(owned);
  // deployer.autolink();
  // deployer.deploy(staged);
  // deployer.autolink();
  // deployer.deploy(GoalsStockExchange);
};
