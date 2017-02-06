(ns childrensfutures-trade.handlers.sync-db
  (:require
   [ajax.core :as ajax]
   [cognitect.transit :as t]


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
;;; utils
;;;

;;; returns updated db
(defn update-db-version [db new-db-version]
  (let [current-db-version (:db-version db)]
    (if (> new-db-version current-db-version)
      (assoc db :db-version new-db-version)
      db)))

;;;
;;;
;;;
(defn db-for-sync [db]
  (select-keys db [:db-version
                   :goals
                   :pulse]))

(defn serialize-db [db]
  (let [w (t/writer :json)
        data (db-for-sync db)]
    (t/write w data)))
;;;
;;;
;;;
;;; events
;;;
;;;

(reg-event-fx
 :sync-db/on-db-updated
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [db-version]]
   {:db (update-db-version db db-version)
    :dispatch [:sync-db/sync]}))


;;;
;;; sync with server
;;;
(reg-event-fx
 :sync-db/sync
 (interceptors-fx :spec true)

 (fn [{:keys [db]}]
   (js/console.log :sync-db/syncing)

   (let [form-data (doto (js/FormData.)
                     (.append "db" (db-for-sync db)))]
     {:http-xhrio {:method :post
                   :format :transit
                   ;; :params {:db {:a 1}}
                   :uri "/refresh-db"
                   :response-format :transit
                   :keywords? true
                   :timeout 6000
                   :on-success [:sync-db/synced]
                   :on-failure [:log-error]
                   ;; :body (js/FormData. (serialize-db {:a 1}))
                   :body form-data
                   }
      :db db})))

;;;
;;; when synced successful
;;;
(reg-event-fx
 :sync-db/synced
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   (js/console.log :sync-db/synced)
   {:db db}))

;;;
;;; fetch db
;;;
(reg-event-fx
 :sync-db/fetch
 (interceptors-fx :spec true)

 (fn [{:keys [db]}]
   (js/console.log :sync-db/fetching)

   {:http-xhrio {:method :get
                 :params {:db-version (:db-version db)}
                 :uri "/fetch-db"
                 :response-format (ajax/raw-response-format)
                 :timeout 6000
                 :on-success [:sync-db/fetched]
                 :on-failure [:log-error]}
    :db db}))

;;;
;;; fetched
;;;
(reg-event-fx
 :sync-db/fetched
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [fetched-db]]
   (js/console.log :sync-db/fetched)

   (let [fetched-db (cljs.reader/read-string fetched-db)
         current-version (:db-version db)
         new-version (get fetched-db :db-version 0)
         need-update? (> new-version current-version)]
     {:db (cond-> db
            need-update?
            (merge fetched-db))
      :dispatch-later [{:ms 5000
                      :dispatch [:sync-db/fetch]}]})))


(reg-event-fx
 :sync-db/print-db-version
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   (js/console.log :db-version (:db-version db))))
