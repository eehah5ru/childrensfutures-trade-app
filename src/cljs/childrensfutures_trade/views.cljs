(ns childrensfutures-trade.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.core :refer [get-mui-theme color]]
    [cljs-react-material-ui.icons :as icons]
    [childrensfutures-trade.utils :as u]
    [childrensfutures-trade.subs :as s]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.components.layout :refer [grid
                                                      row
                                                      col
                                                      outer-paper
                                                      full-width-paper]]

    [childrensfutures-trade.pages :refer [pages]]

    ;;
    ;; components
    ;;
    [childrensfutures-trade.components.drawer :refer [drawer-view]]

    [childrensfutures-trade.components.chat :refer [chat-drawer-view]]

    [childrensfutures-trade.components.new-goal :refer [new-goal-button
                                                        new-goal-dialog]]

    [childrensfutures-trade.components.app-bar :refer [app-bar-view]]

    ;; [childrensfutures-trade.components.accounts :refer [switch-account-dialog]]

    [childrensfutures-trade.components.goals :refer [place-bid-dialog
                                                     confirm-bid-selection-dialog
                                                     view-goal-dialog]]

    ;;
    ;; pages
    ;;
    [childrensfutures-trade.components.home-page :refer [home-page]]
    [childrensfutures-trade.components.my-goals-page :refer [my-goals-page]]
    [childrensfutures-trade.components.my-investments-page :refer [my-investments-page]]
    [childrensfutures-trade.components.pulse-page :refer [pulse-page]]
    [childrensfutures-trade.components.about-page :refer [about-page]]
    [childrensfutures-trade.components.how-to-play-page :refer [how-to-play-page]]
    [childrensfutures-trade.components.view-goal-page :refer [view-goal-page]]

    ))

;; (def col (r/adapt-react-class js/ReactFlexboxGrid.Col))
;; (def row (r/adapt-react-class js/ReactFlexboxGrid.Row))

;;;
;;;
;;; COMPONENTS
;;;
;;;



;;;
;;;
;;; VIEWS
;;;
;;;
(defn- error-modal []
  (let [web3-available? (subscribe [:contract/web3-available?])]
    [ui/dialog
     {:modal true
      :title "Oups! Error..."
      :open (not (if @web3-available? true false))}

     [:p "Welcome! Looks like your browser can't handle Ethereum yet. Please see How to Play"]]))

;;;
;;;
;;; FOOTER
;;;
;;;
(defn- footer []
  [full-width-paper
   {:paper-props {:z-depth 0
                  :style {:text-align "center"
                          :padding-top "20px"
                          :padding-bottom "20px"
                          :background-color (color :grey-900)
                          :color "white"
                          }}}
   [:div "Produced by "
    [:a {:href "http://eeefff.org/"
         :target "blank"}
     "eeefff"]
    " and "
    [:a {:href "http://pikene.no"
         :target "blank"}
     "Pikene på Broen"]
    " For "
    [:a {:href "http://barentsspektakel.no"
         :target "blank"}
     "Barents Spektakel 2017"]]])

;;;
;;; main panel
;;;
(defn main-panel []
  (let [current-page (subscribe [:ui/current-page])
        full-app? (subscribe [:app/full?])
        read-only-app? (subscribe [:app/read-only?])]
    (fn []
      ;; {:fluid true}
      [ui/mui-theme-provider
       {:mui-theme (get-mui-theme {:palette
                                   {:primary-color (color :light-blue500)
                                    :accent-color (color :amber700)}})}
       [:div
        {:style {:position "relative"}}

        [app-bar-view]

        [drawer-view]

        (when @full-app?
          [chat-drawer-view])

        ;;
        ;; error modal window
        ;;
        ;; [error-modal]

        ;;
        ;; switch account dialog
        ;;
        ;; [switch-account-dialog]

        ;;
        ;; new goal dialog
        ;; visibility is controlled inside the dialog
        ;;
        (when @full-app?
          [new-goal-dialog])

        [view-goal-dialog]

        ;;
        ;; place bid dialog
        ;; visiblity is controlled inside
        ;;
        (when @full-app?
          [place-bid-dialog])

        (when @full-app?
          [confirm-bid-selection-dialog])

        ;;
        ;; show active-page
        ;;
        [grid {:fluid true
               :style st/main-grid}
         (when-let [page (pages (:handler @current-page))]
           [page])
         ]

        ;;
        ;; show add goal button
        ;;
        [new-goal-button]

        [footer]

        ;; [ui/snackbar {:message "Adding Goal"
        ;;               :open @sending-new-goal?}]
        ]
       ])))
