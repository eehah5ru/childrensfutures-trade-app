(ns childrensfutures-trade.components.avatars
  (:require
   [cljs-react-material-ui.icons :as icons]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [color]]
   [medley.core :as medley]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]
   [clavatar-js.core :as clavatar]

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
;;; goal avatar
;;;
(defn goal-avatar [goal]
  (let [goals-count (subscribe [:db.goals/count (:owner goal)])]
    [ui/badge
     {:badge-content @goals-count
      :badge-style st/goal-owner-avatar-badge}
     [ui/avatar {:style {:margin-top 5
                         :margin-right 20}
                 :src (clavatar/gravatar (:owner goal))}]]))

;;;
;;; bid avatar
;;;
(defn  bid-avatar [bid]
  (let [goals-count (subscribe [:db.goals/count (:owner bid)])]
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
      [ui/avatar {:style {;;:margin-left "15px"
                          ;; :position "absolute"
                          ;; :margin-left "-60px"
                          ;; :top "50%"
                          ;;:transform "translateY(-50%)"
                          }
                  :src (clavatar/gravatar (:owner bid))}]]]))
