(ns childrensfutures-trade.components.app-bar
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



(defn- right-buttons []
  (let [show-new-goal? (subscribe [:db/show-new-goal?])]
    (fn []
      [row {:middle "xs"}
       ;; NEW GOAL BUTTON
       [ui/raised-button
        {:label "Your Goal"
         :secondary true
         :disabled @show-new-goal?
         :on-touch-tap #(dispatch [:new-goal/toggle-view])
         :style {:margin-right "20px"
                 :margin-top "5px"}}]
       ;; CHANGE ACCOUNT
       [ui/icon-button
        {:tooltip "Change account"
         :children (icons/notification-sync)
         :on-touch-tap #(do (dispatch [:blockchain/load-my-addresses])
                            (dispatch [:accounts/toggle-view]))}]])))

;;;
;;; App bar
;;;
(defn app-bar-view []
  [ui/app-bar
   {:title "Goals Exchange Market"
    :on-left-icon-button-touch-tap #(dispatch [:drawer/toggle-view])
    :icon-element-right (r/as-element [right-buttons])
    :style {:position "fixed"
            :top 0}}])
