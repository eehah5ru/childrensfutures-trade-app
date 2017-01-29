(ns childrensfutures-trade.components.about-page
  (:require
    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    [childrensfutures-trade.styles :as st]
    [childrensfutures-trade.utils :as u]
    [medley.core :as medley]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]))

(defn about-page []
  [outer-paper
   [:h1 "What kind of exchange market is it?"]
   [:p "As an art group EEEFFF we are developing a web application
myfutures.trade that allows you to make transactions by putting up the
goals and dreams of the future for trading. This is a decentralized
speculative financial market to trade futures – investments in events
that have not yet occurred. The app is based on blockchain technology
called Ethereum."]
   [:p "During the residency we would like to make a
series of workshops for teenagers and youth (aged 14-25), where they
will be invited to create a closed community where everyone is
thinking about the future right now.  Exposing the goals for trading,
participants become part of a community. They can sell and exchange a
short-term goals (for example, to gather oneself up for 10 times), and
long-term (to enter the university) to invest in their future now. In
return they offer the potential investor is something that may
interest him (for example, the secret of mutual friends). Investors
are being paid only when the goal is achieved.  Using a financial
instrument as a medium, we would like to involve a person in the
experience of the debt situation, which is taken right now as a given
and is an instrument for the construction of our culture as a
whole."]
   [:p "The project opens up a space for discussion: who owns our
future? How many debts are we able to cover? Do we need to invent new
tools for dealing with the existing economy? What horizonts are
decentralised technological platforms are opening for us?"]])
