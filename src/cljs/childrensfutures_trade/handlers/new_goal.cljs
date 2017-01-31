(ns childrensfutures-trade.handlers.new-goal
  (:require
   [ajax.core :as ajax]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   [cljsjs.web3]
   [cljs.spec :as s]
   [childrensfutures-trade.db :as db]
   [day8.re-frame.http-fx]
   [goog.string :as gstring]
   [goog.string.format]
   [madvas.re-frame.web3-fx]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [childrensfutures-trade.utils :as u]

   ;;
   ;; event handlers
   ;;
   [childrensfutures-trade.handlers.utils :refer [goal-gas-limit]]

   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]

   [childrensfutures-trade.handlers.utils :as hu]
   ))

;;;
;;;
;;; NEW GOAL
;;;
;;;


;;;
;;;
;;; update goal values while editing goal
;;;
;;;
(reg-event-db
 :new-goal.attribute/update
 (interceptors)
 (fn [db [key value]]
   (assoc-in db [:new-goal key] value)))


;;;
;;; send new goal to ethereum contract
;;;
(reg-event-fx
 :blockchain.new-goal/send
 (interceptors-fx :spec false)

 (fn [{:keys [db]} []]
   (let [{:keys [description
                 owner
                 give-in-return]} (:new-goal db)]
     (hu/blockchain-send-transaction
      db
      :gse-contract
      :new-goal
      [description give-in-return]
      :db-path [:new-goal]
      :confirmed-event [:blockchain.new-goal/confirmed]
      :error-event :log-error
      :receipt-loaded-event [:blockchain.new-goal/transaction-receipt-loaded]))))


;;;
;;; change new-goal's state to sending
;;; fired after the trx has been confirmed
;;; see event handler above
;;;
(reg-event-db
 :blockchain.new-goal/confirmed
 (interceptors)
 (fn [db [transaction-hash]]
   (-> db
       (assoc-in [:new-goal :trx-on-air?] true)
       (update :show-new-goal? not))))

;;;
;;; confirms that goal was sent to ethereum contract
;;;
(reg-event-db
 :blockchain.new-goal/transaction-receipt-loaded
 (interceptors)
 (fn [db [{:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   (-> db
       (assoc :new-goal (db/default-goal))
       (assoc-in [:new-goal :owner] (:current-address db)))))
