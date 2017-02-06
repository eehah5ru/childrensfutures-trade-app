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
;;;
;;; common contract events
;;;
;;;

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

;;;
;;;
;;; goal lifecycle
;;;
;;;

;;;
;;; add goal
;;;
(reg-event-db
 :db.goal/add
 (interceptors)

 (fn [db [goal created-at]]
   (-> db
       (db/update-goal
        (:goal-id goal)
        #(merge % (select-keys goal [:owner
                                     :description
                                     :goal-id
                                     :give-in-return])
                {:created-at created-at}))

       (db/change-stage
        (:goal-id goal)
        :created))))

;;;
;;; cancel goal
;;;
(reg-event-db
 :db.goal/cancel
 (interceptors)

 (fn [db [goal]]
   (-> db
       (db/update-goal
        (:goal-id goal)
        #(merge % {:cancelled? true}))

       (db/change-stage (:goal-id goal) :cancelled))))

;;;
;;; place bid
;;;
(reg-event-db
 :db.goal/place-bid
 (interceptors)
 (fn [db [bid]]
   (-> db
       (db/change-stage (:goal-id bid) :bid-placed)

       (db/update-bid (:goal-id bid)
                      (:bid-owner bid)
                      #(merge %
                              (let [{:keys [bid-owner description goal-id]} bid]
                                {:goal-id goal-id
                                 :bid-id bid-owner ;FIXME use bid-id instead
                                 :owner bid-owner
                                 :description description}))))))


;;;
;;; select bid
;;;
(reg-event-db
 :db.goal/select-bid
 (interceptors)
 (fn [db [goal-id bid-id]]
   (-> db
       (db/change-stage goal-id :bid-selected)

       (db/update-bid goal-id
                      bid-id
                      #(merge % {:goal-id goal-id
                                 :bid-id bid-id
                                 :selected? true})))))


;;;
;;; send investment
;;;
(reg-event-db
 :db.goal/send-investment
 (interceptors)

 (fn [db [goal-id bid-id]]
   (db/change-stage db goal-id :investment-sent)))

;;;
;;; receive investment
;;;
(reg-event-db
 :db.goal/receive-investment
 (interceptors)

 (fn [db [goal-id bid-id]]
   (db/change-stage db goal-id :investment-received)))

;;;
;;; achieve goal
;;;
(reg-event-db
 :db.goal/achieve
 (interceptors)

 (fn [db [goal-id]]
   (db/change-stage db goal-id :goal-achieved)))

;;;
;;; ask bonus
;;;
(reg-event-db
 :db.goal/ask-bonus
 (interceptors)

 (fn [db [goal-id bid-id]]
   (db/change-stage db goal-id :bonus-asked)))

;;;
;;; send bonus
;;;
(reg-event-db
 :db.goal/send-bonus
 (interceptors)

 (fn [db [goal-id bid-id]]
   (db/change-stage db goal-id :bonus-sent)))

;;;
;;; complete goal
;;;
(reg-event-db
 :db.goal/complete
 (interceptors)

 (fn [db [goal-id]]
   (db/change-stage db goal-id :goal-completed)))
