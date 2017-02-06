(ns childrensfutures-trade.handlers.sync-db
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
 :sync-db/inc-db-version
 (interceptors)

 (fn [db [db-version]]
   (let [current-db-version (:db-version db)]
     (cond-> db
       (> db-version current-db-version)
       (assoc :db-version db-version)))))

(reg-event-fx
 :sync-db/print-db-version
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   (js/console.log :db-version (:db-version db))))
