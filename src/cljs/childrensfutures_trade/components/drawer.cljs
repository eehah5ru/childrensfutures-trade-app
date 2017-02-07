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
   {:class-name (str "menu-link-" (subs (str route) 1))
    :left-icon (icon)
    :href (pages/path-for route)
    :key route}

   title])

;;;
;;; DRAWER
;;;
(defn drawer-view []
  (let [drawer-open? (subscribe [:ui/drawer-open?])
                read-only-app? (subscribe [:app/read-only?])]
    (fn []
      [ui/drawer {:open @drawer-open?
                  :docked false
                  :on-request-change #(dispatch [:ui.drawer/toggle-view])}

       [ui/app-bar {:title "myFutures"
                    :show-menu-icon-button false}]
       ;; (js/console.log :info pages)
       (for [menu-page (menu-pages :read-only-app? read-only-app?)]
          (menu-link menu-page))])))
