(ns childrensfutures-trade.handlers.utils
  (:require
   [cljs.spec :as s]
   [childrensfutures-trade.db :as db]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [childrensfutures-trade.utils :as u]

   ))


(def goal-gas-limit 1000000)
