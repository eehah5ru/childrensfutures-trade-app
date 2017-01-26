(ns childrensfutures-trade.components.chat
  (:require
    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.core :refer [color]]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.utils :as u]
    [medley.core :as medley]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    ))


(def default-chat-open-button-title "Discuss details")

(defn chat-open-button
  ([]
   (chat-open-button default-chat-open-button-title))

  ([title]

   [ui/raised-button
   {:secondary true
    :label title}]))
