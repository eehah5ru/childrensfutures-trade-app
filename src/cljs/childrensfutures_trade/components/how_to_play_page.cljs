(ns childrensfutures-trade.components.how-to-play-page
  (:require
    [cljs-react-material-ui.icons :as icons]
    [cljs-react-material-ui.reagent :as ui]
    [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    [childrensfutures-trade.styles :as st]
    [medley.core :as medley]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]

    [childrensfutures-trade.components.goals :refer [goals-view]]))

(defn step-actions [step]
  (let [first-step? (subscribe [:ui.how-to-play/firsts-step? step])
        last-step? (subscribe [:ui.how-to-play/last-step? step])]
    [:div
     (when-not @last-step?
       [ui/raised-button
        {:primary true
         :disabled @last-step?
         :disable-touch-ripple true
         :disable-focus-ripple true
         :label "next"
         :on-touch-tap #(dispatch [:ui.how-to-play/next-step])
         :style {:margin-right 20}}])
     (when @last-step?
       [ui/raised-button
        {:primary true
         :disabled false
         :label "Tell about your dream!"
         :on-touch-tap #(dispatch [:ui.new-goal/toggle-view])
         :style {:margin-right 20}}])
     (when-not @first-step?
       [ui/flat-button
        {:label "Back"
         :disable-touch-ripple true
         :disable-focus-ripple true
         :on-touch-tap #(dispatch [:ui.how-to-play/previous-step])}])]))

;;;
;;; generic step
;;;
(defn how-to-play-step [step & {:keys [step-button-content step-content]}]
  (with-meta
    [ui/step
     [ui/step-button
      {:on-touch-tap #(dispatch [:ui.how-to-play/set-step step])}
      step-button-content]
     [ui/step-content
      step-content
      [step-actions step]]]
    {:key step}))

;;;
;;; first step
;;;
(defn open-google-chrome-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Open/Install Google Chrome with Metamask"
   :step-content [:p
                  "Open the website in " [:a
                                         {:href "https://www.google.com/chrome/"
                                          :target "blank"}
                                         "Google Chrome"] ". "
                  "Simply add the plugin " [:a
                                     {:href "https://metamask.io/"
                                      :target "blank"}
                                     "MetaMask"]
                  " to Chrome Browser."]))

;;;
;;; second step
;;;
(defn get-ethers-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Create your new wallet"
   :step-content [:p "You’ll need in-game money, that are not real. For that you need to choose Test Net called Ropsten in Metamask."]))

;;;
;;; get gas
;;;
(defn get-gas-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Get “gas fees”"
   :step-content [:div
                  [:p "To put in-game money on your wallet you need to press “BUY” and then choose “GO TO TEST FAUCET” and request 1 ether from faucet. It will be enough to cover all expenses!"]
                  [:p "Now you are able to pay “gas fees” for given operations!"]]))

;;;
;;; third step
;;;
(defn add-dream-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Tell everyone about your dream!"
   :step-content [:section
                  [:p "Press “+“ button at the right bottom corner and you are ready to write down your goal and what you can give a person instead"]
                  [:p "After you are done with writing, you can publish it, paying with игровые деньги"]
                  [:p "Now you are a dreamer! Wait for your investments! Remember that you can now choose one proposition that you like."]]))

;;;
;;; select investment step
;;;
(defn select-investment-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Become an investor!"
   :step-content [:p "You can become an investor by clicking on someone’s dream! So simple as that!"]))

;;;
;;;
;;;
(defn be-careful-with-community-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Decentralise everything!"
   :step-content [:section
                  [:p "Remember that every transaction needs to be verified and approved by other users – this infrastructure needs to be supplied by paying a small fee (in our case, not real, test money) for not having any middle men between users (like banks, for ex.) and for being completely independent and decentralised!"]
                  [:p "New era of transparent tools starts from you!"]
                  [:p "If you have any questions, don’t hesitate to ask: "
                   [:a
                    {:href "mailto:eeefff.org@gmail.com"}
                    "eeefff.org@gmail.com"]]]))

;;;
;;; fourth
;;;

;;;
;;; page view
;;;
(defn ^:export how-to-play-page []
  (r/create-class
   {:component-did-mount #(dispatch [:ui.how-to-play/init])
    :display-name "how-to-play-page"
    :reagent-render (fn []
                      (let [step (subscribe [:ui.how-to-play/step])]
                        [outer-paper
                         [:h1 "How to play on this exchange market?"]
                         [:p "Since my futures.trade is based on blockchain technology Ethereum, you will need a special tool to be able to interact with it."]
                         [ui/stepper
                          {:style {:padding-bottom "60px"}
                           :linear false
                           :active-step @step
                           :orientation :vertical
                           :children (r/as-element (map #(%1 %2)
                                           [open-google-chrome-step
                                            get-ethers-step
                                            get-gas-step
                                            add-dream-step
                                            select-investment-step
                                            be-careful-with-community-step]
                                           (range)))}]
                         ]))}))
