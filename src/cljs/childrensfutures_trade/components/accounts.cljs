(ns childrensfutures-trade.components.accounts
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.icons :as icons]
    [childrensfutures-trade.utils :as u]
    [childrensfutures-trade.subs :as s]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.address-select-field :refer [address-select-field]]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    ))

(defn- switch-account-view []
  ;; IMPORTANT!!!! let outside fn!!!
  (let [my-addresses (subscribe [:db/my-addresses])
        current-address (subscribe [:db/current-address])
        balance (subscribe [:db/selected-address-balance])]
    (fn []
      [row
       [col {:xs 12 :sm 12 :md 10 :lg 6 :md-offset 1 :lg-offset 3}
        [:h1 "Change Ethereum address"]
        [address-select-field
         @my-addresses
         @current-address
         [[:new-goal/update :owner] [:current-address/update] [:accounts/toggle-view]]]

        [:br]
        [:h3 "Balance: " (u/eth @balance)]]])))


(defn switch-account-dialog []
  (let [show-accounts? (subscribe [:db/show-accounts?])
        cancel-button [ui/flat-button
                       {:secondary true
                        :disabled false
                        :label "cancel"
                        :on-touch-tap #(dispatch [:accounts/toggle-view])}]]
    (fn []
      [ui/dialog
       {:modal false
        :actions [(r/as-element cancel-button)]
        :children (r/as-element [switch-account-view])
        :open @show-accounts?}])))
