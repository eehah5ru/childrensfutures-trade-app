(ns childrensfutures-trade.utils
  (:require [clojure.java.io :as io]))


(defn parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))
