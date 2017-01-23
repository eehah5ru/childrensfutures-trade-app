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


(defn- new-goal-view [new-goal]
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


(defn new-goal-dialog []
  (let [show-new-goal? (subscribe [:ui/show-new-goal?])
        new-goal (subscribe [:db/new-goal])
        create-button [ui/raised-button
                       {:secondary true
                        :disabled (or (empty? (:description @new-goal))
                                      (:trx-on-air? @new-goal))
                        :label "Place on Exchange"
                        :style {:margin-top 15}
                        :on-touch-tap #(dispatch [:blockchain.new-goal/send])}]
        cancel-button [ui/flat-button
                       {:secondary true
                        :disabled false
                        :label "cancel"
                        :on-touch-tap #(dispatch [:ui.new-goal/toggle-view])}]]

    [ui/dialog
     {:modal true
      :actions [(r/as-element create-button)
                (r/as-element cancel-button)]
      :children (r/as-element [new-goal-view @new-goal])
      :open @show-new-goal?}]))

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
            :bottom 20
            :z-index 99}}])
