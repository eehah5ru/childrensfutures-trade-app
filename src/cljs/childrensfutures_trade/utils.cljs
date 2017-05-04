(ns childrensfutures-trade.utils
  (:require [cljs-time.coerce :refer [to-date-time to-long to-local-date-time]]
            [cljs-time.core :refer [date-time to-default-time-zone]]
            [cljs-time.format :as time-format]
            [cljs-web3.core :as web3]
            [reagent.core :as r]
            [goog.dom :as gd]
            [goog.fx.dom :as gfd]
            [clojure.string :as str]
            [bidi.bidi :as bidi]

            ))

;;; local figwheel if true
(goog-define DEV false)
;;; dev.myfutures.trade if true
(goog-define STAGING false)

;;; could be :production, :staging or :private
(goog-define CONTRACTS "production")

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

;;;
;;;
;;; childern nodes utils
;;;
;;;

;;;
;;; deep merge properties
;;;
;; (defn- merge-props [& props]
;;   (apply merge-with merge props ))

(defn extract-props [v]
  (let [p (nth v 0 nil)]
    (if (map? p) p)))

(defn extract-children [v]
  (let [p (nth v 0 nil)
        first-child (if (or (nil? p) (map? p)) 1 0)]
    (if (> (count v) first-child)
      (subvec v first-child))))


;;;
;;; deep merge properties
;;;

(defn merge-props [props props-and-children]
  (let [p (extract-props props-and-children)
        c (extract-children props-and-children)]
    (letfn [(mergef [& maps]
              (apply merge-with mergef maps))]
      (-> p
          (as-> xs (merge-with mergef xs props))
          vector
          (concat c)
          vec))))

#_(let [a {:a 1 :b {:c 1 :d {:f 1}}}
      b {:e 2 :b {:g 3 :d {:h 5}}}]
  (letfn []
    (merge-with mergef a b)))

;;;
;;;
;;; CHAT UTILS
;;;
;;;
(defn chat-channel-id [& ids]
  (->> ids
       (flatten)
       (apply str)
       (web3/sha3)))

;;;
;;;
;;; UI utils
;;;
;;;
(defn- scroll! [el start end time]
  ;; (js/console.log :debug :scroll el)
  (let [start (clj->js start)
        end (clj->js end)
        scroll-o (goog.fx.dom.Scroll. (clj->js el) start end time)]
    (.play scroll-o)))

(defn scroll-to-bottom [el-id time]
  (let [el (goog.dom/getElement el-id)
        start [0 (.-scrollTop el)]
        end [0 (.-scrollHeight el)]]
    (scroll! el start end time)))

;;;
;;;
;;; PATH utils
;;;
;;;
;; (def path-for (partial bidi/path-for pages/routes))


;;;
;;;
;;; dynamically invoce function
;;;
;;;
(defn ->js [var-name]
      (-> var-name
          (str/replace #"/" ".")
          (str/replace #"-" "_")))


(defn invoke [function-name & args]
      (let [fun (js/eval (->js function-name))]
           (apply fun args)))
