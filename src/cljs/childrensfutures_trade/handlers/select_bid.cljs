(ns childrensfutures-trade.handlers.select-bid
  (:require
   [ajax.core :as ajax]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.personal :as web3-personal]
   ;; [cljsjs.web3]
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
;;; SELECT BID
;;;
;;;

;;;
;;; make select bid trx in the ethereum
;;;
(reg-event-fx
 :blockchain.select-bid/send
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [goal-id bid-id]]
   (hu/blockchain-send-transaction
    db
    :gse-contract
    :select-bid
    [goal-id bid-id]
    :db-path [goal-id :select-bid bid-id]
    :confirmed-event [:blockchain.goal.select-bid/transaction-confirmed goal-id bid-id]
    :error-event :log-error
    :receipt-loaded-event [:blockchain.goal.select-bid/transaction-receipt-loaded goal-id bid-id])))


;;;
;;; change state of selected bid
;;; if trx was confirmed by user
;;;
(reg-event-fx
 :blockchain.goal.select-bid/transaction-confirmed
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [goal-id bid-id tx-hash]]
   {:db (-> db
            (assoc-in [:goals goal-id :trx-on-air?] true)
            (assoc-in [:goals goal-id :bids bid-id :selecting?] true))
    :dispatch [:ui.snackbar/show "Saving your decision into blockchain. Please wait!"]}))

;;;
;;; confirms that bid was selected
;;;
(reg-event-fx
 :blockchain.goal.select-bid/transaction-receipt-loaded
 (interceptors-fx :spec true)
 (fn [{:keys [db]} [goal-id bid-id & {:keys [gas-used] :as transaction-receipt}]]
   (console :log transaction-receipt)
   (when (= gas-used goal-gas-limit)
     (console :error "All gas used"))
   {:db (-> db
       (assoc-in [:goals goal-id :trx-on-air?] false)
       (assoc-in [:goals goal-id :bids bid-id :selecting?] false))
    :dispatch [:ui.snackbar/show-updating]}))
