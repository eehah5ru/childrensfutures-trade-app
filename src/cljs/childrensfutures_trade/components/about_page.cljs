(ns childrensfutures-trade.components.about-page
  (:require
    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.utils :as u]
    [medley.core :as medley]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [goog.string :as gstring]
    ))


(defn about-page []
  [outer-paper
   [:h1 "What kind of exchange market is it?"]
   [:p "The web application "
    [:a {:href "http://myfutures.trade"} "myfutures.trade"]
    (gstring/unescapeEntities " allows you to make transactions by trading future goals and dreams. This is a decentralized, speculative financial stock exchange that is based on a blockchain-technology called ethereum. It is created to play with futures &mdash; investing into the events that have not yet occurred.")]
   [:p "Participants become a part of an anonymous society by trading their own goals. They can sell and exchange both short-term (for instance, 10 pull ups) and long-term goals (such as to enrol in a university) for the investments into their future right now. In exchange, they suggest a potential investor something that might interest them (for example, a secret about their friends). Investors receive the compensation only when the goal is achieved."]
   [:p "Use this financial instrument as the medium to experience new forms of the decentralized financial organisation. Its aim is to cripple the world market!"]
   [:p "The app is inspired by researching the processes that take place inside of the DarkNet and the blockchain community."]
   [:p "Enjoy!"]])
