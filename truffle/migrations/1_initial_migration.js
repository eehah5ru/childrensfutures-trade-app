var options = {
  // gas: 1550000
};

module.exports = function(deployer) {
  deployer.deploy(Migrations, options);
};
