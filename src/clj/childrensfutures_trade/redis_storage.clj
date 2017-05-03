(ns childrensfutures-trade.redis-storage
  (:require [childrensfutures-trade.db :as db]))

(deftype RedisStorage [server-conn]
  db/IStorage

  (refresh-db [this new-db]
    nil)

  (get-db [_]
    nil)

  (db-version [_]
    nil)

  (reset-db [_]
    nil))

;;;
;;; new instance of storage adaptor
;;;
;; (defn create []
;;   (RedisStorage. nil))
