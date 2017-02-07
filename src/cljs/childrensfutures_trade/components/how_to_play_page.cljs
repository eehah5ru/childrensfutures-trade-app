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
                  "Add the plugin " [:a
                                     {:href "https/metamask.io/"
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
   :step-content [:p "Добавить игровые деньги/пробные эзереумы себе на счет — как это сделать? Описать или найти видос"]))

;;;
;;; third step
;;;
(defn add-dream-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Tell everyone about your dream!"
   :step-content [:section
                  [:p "Press + button at the right corner and you are ready to write down your goal and what you can give a person instead"]
                  [:p "After you are done with writing, you can publish it, paying with игровые деньги"]
                  [:p "Now you are a dreamer! Wait for your investments! Remember that you can now choose one proposition that you like."]]))

;;;
;;; select investment step
;;;
(defn select-investment-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Be an investor!"
   :step-content [:p "And you can also become an investor. By clicking on someone’s dream! So simple as that!"]))

;;;
;;;
;;;
(defn be-careful-with-community-step [step-number]
  (how-to-play-step
   step-number
   :step-button-content "Decentralize everything!"
   :step-content [:section
                  [:p "Remember that every transaction needs to be verified and approved by other users – this infrastructure needs to be supplied by paying a small fee for not having any middle men between users (like banks, for ex.) and for being completely independent and decentralised! "]
                  [:p "New era of transparent tools starts from you!"]]))

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
                         [:h1 "How to play on this exhange market?"]
                         [ui/stepper
                          {:linear false
                           :active-step @step
                           :orientation :vertical
                           :children (r/as-element (map #(%1 %2)
                                           [open-google-chrome-step
                                            get-ethers-step
                                            add-dream-step
                                            select-investment-step
                                            be-careful-with-community-step]
                                           (range)))}]
                         ]))}))
