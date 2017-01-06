(ns childrensfutures-trade.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [childrensfutures-trade.address-select-field :refer [address-select-field]]
    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.core :refer [get-mui-theme color]]
    [cljs-react-material-ui.icons :as icons]
    [childrensfutures-trade.utils :as u]
    [childrensfutures-trade.subs :as s]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.components.home-page :refer [home-page]]
    ;; [childrensfutures-trade.components.how-to-play-page :refer [how-to-play-page]]
    [childrensfutures-trade.components.about-page :refer [about-page]]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    ))

;; (def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
;; (def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

;;;
;;;
;;; COMPONENTS
;;;
;;;

(def pages
  {:home home-page
   :about about-page
   ;; :how-to-play how-to-play-page
   })

(def menu-pages
  [[:home "Home" icons/action-home]
   [:how-to-play "How To Play" icons/action-help]
   [:about "About" icons/action-info]])

(defn- menu-link [[route title icon]]
  [ui/list-item
   {:left-icon (icon)
    :href (u/path-for route)
    :key route} title])

;;;
;;;
;;; VIEWS
;;;
;;;

;;;
;;; SWITCH ACCOUNT
;;;
(defn- switch-account-view []
  ;; IMPORTANT!!!! let outside fn!!!
  (let [my-addresses (subscribe [:db/my-addresses])
        current-address (subscribe [:db/current-address])
        balance (subscribe [:db/selected-address-balance])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [ui/paper {:style {:padding 20 :margin-top 20}}
         [:h1 "Change Ethereum address"]
         [address-select-field
          @my-addresses
          @current-address
          [[:new-goal/update :owner] [:current-address/update] [:accounts/toggle-view]]]

         [:br]
         [:h3 "Balance: " (u/eth @balance)]]]])))

;;;
;;;
;;; NEW GOAL
;;;
;;;
(defn- new-goal-component []
  (let [new-goal (subscribe [:db/new-goal])]
    [row
     [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
      [ui/paper {:style {:padding 20 :margin-top 20}}
       [:h1 "New Goal"]
       [ui/text-field {:default-value (:description @new-goal)
                       :on-change #(dispatch [:new-goal/update :description (u/evt-val %)])
                       :name "description"
                       :max-length 120 ;FIXME
                       :floating-label-text "Goal's description"
                       :style {:width "100%"}}]
       [:br]
       ;;
       ;; add goal
       ;;
       [ui/raised-button
        {:secondary true
         :disabled (or (empty? (:description @new-goal))
                       (:sending? @new-goal))
         :label "Place on Exchange"
         :style {:margin-top 15}
         :on-touch-tap #(dispatch [:new-goal/send])}]
       ;;
       ;; cancel
       ;;
       [ui/flat-button
        {:secondary true
         :disabled false
         :label "cancel"
         :on-touch-tap #(dispatch [:new-goal/toggle-view])}]
       ]]

     ]
    ))



;;;
;;; App bar
;;;
(defn- app-bar-view []
  (let [show-new-goal? (subscribe [:db/show-new-goal?])]
    (fn []
      [ui/app-bar {:title "Goals Exchange Market"
                   :on-left-icon-button-touch-tap #(dispatch [:drawer/toggle-view])
                   :icon-element-right (r/as-element
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
                                                              (dispatch [:accounts/toggle-view]))}]])}])))


;;;
;;; DRAWER
;;;
(defn- drawer-view []
  (let [drawer-open? (subscribe [:ui/drawer-open?])]
    (fn []
      [ui/drawer {:open @drawer-open?
                  :docked false
                  :on-request-change #(dispatch [:drawer/toggle-view])}

       [ui/app-bar {:title "myFutures"
                    :show-menu-icon-button false}]
       (for [menu-page menu-pages]
          (menu-link menu-page))])))

;;;
;;; main panel
;;;
(defn main-panel []
  (let [show-new-goal? (subscribe [:db/show-new-goal?])
        sending-new-goal? (subscribe [:db/sending-new-goal?])
        show-accounts? (subscribe [:db/show-accounts?])
        drawer-open? (subscribe [:ui/drawer-open?])
        current-page (subscribe [:ui/current-page])]
    (fn []
      {:fluid true}
      [ui/mui-theme-provider
       {:mui-theme (get-mui-theme {:palette {:primary-color (color :light-blue500)
                                             :accent-color (color :amber700)}})}
       [:div
        [app-bar-view]

        (when @drawer-open?
          [drawer-view])

        (when @show-new-goal?
          [new-goal-component])

        (when @show-accounts?
          [switch-account-view])

        ;;
        ;; show active-page
        ;;
        [grid {:fluid true
               :style st/main-grid}
         (when-let [page (pages (:handler @current-page))]
           [page])
         ]

        [ui/snackbar {:message "Adding Goal"
                      :open @sending-new-goal?}]]])))
