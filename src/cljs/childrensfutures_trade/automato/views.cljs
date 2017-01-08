(ns childrensfutures-trade.automato.views
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.core :refer [get-mui-theme color]]
    [cljs-react-material-ui.icons :as icons]
    [childrensfutures-trade.automato.subs :as s]
    ;; [childrensfutures-trade.styles :as st]
    ;; [childrensfutures-trade.components.home-page :refer [home-page]]
    ;; [childrensfutures-trade.components.how-to-play-page :refer [how-to-play-page]]
    ;; [childrensfutures-trade.components.about-page :refer [about-page]]
    ;; [childrensfutures-trade.components.layout :refer [grid row col outer-paper]]
    ))

(defn pic-slide [slide]
  [:h1
   "This is pic slide"])

(defn video-slide [slide]
  [:h1
   "There will be a video"])

(defn color-slide [slide]
  [:h1
   "Yeah! Color!"])

(defn black-slide []
  [:h1
   "Black slide"])

(defn slides-view []
  (let [current-slide (subscribe [:ui/current-slide])]
    (fn []
      {:fluid true}
      [:div.slides
       [:div.slide
        (condp = (:type @current-slide)
          :black [black-slide]

          :pic [pic-slide @current-slide]

          :video [video-slide @current-slide]

          ;; unknown type
          [black-slide])]])))
