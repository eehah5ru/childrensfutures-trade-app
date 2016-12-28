(ns childrensfutures-trade.db
  (:require [cljs-web3.core :as web3]))

;; (defn mk-web3 []
;;   (or (aget js/window "web3")
;;              (if goog.DEBUG
;;                (web3/create-web3 "http://localhost:8545/")
;;                (web3/create-web3 "https://morden.infura.io/metamask"))))

(defn mk-web3 []
  (or (aget js/window "web3")
      (web3/create-web3 "http://192.168.0.3:8545/")))


(def default-db
  {:goals []
   :settings {}
   :my-addresses []
   :accounts {}
   :new-goal {:description ""
              :address nil
              :sending? false}
   :web3 (mk-web3)
   :provides-web3? (or (aget js/window "web3") goog.DEBUG)
   :contract {:name "GoalsStockExchange"
              :abi nil
              :bin nil
              :instance nil
              :address "0x7617f9ac8d67ab95134678a2d3ca6c640574a992"}})
