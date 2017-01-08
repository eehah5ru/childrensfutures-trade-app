(ns childrensfutures-trade.automato.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :ui/current-slide
 (fn [db]
   (:current-slide db)))
