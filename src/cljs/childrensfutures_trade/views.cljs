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
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]

    [childrensfutures-trade.pages :refer [pages]]

    ;;
    ;; components
    ;;
    [childrensfutures-trade.components.drawer :refer [drawer-view]]

    [childrensfutures-trade.components.new-goal :refer [new-goal-button
                                                        new-goal-dialog]]

    [childrensfutures-trade.components.app-bar :refer [app-bar-view]]

    [childrensfutures-trade.components.accounts :refer [switch-account-dialog]]

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


;;;
;;; main panel
;;;
(defn main-panel []
  (let [show-new-goal? (subscribe [:db/show-new-goal?])
        sending-new-goal? (subscribe [:db/sending-new-goal?])

        drawer-open? (subscribe [:ui/drawer-open?])
        current-page (subscribe [:ui/current-page])]
    (fn []
      ;; {:fluid true}
      [ui/mui-theme-provider
       {:mui-theme (get-mui-theme {:palette {:primary-color (color :light-blue500)
                                             :accent-color (color :amber700)}})}
       [:div
        {:style {:position "relative"}}

        [app-bar-view]

        (when @drawer-open?
          [drawer-view])

        ;;
        ;; switch account dialog
        ;;
        [switch-account-dialog]

        ;;
        ;; new goal dialog
        ;; visibility is controlled inside the dialog
        ;;
        [new-goal-dialog]

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

        ;; [ui/snackbar {:message "Adding Goal"
        ;;               :open @sending-new-goal?}]
        ]])))
