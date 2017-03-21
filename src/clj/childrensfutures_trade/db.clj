(ns childrensfutures-trade.db)

(def default-db {:db-version 0
                 :goals (hash-map)
                 :pulse []})


(defprotocol IStorage
  (refresh-db [this new-db])

  (get-db [this])

  (db-version [this])

  (reset-db [this]))
