(ns childrensfutures-trade.handlers.goal
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
;;; mark goal as on air
;;; if trx was confirmed by user
;;;
(reg-event-db
 :blockchain.goal/transaction-confirmed
 (interceptors)
 (fn [db [goal-id tx-hash]]
   (assoc-in db [:goals goal-id :trx-on-air?] true)))


;;;
;;; confirms that bid was selected
;;;
(reg-event-db
 :blockchain.goal/transaction-receipt-loaded
 (interceptors)

 (hu/blockchain-trx-receipt-loaded
  (fn [db [goal-id _]]
    (-> db
        (assoc-in [:goals goal-id :trx-on-air?] false)))))
