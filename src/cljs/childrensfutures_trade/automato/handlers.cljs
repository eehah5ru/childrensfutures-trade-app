(ns childrensfutures-trade.automato.handlers
  (:require
   [ajax.core :as ajax]
   [cljs.spec :as s]
   [childrensfutures-trade.automato.db :as db]
   [day8.re-frame.http-fx]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :refer [reg-event-db reg-event-fx path trim-v after debug reg-fx console dispatch]]
   [childrensfutures-trade.utils :as u]))

(def interceptors [#_(when ^boolean js/goog.DEBUG debug)
                   trim-v])


(defn add-indexes-to-slides [slides]
  (map (fn [[i s]] (assoc s :index i))
       (map vector (iterate inc 0) slides)))

(reg-event-db
 :initialize
 (fn [_ _]
   (let [db (update db/default-db :slides add-indexes-to-slides)]
     (merge db
            {:current-slide (-> db
                                :slides
                                first)}))))


(reg-event-db
 :slide/next
 (fn [db]
   (js/console.log :debug (:slides db))
   (let [current-index (get-in db [:current-slide :index])
         next-index (inc current-index)
         last-slide? (= next-index (count (:slides db)))]
     (assoc db
             :current-slide
             (if last-slide?
               (-> db
                   :slides
                   first)
               (nth (:slides db) next-index))))))

(reg-event-db
 :slide/go-to
 (fn [db [slide-index]]
   db))
