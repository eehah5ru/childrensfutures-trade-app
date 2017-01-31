(ns childrensfutures-trade.handlers.send-bonus
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
;;; ASK BONUS
;;;
;;;

;;;
;;; send trx
;;;
(reg-event-fx
 :blockchain.send-bonus/send
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [goal-id bid-id]]
   (hu/blockchain-send-transaction
    db
    :gse-contract
    :send-bonus
    [goal-id bid-id]
    :db-path [goal-id bid-id]
    :confirmed-event [:blockchain.goal/transaction-confirmed goal-id]
    :error-event :log-error
    :receipt-loaded-event [:blockchain.goal/transaction-receipt-loaded goal-id])))
