(ns childrensfutures-trade.in-memory-storage
  (:require [clojure.java.io :as io]
            [ring.logger.onelog :as logger.onelog]
            ))

(def default-db {:db-version 0
                 :goals (hash-map)
                 :pulse []})

(def db (atom default-db))

(defn refresh-db [new-db]
  (let [current-version (get @db :db-version 0)
        next-version (get new-db :db-version 0)]
    (if (> next-version current-version)
      (do
       (reset! db new-db)
       true)
      false)))

(defn get-db []
  @db)

(defn db-version []
  (get @db :db-version 0))


(defn reset-db []
  (reset! db default-db))
