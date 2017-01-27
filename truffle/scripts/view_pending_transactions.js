module.exports = function(callback) {
  // console.log(web3.pendingTransactions);
  var filter = web3.eth.filter("pending");

  // watch for changes
  filter.watch(function(error, result){
    if (!error)
      console.log(result);
  });

  callback();
};
