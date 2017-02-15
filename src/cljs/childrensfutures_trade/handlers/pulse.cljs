(ns childrensfutures-trade.handlers.pulse
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
;;; generic push event
;;;
(defn push-event [db type default-f number event-o]
  (let [events (:pulse db)
        event-data (merge (default-f)
                          {:number number}
                          event-o)]
    ;; (js/console.log :debug :pulse-goal-added goal-added)
    (assoc db :pulse (conj events event-data))) )

(reg-event-db
 :pulse/push-goal-added
 (interceptors)

 (fn [db [number goal-id]]
   (push-event db
               :goal-added
               db/default-pulse-goal-added
               number
               {:goal-id goal-id})))

(reg-event-db
 :pulse/push-investment-placed
 (interceptors)

 (fn [db [number goal-id bid-id]]
   (push-event db
               :investment-placed
               db/default-pulse-investment-placed
               number
               {:goal-id goal-id
                :bid-id bid-id})))


(reg-event-db
 :pulse/push-investment-sent
 (interceptors)

 (fn [db [number goal-id bid-id]]
   (push-event db
               :investment-sent
               db/default-pulse-investment-sent
               number
               {:goal-id goal-id
                :bid-id bid-id})))

(reg-event-db
 :pulse/push-investment-received
 (interceptors)

 (fn [db [number goal-id bid-id]]
   (push-event db
               :investment-received
               db/default-pulse-investment-received
               number
               {:goal-id goal-id
                :bid-id bid-id})))

(reg-event-db
 :pulse/push-goal-achieved
 (interceptors)

 (fn [db [number goal-id]]
   (push-event db
               :goal-achieved
               db/default-pulse-goal-achieved
               number
               {:goal-id goal-id})))

(reg-event-db
 :pulse/push-bonus-asked
 (interceptors)

 (fn [db [number goal-id bid-id]]
   (push-event db
               :bonus-asked
               db/default-pulse-bonus-asked
               number
               {:goal-id goal-id
                :bid-id bid-id})))

(reg-event-db
 :pulse/push-bonus-sent
 (interceptors)

 (fn [db [number goal-id bid-id]]
   (push-event db
               :bonus-sent
               db/default-pulse-bonus-sent
               number
               {:goal-id goal-id
                :bid-id bid-id})))

(reg-event-db
 :pulse/push-goal-completed
 (interceptors)

 (fn [db [number goal-id]]
   (push-event db
               :goal-completed
               db/default-pulse-goal-completed
               number
               {:goal-id goal-id})))

(reg-event-db
 :pulse/push-goal-cancelled
 (interceptors)

 (fn [db [number goal-id]]
   (push-event db
               :goal-cancelled
               db/default-pulse-goal-cancelled
               number
               {:goal-id goal-id})))
