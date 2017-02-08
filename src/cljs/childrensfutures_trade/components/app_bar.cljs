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
  (let [balance (subscribe [:db/selected-address-balance])
        full-app? (subscribe [:app/full?])]
    (fn []
      [row {:middle "xs"}
       ;; NEW GOAL BUTTON
       ;; [ui/raised-button
       ;;  {:label "Your Goal"
       ;;   :secondary true
       ;;   :disabled @show-new-goal?
       ;;   :on-touch-tap #(dispatch [:ui.new-goal/toggle-view])
       ;;   :style {:margin-right "20px"
       ;;           :margin-top "5px"}}]

       (when @full-app?
         [:h4 (u/eth @balance)])

       ;; CHANGE ACCOUNT
       (when @full-app?
         [ui/icon-button
          {:tooltip "Update"
           :children (icons/notification-sync)
           :on-touch-tap #(do (dispatch [:blockchain.account/refresh]))}])])))

;;;
;;; app title
;;;
(defn- app-title []
  (let [current-page-name (subscribe [:ui/current-page-name])]
    [:span
     {:class-name "app-title"}
     (str "myfutures.trade / " @current-page-name)]))

;;;
;;; App bar
;;;
(defn app-bar-view []
  [ui/app-bar
   {:title (r/as-component [app-title])
    :on-left-icon-button-touch-tap #(dispatch [:ui.drawer/toggle-view])
    :icon-element-right (r/as-element [right-buttons])
    :style {:position "fixed"
            :top 0}}])
