(ns childrensfutures-trade.handlers.sync-db
  (:require
   [ajax.core :as ajax]
   [cognitect.transit :as t]


   [childrensfutures-trade.db :as db]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console]]
   [childrensfutures-trade.utils :as u]

   ;;
   ;; event handlers
   ;;
   [childrensfutures-trade.handlers.interceptors :refer [interceptors
                                                         interceptors-fx]]

   [childrensfutures-trade.handlers.utils :as hu]
   ))

;;;
;;; constants
;;;

;;; min interval between syncing sessions
(def sync-interval 3000)

;;;
;;; utils
;;;

;;; get current timestamp as int
(defn current-timestamp []
  (.getTime (js/Date.)))

;;;
;;; db utils
;;;

;;; returns updated db
(defn update-db-version [db new-db-version]
  (let [current-db-version (:db-version db)]
    (if (> new-db-version current-db-version)
      (assoc db :db-version new-db-version)
      db)))

;;; returns updated db
(defn update-db-version-synced [db version]
  (let [current-synced-db-version (:db-version-synced db)]
    (assoc db :db-version-synced (min version current-synced-db-version))))

;;;
;;; select what to sync
;;;
(defn db-for-sync [db]
  (select-keys db [:db-version
                   :goals
                   :pulse]))

;;;
;;; db -> string
;;;
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
   {:db (-> db
            (update-db-version db-version)
            (update-db-version-synced db-version))
    :dispatch [:sync-db/check-and-sync]}))


(reg-event-fx
 :sync-db/check-and-sync
 (interceptors-fx :spec true)

 (fn [{:keys [db]}]
   (let [cur-db-version (:db-version db)
         cur-db-version-synced (:db-version-synced db)
         syncing? (:db-syncing? db)
         db-synced-at (:db-synced-at db)
         cur-timestamp (current-timestamp)
         sync-now? (> cur-timestamp
                      (+ db-synced-at
                         sync-interval))]
     (if (<= cur-db-version
            cur-db-version-synced)
       ;; do nothing
       {}
       ;; try to sync
       (cond
         (and (not syncing?)
              sync-now?)
         (do
           (js/console.log :syncing-now)
           {:db (assoc db :db-syncing? true)
            :dispatch [:sync-db/do-sync]})

         (and (not syncing?)
              (not sync-now?))
         (do
           (js/console.log :syncing-later db-synced-at)
           {:dispatch-later [{:ms sync-interval
                              :dispatch [:sync-db/check-and-sync]}]})

         :else
         {}))
     )))


;;;
;;; sync with server
;;;
(reg-event-fx
 :sync-db/do-sync
 (interceptors-fx :spec true)

 (fn [{:keys [db]}]
   (js/console.log :sync-db/syncing)

   (let [cur-db-version (:db-version db)
         form-data (doto (js/FormData.)
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
      :db (assoc db :db-version-synced cur-db-version)})))

;;;
;;; when synced successful
;;;
(reg-event-fx
 :sync-db/synced
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   (js/console.log :sync-db/synced)
   {:db (-> db
            (assoc :db-synced-at (current-timestamp)
                   :db-syncing? false))}))


;;;
;;; fetch db once and fire event
;;;
(reg-event-fx
 :sync-db/fetch
 (interceptors-fx :spec false)

 (fn [{:keys [db]} [on-fetched]]
   (js/console.log :sync-db/fetching)

   {:http-xhrio {:method :get
                 :params {:db-version (:db-version db)}
                 :uri "/fetch-db"
                 :response-format (ajax/raw-response-format)
                 :timeout 6000
                 :on-success [:sync-db/fetched on-fetched]
                 :on-failure [:log-error]}}))

;;;
;;; fetching db periodically
;;;
(reg-event-fx
 :sync-db/fetch-forever
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   {:dispatch [:sync-db/fetch
               {:dispatch-later [{:ms 5000
                                  :dispatch [:sync-db/fetch-forever]}]}]}))

;;;
;;; fetched
;;;
(reg-event-fx
 :sync-db/fetched
 (interceptors-fx :spec true)

 (fn [{:keys [db]} [{:keys [dispatch-later dispatch dispatch-n]} fetched-db]]
   (js/console.log :sync-db/fetched)

   (let [fetched-db (cljs.reader/read-string fetched-db)
         current-version (:db-version db)
         new-version (get fetched-db :db-version 0)
         need-update? (> new-version current-version)]
     (cond-> {:db db}
       need-update?
       (update :db #(merge % fetched-db))

       dispatch
       (assoc :dispatch (conj (vec dispatch) new-version))

       dispatch-n
       (assoc :dispatch-n (map #(conj % new-version) (vec dispatch-n)))

       dispatch-later
       (assoc :dispatch-later (map (fn [x]
                                     (update x :dispatch #(conj % new-version)))
                                   dispatch-later))))))


(reg-event-fx
 :sync-db/print-db-version
 (interceptors-fx :spec false)

 (fn [{:keys [db]}]
   (js/console.log :db-version (:db-version db))))
