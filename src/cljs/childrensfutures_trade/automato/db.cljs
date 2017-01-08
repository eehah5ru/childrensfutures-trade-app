(ns childrensfutures-trade.automato.db
  (:require
   [cljs.reader]
   [cljs.spec :as s]))


(defn black-slide []
  {:type :black})

(defn pic-slide [_]
  {:type :pic})

(defn video-slide [_]
  {:type :video})


(def default-db
  {:slides
   [
    ;; 1
    (black-slide)

    ;; 2
    (pic-slide :a)

    ;; 3
    (video-slide :b)
    ]
   :current-slide nil})
