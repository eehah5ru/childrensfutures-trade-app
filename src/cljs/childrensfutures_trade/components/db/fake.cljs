(ns childrensfutures-trade.db.fake
  (:require
            [cljs.reader]
            [cljs.spec :as s]
            [childrensfutures-trade.db :as d]))

;;;
;;;
;;; generator of fake text
;;;
;;;
(def default-min-short-text-length 10)
(def default-max-short-text-length 20)

(def default-min-long-text-length 45)
(def default-max-long-text-length 150)

(def fake-text "a speculative computer club, is a platform where we take the present to pieces and then gather them together according to the rules that we have made up ourselves. Within the scope of the club we are planning: to mix technological with political in an antidisciplinary way; to make meetings with economists, hackers, programmers, biologists, futurologists, philosophers, etc.; to make city trips in order to explore infrastructure on-site; to have fun and bully; to use economical models of virtual worlds as exercisers for building-up physique in existing economies; to raise computer knowledge; to make workshops on gathering different technical stuff in order to give our speculation a material form. The computer club, where we can use games not as intended, the club that treats the present eyes wide open and appropriate technologies that can be reused. ")


(defn get-text [min-len max-len]
  (let [len (min (.length fake-text)
                 (+ min-len (rand-int (- max-len min-len))))]
    (subs fake-text 0 len)))

(defn short-text
  ([]
   (short-text default-min-long-text-length default-max-long-text-length))

  ([min-len max-len]
   (get-text min-len max-len)))

(defn long-text
  ([]
   (long-text default-min-long-text-length default-max-long-text-length))

  ([min-len max-len]
   (get-text min-len max-len)))
