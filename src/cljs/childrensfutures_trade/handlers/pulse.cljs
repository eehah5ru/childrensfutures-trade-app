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

(reg-event-db
 :pulse/push-goal-added
 (interceptors)

 (fn [db [number goal-id]]
   (let [events (:pulse db)
         goal-added (merge (db/default-pulse-goal-added)
                           {:number number
                            :goal-id goal-id})]
     ;; (js/console.log :debug :pulse-goal-added goal-added)
     (assoc db :pulse (conj events goal-added)))))

(reg-event-db
 :pulse/push-investment-placed
 (interceptors)

 (fn [db [number goal-id bid-id]]
   (let [events (:pulse db)
         investment-placed (merge (db/default-pulse-investment-placed)
                                  {:number number
                                   :goal-id goal-id
                                   :bid-id bid-id})]
     (assoc db :pulse (conj events investment-placed)))))
