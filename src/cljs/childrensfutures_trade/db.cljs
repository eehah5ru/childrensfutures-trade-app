(ns childrensfutures-trade.db
  (:require [cljs-web3.core :as web3]))

(defn mk-web3 []
  (or (aget js/window "web3")
             (if goog.DEBUG
               (web3/create-web3 "http://localhost:8545/")
               (web3/create-web3 "https://morden.infura.io/metamask"))))

;; (defn mk-web3 []
;;   (or (aget js/window "web3")
;;       (web3/create-web3 "http://192.168.0.3:8545/")))

;;;
;;;
;;; GOAL UTILS
;;;
;;;
(defn default-goal []
  {:created-at (js/Date.now)
   :description ""
   :owner nil
   :sending? false
   :cancelling? false
   :cancelled? false})

(def default-db
  {:goals (hash-map)
   :settings {}
   :my-addresses []
   :current-address ""
   :accounts {}
   :new-goal (default-goal)
   :web3 (mk-web3)
   :provides-web3? (or (aget js/window "web3") goog.DEBUG)
   :contract {:name "GoalsStockExchange"
              :abi nil
              :bin nil
              :instance nil
              :address "0x407b422779b63fad1de37c3a4c3d6300d8e97fbd"}})
