(ns childrensfutures-trade.handlers.send-investment
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
;;; SEND INVESTMENTS
;;;
;;;

;;;
;;; make send investments trx in the ethereum
;;;
(reg-event-fx
 :blockchain.send-investment/send
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [goal-id bid-id]]
   (hu/blockchain-send-transaction
    db
    [goal-id bid-id]
    :send-investment
    :db-path [goal-id bid-id]
    :confirmed-event [:blockchain.goal/transaction-confirmed goal-id]
    :error-event :log-error
    :receipt-loaded-event [:blockchain.goal/transaction-receipt-loaded goal-id])))
