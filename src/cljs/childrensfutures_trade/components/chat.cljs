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

    [childrensfutures-trade.components.avatars :as avatars]
    ))


;;;
;;; chat app-bar
;;;
(defn- chat-app-bar []
  [ui/app-bar {:title "Chat"
               :show-menu-icon-button false
               :style {:position "fixed"
                       :top 0}}])

;;;
;;; messages list
;;;
(defn- chat-messages []
  (let [messages (subscribe [:db.chat.current/messages])]
    [:div
     {:id "chat-messages-container"
      :style {:height "calc(100% - 100px)"
              :max-height "100%"
              :overflow "scroll"
              :margin 0}}
     [ui/list
      (for [msg @messages]
        (let [{:keys [message-id
                      owner
                      text]} msg]
          ^{:key message-id}
          [ui/list-item
           {:inset-children true
            :secondary-text (r/as-element text)
            :secondary-text-lines 2
            :left-avatar (r/as-element
                          [avatars/avatar
                           owner
                           :avatar-style {:position "absolute"
                                          :display "inline-block"
                                          :top "50%"
                                          :left "15px"
                                          :transform "translateY(-50%)"}])}])
        )]]))

;;;
;;; new message text-field
;;;
(defn- new-message-text-field []
  (let [new-chat-message (subscribe [:db.chat/new-chat-message])
        {:keys [trx-on-air? text]} @new-chat-message]
    [ui/text-field
     {:full-width true
      :multi-line true
      :value text
      :disabled trx-on-air?
      :rows 2
      :rows-max 2
      :floating-label-text "your message"
      :on-change #(dispatch [:new-chat-message.attribute/update :text (u/evt-val %)])
      :style {:padding-left "20px"
              :padding-right "20px"
              :width "80%"}}]))

;;;
;;; send message button
;;;
(defn- send-message-button []
  [ui/floating-action-button
   {:on-touch-tap #(dispatch [:blockchain.new-chat-message/send])
    :children (icons/content-send)
    :z-depth 1
    :mini true
    :style {:position "fixed"
            :right 10
            :bottom 80
            :z-index 99}}]
  )

;;;
;;; DRAWER
;;;
(defn chat-drawer-view []
  (let [drawer-open? (subscribe [:ui.chat/drawer-open?])]
    (fn []
      [ui/drawer {:open @drawer-open?
                  :open-secondary true
                  :docked false
                  :width 320
                  :on-request-change #(dispatch [:ui.chat/toggle-view])}
       [chat-app-bar]

       [chat-messages]

       [ui/divider]

       [new-message-text-field]

       ;;
       ;; send button
       ;;
       [send-message-button]

       ])))
