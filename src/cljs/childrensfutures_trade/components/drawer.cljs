(ns childrensfutures-trade.components.drawer
  (:require
    [medley.core :as medley]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]

    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.utils :as u]

    [childrensfutures-trade.pages :as pages :refer [menu-pages pages]]

    ))



(defn- menu-link [[route title icon]]
  [ui/list-item
   {:left-icon (icon)
    :href (pages/path-for route)
    :key route}

   title])

;;;
;;; DRAWER
;;;
(defn drawer-view []
  (let [drawer-open? (subscribe [:ui/drawer-open?])]
    (fn []
      #_[ui/drawer {:open @drawer-open?
                  :docked false
                  :on-request-change #(dispatch [:ui.drawer/toggle-view])}

       [ui/app-bar {:title "myFutures"
                    :show-menu-icon-button false}]
       ;; (js/console.log :info pages)
       (for [menu-page menu-pages]
          (menu-link menu-page))])))
