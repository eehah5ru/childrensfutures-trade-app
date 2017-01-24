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
   [clavatar-js.core :as clavatar]))


(defn chat-view []
  (let [channel-id (subscribe [:chat/channel-id])
        chat-open? (subscribe [:ui/chat-open?])]
    [ui/paper {:id "tlkio"
               :data-channel @channel-id

               :data-theme "theme--pop"
               :style {:width "400px"
                       :height "400px"
                       :position "fixed"
                       :bottom 0
                       :right "30px"
                       :z-index 999
                       :display (if @chat-open? "block" "none")}}
     [:script
      {
       :type "text/javascript"
       :src "http://tlk.io/embed.js"}]]))
