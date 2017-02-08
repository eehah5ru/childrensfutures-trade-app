(ns childrensfutures-trade.components.read-only
  (:require
   [cljs-react-material-ui.icons :as icons]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.core :refer [color]]
   [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
   [childrensfutures-trade.styles :as st]
   [medley.core :as medley]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]

   [childrensfutures-trade.pages :refer [path-for]]
   ))



(defn ^:export read-only-notification []
  [outer-paper
   [:h3
    ;; {:style {:color (color :deep-orange-a700)}}
    "Oops! In order to trade you need to open the app in Google Chrome browser on your computer. Otherwise you will have a “read only” version. Please check "
    [:a {:href (path-for :how-to-play)}
     "How to Play"]
    "."]]

   )
