(ns childrensfutures-trade.db)

(def default-db {:db-version 0
                 :goals (hash-map)
                 :pulse []})


(defprotocol IStorage
  ;;
  ;; update db
  ;;
  (refresh-db [this new-db])

  ;;
  ;; get db content
  ;;
  (get-db [this])

  ;;
  ;; get db version
  ;;
  (db-version [this])

  ;;
  ;; reset db to default state
  ;;
  (reset-db [this]))
