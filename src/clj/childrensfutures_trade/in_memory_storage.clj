(ns childrensfutures-trade.in-memory-storage
  (:require [clojure.java.io :as io]
            [ring.logger.onelog :as logger.onelog]

            [childrensfutures-trade.db :as db]
            ))

(defn new-db-storage []
  (atom db/default-db))

(deftype InMemoryStorage [db-storage]
  db/IStorage

  (refresh-db [this new-db]
    (let [current-version (get @db-storage :db-version 0)
          next-version (get new-db :db-version 0)]
      (do
        (reset! db-storage new-db)
        true)))

  (get-db [_]
    @db-storage)

  (db-version [_]
    (get @db-storage :db-version 0))

  (reset-db [_]
    (reset! db-storage db/default-db)))

(defn create []
  (->InMemoryStorage (new-db-storage)))
