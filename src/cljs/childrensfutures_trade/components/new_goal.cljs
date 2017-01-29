(ns childrensfutures-trade.components.new-goal
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.icons :as icons]
    [childrensfutures-trade.utils :as u]
    [childrensfutures-trade.subs :as s]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    ))


#_(defn- new-goal-view [new-goal]
  [:div
   [:h1 "New Goal"]
   [ui/text-field
    {:default-value (:description new-goal)
     :on-change #(dispatch [:new-goal.attribute/update :description (u/evt-val %)])
     :name "description"
     :max-length 2000                   ;FIXME
     :multi-line true
     :rows 3
     :rows-max 5
     :floating-label-text "Goal's description"
     :style {:width "100%"}}]
   [ui/text-field
    {:default-value (:give-in-return new-goal)
     :on-change #(dispatch [:new-goal.attribute/update :give-in-return (u/evt-val %)])
     :name "give-in-return"
     :max-length 2000                   ;FIXME
     :multi-line true
     :rows 3
     :rows-max 5
     :floating-label-text "What I'll give in return"
     :style {:width "100%"}}]
   ])

(defn- new-goal-view []
  [:iframe
   {:src "https://www.youtube.com/embed/2rzdt3ytH_Y?rel=0&autoplay=1"
    :frame-border "0"
    :allow-full-screen true
    :style {:position "absolute"
            :top 0
            :left 0
            :width "100%"
            :height "100%"}}])

(defn new-goal-dialog []
  (let [show-new-goal? (subscribe [:ui/show-new-goal?])
        create-button [ui/raised-button
                       {:secondary true
                        :disabled false
                        :label "Place on Exchange"
                        :style {:margin-top 15}
                        :on-touch-tap #(dispatch [:blockchain.new-goal/send])}]
        cancel-button [ui/flat-button
                       {:secondary true
                        :disabled false
                        :label "cancel"
                        :on-touch-tap #(dispatch [:ui.new-goal/toggle-view])}]]

    [ui/dialog
     {:modal false
      :body-style {:position "relative"
                   :padding-bottom "56.25%" ; /* 16:9 */
                   :padding-top "0px"      ;
                   :height "0"}
      :actions []
      :on-request-close #(dispatch [:ui.new-goal/toggle-view])
      :open @show-new-goal?}
     [new-goal-view]]))

;;;
;;; NEW GOAL BUTTON
;;;
(defn new-goal-button []
  [ui/floating-action-button
   {:on-touch-tap #(dispatch [:ui.new-goal/toggle-view])
    :children (icons/content-add)
    :z-depth 2
    :style {:position "fixed"
            :right 20
            :bottom "34px"
            :z-index 99}}])
