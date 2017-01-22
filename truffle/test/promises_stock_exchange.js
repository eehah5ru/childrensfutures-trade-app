contract('GoalsStockExchange', function(accounts) {
  //
  // setup testing environment
  //
  var chai = require("chai");
  var should = chai.should();
  chai.use(require('chai-as-promised'));
  var expect = chai.expect;


  //
  //
  // stubs
  //
  //


  //
  // helpers
  //

  //
  // deployed exchange contract
  //
  var gse = function() {
    return GoalsStockExchange.deployed();
  };

  //
  // adding goal
  // returns GoalAdded event args
  //
  var performAddGoal = function(descr, giveInReturn, options) {
    var watcher = gse().GoalAdded({}, {fromBlock: 'latest'});

    var deferredEvent = new Promise(function(resolve, reject) {
      watcher.watch(function(error, event) {
        if(!error) {
          watcher.stopWatching();
          resolve(event.args);
        } else {
          watcher.stopWatching();
          resolve(error);
        }
      });
    });

    return gse().newGoal(descr, giveInReturn, options)
      .then(function() {
        return deferredEvent;
      });
  };

  //
  // returns raw solidity results of getGoal call
  //
  var performGetGoal = function(goalId, options) {
    return gse().getGoal.call(goalId, options);
  };


  //
  // performs cancelGoal transaction
  // returns GoalCancelled args
  //
  var performCancelGoal = function(goalId, options) {
    var watcher = gse().GoalCancelled({}, {fromBlock: 'latest'});

    var deferredEvent = new Promise(function(resolve, reject) {
      watcher.watch(function(error, event) {
        if(!error) {
          watcher.stopWatching();
          resolve(event.args);
        } else {
          watcher.stopWatching();
          resolve(error);
        }
      });
    });

    return gse().cancelGoal(goalId, options)
      .catch(function(e) {
        watcher.stopWatching();
        throw ("cancelGoal: " + e.toStrring());
      })
      .then(function() {
        return deferredEvent;
      });
  };

  //
  // performs placeBid transaction
  // returns BidPlaced args
  //
  var performPlaceBid = function(goalId, bidDescription, options) {
    var watcher = gse().BidPlaced({}, {fromBlock: 'latest'});

    var deferredEvent = new Promise(function(resolve, reject) {
      watcher.watch(function(error, event) {
        if(!error) {
          watcher.stopWatching();
          resolve(event.args);
        } else {
          watcher.stopWatching();
          resolve(error);
        }
      });
    });

    return gse().placeBid(goalId, bidDescription, options)
      .catch(function(e) {
        watcher.stopWatching();
        throw ("placeBid: " + e.toString());
      })
      .then(function() {
        return deferredEvent;
      });
  };

  //
  // performs selectBid transaction
  // returns BidSelected args
  //
  var performSelectBid = function(goalId, bidId, options) {
    var watcher = gse().BidSelected({}, {fromBlock: 'latest'});

    var deferredEvent = new Promise(function(resolve, reject) {
      watcher.watch(function(error, event) {
        if(!error) {
          watcher.stopWatching();
          resolve(event.args);
        } else {
          watcher.stopWatching();
          resolve(error);
        }
      });
    });

    return gse().selectBid(goalId, bidId, options)
      .catch(function(e) {
        watcher.stopWatching();
        throw ("selectBid: " + e.toString());
      })
      .then(function() {
        return deferredEvent;
      });
  };

  //
  //
  // complex actions
  //
  //

  //
  // add goal
  //
  // @returns GoalAdded event data
  //
  // options:
  // - goalOwner
  // - addGoal.description || current timestamp
  // - addGoal.giveInReturn || current timestamp
  var doAddGoal = function(options) {
    // defaults
    options.addGoal = options.addGoal || {};
    options.addGoal.description = options.addGoal.description || Date.now().toString();
    options.addGoal.giveInReturn = options.addGoal.giveInReturn || Date.now().toString();

    return performAddGoal(options.addGoal.description,
                          options.addGoal.giveInReturn,
                          { from: options.goalOwner });
  };


  //
  // add goal
  // place bid
  //
  // @returns BidPlaced event data
  //
  // options:
  // - all doAddGoal options
  //
  // - goalAdded - function returns GoalAdded event data
  // - bidOwner
  // - bidDescription || current timestamp
  //
  var doPlaceBid = function(options) {
    // defaults
    options.goalAdded = options.goalAdded || function(gaData){ return gaData; };
    options.bidDescription = options.bidDescription || Date.now().toString();

    return doAddGoal(options)
      .then(options.goalAdded)
      .then(function(gaData) {
        return performPlaceBid(gaData.goalId,
                               options.bidDescription,
                               { from: options.bidOwner });
      });
  };

  //
  // add goal
  // place bid
  // select bid
  //
  // @returns BidSelected event data
  //
  // options:
  // - all doAddGoal options
  // - all doPlaceBid options
  //
  // - bidPlaced - function return BidBlaced event data
  // - bidSelector || goalOwner
  // - selectBid.bidId || id of placed bid
  // - selectBid.goalId || id of added goal
  var doSelectBid = function(options) {
    // defaults
    options.bidPlaced = options.bidPlaced || function(pbData) { return pbData; };
    options.bidSelector = options.bidSelector || options.goalOwner;
    options.selectBid = options.selectBid || {};
    // options.selectBid.bidId - see below
    // options.selectBid.goalId - see below

    return doPlaceBid(options)
      .then(options.bidPlaced)
      .then(function(pbData) {
        return performSelectBid(options.selectBid.goalId || pbData.goalId,
                                options.selectBid.bidId || pbData.bidId,
                                {from: options.bidSelector });
      });
  };

  //
  // add goal
  // place bid
  // select bid
  // achieve goal
  //
  // @returns GoalReached event data
  //
  // options
  // - all doAddGoal options
  // - all doPlaceBid options
  // - all doSelectBid options
  //
  // - achieveGoal.goalId || id of added goal
  // - achieveGoal.sender || goal's owner
  var doAchieveGoal = function(options) {
    // TODO: implement
  };




  //
  //
  // specs
  //
  //

  //
  // initial state
  //
  describe("initial state", function() {
    it("should have owner", function() {
      return expect(gse().owner.call()).to.eventually.equal(accounts[0]);
    });

    it("hasGoals should return false", function() {
      return expect(gse().hasGoals.call()).to.eventually.be.equal(false);
    });

    it("getNumGoals should return 0", function() {
      return expect(gse().getNumGoals.call()
                    .then(function(v) { return v.toNumber(); }))
        .to.eventually.be.equal(0);
    });

    describe("getGoal", function() {
      it("getGoal(0) throws exception", function() {
        return expect(gse().getGoal.call(0))
          .to.be.rejected;
      });

      it("getGoal(8) throws expeption also", function() {
        return expect(gse().getGoal.call(8))
          .to.be.rejected;
      });
    });
  });


  //
  //
  // adding goal
  //
  //
  describe("#newGoal", function() {
    describe("fire GoalAdded event", function() {
      it("that has goalId", function() {
        return expect(
          performAddGoal(Date.now().toString(), // descr
                         Date.now().toString(), // giveInReturn
                         {from: accounts[1]})
        ).to.be.fulfilled
          .and.eventually.have.deep.property("goalId");
      });

      it("that has description", function() {
        var description = Date.now().toString();
        return expect(
          performAddGoal(description,
                         Date.now().toString(),
                         {from: accounts[1]})
        ).to.eventually
          .have.deep.property("description", description);
      });

      it("that has owner", function() {
        return expect(
          performAddGoal(Date.now().toString(),
                         Date.now().toString(),
                         {from: accounts[1]})
        ).to.eventually
          .have.deep.property("owner", accounts[1]);
      });

      it("goalId should be a string", function() {
        return expect(
          performAddGoal(Date.now().toString(),
                         Date.now().toString(),
                         {from: accounts[1]})
        ).to.be.fulfilled
          .and.eventually.have.deep.property("goalId").that.is.a("string");
      });

      it("has a giveInReturn property", function() {
        var giveInReturn = Date.now().toString();
        return expect(
          performAddGoal(Date.now().toString(),
                         giveInReturn,
                         {from: accounts[1]})
        ).to.eventually
          .have.deep.property("giveInReturn", giveInReturn);
      });
    });

    it("should be fulfilled", function() {
      return expect(
        performAddGoal(Date.now().toString(),
                      Date.now().toString())
      ).to.be.fulfilled;
    });

    it("should throw exception if description is empty", function() {
      return expect(
        performAddGoal("", Date.now().toString())
      ).to.be.rejected;
    });

    it("should throw an expection if giveInReturn is empty", function() {
      return expect(
        performAddGoal(Date.now().toString(), "")
      ).to.be.rejected;
    });

    it("should increase numGoals", function() {
      var prevNumGoals = null;
      var curNumGoals = null;

      return performAddGoal(Date.now().toString(),
                           Date.now().toString())
          .then(function() {
            return gse().getNumGoals.call();
          })
        .then(function(n) {
          prevNumGoals = n.toNumber();

          return performAddGoal(Date.now().toString(),
                               Date.now().toString());
        })
        .then(function() {
          return gse().getNumGoals.call();
        })
        .then(function(n) {
          curNumGoals = n.toNumber();

          expect(prevNumGoals + 1).to.be.equal(curNumGoals);
        });
    });

    it("two calls of #newGoal should generate different goalId", function() {
      var firstGoalId = null;

      return performAddGoal("aaa", "aaaaa")
        .then(function(gaData) {
          firstGoalId = gaData.goalId;

          return performAddGoal("aaa", "aaaa"); // same description and owner
        })
        .then(function(gaData) {
          expect(firstGoalId).to.be.not.equal(gaData.goalId);
        });
    });
  });

  describe("#getGoal", function() {
    it("should be successful", function() {
      var expectedGoalId = null;

      return expect(performAddGoal("aaa",
                                   Date.now().toString(),
                                   {from: accounts[2]})
                    .then(function(goalAddedEventData) {
                      expectedGoalId = goalAddedEventData.goalId;

                      return gse().getGoal.call(goalAddedEventData.goalId, {from: accounts[3]});
                    })).to.be.fulfilled;
    });

    it("returns goalId", function() {
      var expectedGoalId = null;

      return performAddGoal("aaa",
                            Date.now().toString(),
                            {from: accounts[2]})
        .then(function(goalAddedEventData) {
          expectedGoalId = goalAddedEventData.goalId;

          return gse().getGoal.call(goalAddedEventData.goalId, {from: accounts[3]});
        })
        .then(function(goal) {
          expect(goal[0]).to.be.equal(expectedGoalId);
        });
    });


    it("returns goalOwner", function() {
      return performAddGoal("bbb",
                            Date.now().toString(),
                            {from: accounts[3]})
        .then(function(gaData) {
          return performGetGoal(gaData.goalId, {from: accounts[4]});
        })
        .then(function(goal) {
          expect(goal[1]).to.be.equal(accounts[3]);
        });
    });

    it("returns goal's description", function() {
      return expect(
        performAddGoal("cccc",
                       Date.now().toString(),
                       {from: accounts[4]})
          .then(function(gaData) {
            return performGetGoal(gaData.goalId, {from: accounts[1]});
          })
      ).to.be.fulfilled
        .and.eventually.have.deep.property("[2]", "cccc");
    });

    it("fails if goalId is too big", function() {
      return expect(
        performAddGoal("ddd", Date.now().toString())
          .then(function(gaData) {
            return performGetGoal(gaData.goalId.toNumber() + 2);
          })
      ).to.be.rejected;
    });

    it("fails if goalId is equal 0", function() {
      return expect(
        performAddGoal("ddd", Date.now().toString())
          .then(function(gaData) {
            return performGetGoal(0);
          })
      ).to.be.rejected;
    });

    it("fails if goalId is less than 0", function() {
      return expect(
        performAddGoal("aaa", Date.now().toString())
          .then(function(gaData) {
            return performGetGoal(-1);
          })
      ).to.be.rejected;
    });
  });

  //
  //
  // cancelGoal
  //
  //
  describe("#cancelGoal", function() {
    describe("GoalCancelled event", function() {
      it("should have goalId property", function() {
        var gId = null;
        return performAddGoal("123",
                              Date.now().toString(),
                              {from: accounts[1]})
          .then(function(gaData) {
            gId = gaData.goalId;

            return performCancelGoal(gaData.goalId, {from: accounts[1]});
          })
          .then(function(gcData) {
            return expect(gcData.goalId).to.be.equal(gId);
          });
      });

      it("should have owner property", function() {
        return performAddGoal("123",
                              Date.now().toString(),
                              {from: accounts[1]})
          .then(function(gaData) {
            return performCancelGoal(gaData.goalId, {from: accounts[1]});
          })
          .then(function(gcData) {
            expect(gcData.owner).to.be.equal(accounts[1]);
          });

      });

      it("should have description property", function() {
        return performAddGoal("123",
                              Date.now().toString(),
                              {from: accounts[1]})
          .then(function(gaData) {
            return performCancelGoal(gaData.goalId, {from: accounts[1]});
          })
          .then(function(gcData) {
            expect(gcData.description).to.be.equal("123");
          });
      });

      it("impossible to cancel goal if bid has been selected");
    });


    it("should be fulfilled", function() {
      return expect(
        performAddGoal("ddd",
                       Date.now().toString(),
                       {from: accounts[1]})
          .then(function(gaData) {
            return performCancelGoal(gaData.goalId, {from: accounts[1]});
          })
      ).to.be.fulfilled;
    });

    it("only dreamer can do that", function() {
      return expect(
        performAddGoal("ddd",
                       Date.now().toString(),
                       {from: accounts[1]})
          .then(function(gaData) {
            return performCancelGoal(gaData.goalId, {from: accounts[2]});
          })
      ).to.be.rejected;
    });

    it("only existing goal can be cancelled", function() {
      return expect(
        performCancelGoal(-32)
      ).to.be.rejected;
    });

    it("it is impossible to cancel already cancelled goal", function() {
      return expect(
        performAddGoal("fffdd",
                       Date.now().toString(),
                       {from: accounts[1]})
          .then(function(gaData) {
            return performCancelGoal(gaData.goalId, {from: accounts[1]});
          })
          .then(function(gcData) {
            return performCancelGoal(gcData.goalId, {from: accounts[1]});
          })
      ).to.be.rejected;
    });
  });

  //
  //
  // placeBid
  //
  //
  describe("placeBid", function() {
    describe("BidPlaced event", function() {
      it("has goalId property", function() {
        var gId = null;

        return performAddGoal("234",
                              Date.now().toString(),
                              {from: accounts[1]})
          .then(function(gaData) {
            gId = gaData.goalId;

            return performPlaceBid(gaData.goalId, "aaaa", {from: accounts[2]});
          })
          .then(function(pbData) {
            return expect(pbData.goalId).to.be.equal(gId);
          });
      });

      it("has goalOwner property", function() {
        return expect(
          performAddGoal("2345",
                         Date.now().toString(),
                         {from: accounts[2]})
            .then(function(gaData) {
              return performPlaceBid(gaData.goalId, "aaaaaa", {from: accounts[3]});
            })
        ).to.eventually.have.property("goalOwner", accounts[2]);
      });

      it("has bidOwner property", function() {
        return expect(
          performAddGoal("23456",
                         Date.now().toString(),
                         {from: accounts[2]})
            .then(function(gaData) {
              return performPlaceBid(gaData.goalId, "aaaaaa", {from: accounts[3]});
            })
        ).to.eventually.have.property("bidOwner", accounts[3]);

      });

      it("has bidId prop", function() {
        return expect(
          doPlaceBid({
            goalOwner: accounts[1],
            bidOwner: accounts[2]
          })
        ).to.eventually.have.property("bidId").that.is.a("string");
      });

      it("has description property", function() {
        return expect(
          performAddGoal("23478",
                         Date.now().toString(),
                         {from: accounts[2]})
            .then(function(gaData) {
              return performPlaceBid(gaData.goalId, "aaaaaa", {from: accounts[3]});
            })
        ).to.eventually.have.property("description", "aaaaaa");
      });
    });

    it("should be fulfilled", function() {
      return expect(
        performAddGoal("aaa",
                       Date.now().toString(),
                       {from: accounts[1]})
          .then(function(gaData) {
            return performPlaceBid(gaData.goalId, "aaa", {from: accounts[2]});
          })
      ).to.be.fulfilled;
    });

    it("goal's owner can't place bid on her own goal", function() {
      return expect(
        performAddGoal("bbb",
                       Date.now().toString(),
                       {from: accounts[0]})
          .then(function(gaData) {
            return performPlaceBid(gaData.goalId, "aaa", {from: accounts[0]});
          })
      ).to.be.rejected;
    });

    it("it is possible to place bid only on existing goal", function() {
      return expect(
        performPlaceBid("-1", "aaa", {from: accounts[1]})
      ).to.be.rejected;
    });

    it("allows to place bids from different accounts");

    it("is's impossible to place bit on the same goal twice", function() {
      var goalId = null;

      return expect(
        performAddGoal("aaaa",
                       Date.now().toString(),
                       {from: accounts[3]})
          .then(function(gaData) {
            goalId = gaData.goalId;

            return performPlaceBid(goalId, "aaaa", {from: accounts[4]});
          })
          .then(function(pbData) {
            return performPlaceBid(goalId, "bbb", {from: accounts[4]});
          })
      ).to.be.rejected;
    });

    it("should fail when bidDescription is mepty", function() {
      return expect(
        performAddGoal("aaa",
                       Date.now().toString(),
                       {from: accounts[4]})
          .then(function(gaData) {
            return performPlaceBid(gaData.goalId, "", {from: accounts[5]});
          })
      ).to.be.rejected;
    });

    it("it's not possible to place bid on already cancelled goal", function() {
      return expect(
        performAddGoal("aaa",
                       Date.now().toString(),
                       {from: accounts[5]})
          .then(function(gaData) {
            return performCancelGoal(gaData.goalId, {from: accounts[5]});
          })
          .then(function(gcData) {
            return performPlaceBid(gcData.goalId, "nnn", {from: accounts[4]});
          })
      ).to.be.rejected;
    });

    it("impossible to place bid on already BidSelected goal");
  });

  //
  //
  // slectBid
  //
  //
  describe("#selectBid", function() {
    describe("BidSelected event", function() {
      it("has goalId prop", function() {
        var gId = null;

        return doSelectBid({
          goalOwner: accounts[1],
          addGoal: {description: "aaa",
                    giveInReturn: "aaaaa"},
          bidDescription: "bbb",
          bidOwner: accounts[2],
          goalAdded: function(gaData) {
            gId = gaData.goalId;
            return gaData;
          }})
          .then(function(bsData) {
            return expect(bsData.goalId).to.be.equal(gId);
          });
      });

      it("has goalOwner prop", function() {
        return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2]
          })
        ).to.eventually.have.property("goalOwner", accounts[1]);
      });

      it("has bidId prop", function() {
        return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2]
          })
        ).to.eventually.have.property("bidId").that.is.a("string");
      });

      it("has bidOwner prop", function() {
        return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2]
          })
        ).to.eventually.have.property("bidOwner", accounts[2]);
      });
    });

    it("should be fulfilled", function() {
        return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2]
          })
        ).to.be.fulfilled;
    });

    // FIXME: strange test
    it("bid's owner can't select her own bid", function() {
        return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2],
            bidSelector: accounts[2]
          })
        ).to.be.rejected;
    });

    it("only goal's owner can select bid", function() {
        return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2],
            bidSelector: accounts[3]
          })
        ).to.be.rejected;

    });

    it("it's possible to select only existsing bid", function() {
        return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2],
            selectBid: {
              bidId: "-1"
            }
          })
        ).to.be.rejected;
    });

    it("it's possible to select bid only in existing goal", function() {
        return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2],
            selectBid: {
              goalId: "-1"
            }
          })
        ).to.be.rejected;
    });

    it("it's impossible to select bid twice", function() {
      var bpData = null;

      return expect(
          doSelectBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2],
            bidPlaced: function(v) {
              bpData = v;

              return v;
            }
          })
          .then(function(bsData) {
            return performSelectBid(bsData.goalId, bpData.bidId, {from: accounts[1]});
          })
        ).to.be.rejected;

    });

    it("it's impossible to reselect bid", function() {
      var bid1 = null;

      return expect(
          doPlaceBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2]
          })
          .then(function(bpData) {
            bid1 = bpData;

            return performPlaceBid(bpData.goalId, "ccc", {from: accounts[3]});
          })
          .then(function(bpData) {
            return performSelectBid(bpData.goalId, bpData.bidId, {from: accounts[1]});
          })
          .then(function(bsData) {
            return performSelectBid(bsData.goalId, bid1.bidId, {from: accounts[1]});
          })
        ).to.be.rejected;
    });

    it("impossible to select bid in cancelled goal", function() {
      var bid1 = null;

      return expect(
          doPlaceBid({
            goalOwner: accounts[1],
            bidDescription: "bbb",
            bidOwner: accounts[2]
          })
          .then(function(bpData) {
            bid1 = bpData;

            return performCancelGoal(bpData.goalId,
                                     {from: accounts[1]});
          })
          .then(function(gcData) {
            return performSelectBid(bid1.goalId,
                                    bid1.bidId,
                                    {from: accounts[1]});
          })
      ).to.be.rejected;
    });
  });
});
