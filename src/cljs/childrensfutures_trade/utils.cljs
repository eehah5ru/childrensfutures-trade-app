(ns childrensfutures-trade.utils
  (:require [cljs-time.coerce :refer [to-date-time to-long to-local-date-time]]
            [cljs-time.core :refer [date-time to-default-time-zone]]
            [cljs-time.format :as time-format]
            [cljs-web3.core :as web3]
            [bidi.bidi :as bidi]
            [childrensfutures-trade.routes :refer [routes]]))

(defn truncate
  "Truncate a string with suffix (ellipsis by default) if it is
   longer than specified length."
  ([string length]
   (truncate string length "..."))
  ([string length suffix]
   (let [string-len (count string)
         suffix-len (count suffix)]
     (if (<= string-len length)
       string
       (str (subs string 0 (- length suffix-len)) suffix)))))

(defn evt-val [e]
  (aget e "target" "value"))

(defn big-number->date-time [big-num]
  (to-date-time (* (.toNumber big-num) 1000)))

(defn eth [big-num]
  (str (web3/from-wei big-num :ether) " ETH"))

(defn format-date [date]
  (time-format/unparse-local (time-format/formatters :rfc822) (to-default-time-zone (to-date-time date))))

(defn extract-props [v]
  (let [p (nth v 0 nil)]
    (if (map? p) p)))

(defn extract-children [v]
  (let [p (nth v 0 nil)
        first-child (if (or (nil? p) (map? p)) 1 0)]
    (if (> (count v) first-child)
      (subvec v first-child))))


(def path-for (partial bidi/path-for routes))
