(ns childrensfutures-trade.handlers.achieve-goal
  (:require
   [childrensfutures-trade.db :as db]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [childrensfutures-trade.utils :as u]

   ;;
   ;; event handlers
   ;;
   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]

   [childrensfutures-trade.handlers.utils :as hu]
   ))

;;;
;;;
;;; ACHIEVE GOAL
;;;
;;;

;;;
;;; send trx
;;;
(reg-event-fx
 :blockchain.achieve-goal/send
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [goal-id]]
   (hu/blockchain-send-transaction
    db
    :gse-contract
    :achieve-goal
    [goal-id]
    :db-path [goal-id]
    :confirmed-event [:blockchain.goal/transaction-confirmed goal-id]
    :error-event :log-error
    :receipt-loaded-event [:blockchain.goal/transaction-receipt-loaded goal-id])))
