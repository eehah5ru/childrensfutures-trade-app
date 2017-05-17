(ns childrensfutures-trade.components.goal.utils
  (:require
   [cljs-react-material-ui.reagent :as ui]
   [re-frame.core :refer [subscribe dispatch]]

   [childrensfutures-trade.styles :as st]))


(def default-card-properties
  {:card-style (constantly st/goal-card)
   :card-text (constantly [:h1 "Place here some text"])
   :card-actions (constantly [])
   :card-subtitle-extra nil
   :goal-statuses-extra (constantly [])})

(defn card-properties [props]
  (merge default-card-properties
         props))

;;;
;;; select according to goal's role
;;;
(defn with-role [goal selections default]
  (let [goal-id (:goal-id goal)
        role (subscribe [:role/role goal-id])]
    (get selections @role default)))

;;;
;;;
;;; UI ELEMS
;;;
;;;

(defn card-title [props]
  [ui/card-title
   (merge {:title "Place some text here!!!!"
           :class-name "goal-card-title"}
          props)])
