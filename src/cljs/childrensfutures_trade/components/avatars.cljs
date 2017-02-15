(ns childrensfutures-trade.components.avatars
  (:require
   [cljs-react-material-ui.icons :as icons]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [color]]
   [medley.core :as medley]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]
   [clavatar-js.core :as clavatar]
   [clojure.string :refer [blank?]]

   [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
   [childrensfutures-trade.styles :as st]
   [childrensfutures-trade.utils :as u]

   ))

;;;
;;;
;;; AVATARS
;;;
;;;

;;;
;;; generic version of avatar
;;;
(defn avatar [avatar-id & {:keys [avatar-style]
                           :or {avatar-style {}}}]
  (let [avatar-id (cond
                    (nil? avatar-id)
                    "unknwon-owner"

                    (blank? avatar-id)
                    "unknwon-owner"

                    :else
                    avatar-id)]
   [ui/avatar {:style avatar-style
               :src (clavatar/gravatar avatar-id)}]))

;;;
;;; goal avatar
;;;
(defn goal-avatar [goal]
  (let [avatar-id (get goal :owner "unknwon-owner")
        goals-count (subscribe [:db.goals/count (:owner goal)])]
    [ui/badge
     {:badge-content @goals-count
      :badge-style st/goal-owner-avatar-badge}
     [avatar avatar-id
      {:avatar-style {:margin-top 5
                      :margin-right 20}}]]))

;;;
;;; bid avatar
;;;
(defn  bid-avatar [bid]
  (let [avatar-id (get bid :owner "unknwon-owner")
        goals-count (subscribe [:db.goals/count (:owner bid)])]
    [:div
     {:style {:position "absolute"
              :left "15px"
              :transform "translateY(-50%)"
              :top "50%"}}
     [ui/badge
      {:badge-content @goals-count
       :badge-style st/bid-owner-avatar-badge
       :style {:display "block"
               :padding 0
               :position "relative"
               :height 40
               :width 40}}
      [avatar avatar-id]]]))
