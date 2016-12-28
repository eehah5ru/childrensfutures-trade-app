(ns play-with-reagent)
;;;
;;; some utils
;;;
(comment
 (defn mount-to-root [view]
   (reagent.core/render view (.getElementById js/document "app")))

;;; play with atoms and views
 (do
   ;; (def expanded (atom true))

   (def expanded (reagent.core/atom true))

   (defn on-header-click []
     (.log js/console @expanded)
     (swap! expanded not))

   (defn expandable-view []
     [:div.expandable
      [:div.header {:on-click on-header-click}
       "Click me to expand and collapse the body"]
      (if @expanded
        [:div.body "I am the body"])])
   (mount-to-root [expandable-view]))

;;;
;;; a little bit refactored previous code
;;;
 (do
   (let [expanded (reagent.core/atom true)]
     (letfn [
             ;; events
             (on-header-click []
               (swap! expanded not))
             ;; view
             (expandable-view-2 []
               [:div.expandable
                [:div.header {:on-click on-header-click}
                 "Click me to expand the body"
                 (if @expanded
                   [:div.body "I am the body"])]])]
       (mount-to-root [expandable-view-2]))))

;;;
;;; play with state atoms
;;;
 (do
   (def comments-storage (reagent.core/atom []))

   (defn add-comment [name text]
     (swap! comments-storage #(conj % {:name name, :text text})))

   (defn delete-all-comments []
     (swap! comments-storage (fn [] [])))

   (defn comment-view [comment]
     [:div.comment
      [:div.author (:name comment)]
      [:div.text (:text comment)]])

   (defn comments-list [comments]
     [:div.comments-list
      (for [comment @comments]
        (comment-view comment))])

   (mount-to-root [comments-list comments-storage]))

;;;
;;; play with input
;;;
 (do
   ;; (ns atom-input
   ;;   (:require [reagent.core :as r]))

   (defn atom-input [value]
     [:input {:type "text"
              :value @value
              :on-change #(reset! value (-> % .-target .-value))}])

   (defn simple-input-view []
     (let [value (reagent.core/atom "blla")]
       (fn []
         [:div.simple-input
          [:p "value is now: " @value]
          [:p "change it here: " [atom-input value]]])))

   (mount-to-root [simple-input-view]))


;;;
;;; simple clock
;;;
 (do
   (defonce clock (reagent.core/atom (js/Date.)))

   (defonce clock-color (reagent.core/atom "#eeefff"))

   (defn greeting-view [msg]
     [:h1 msg])

   (defn clock-view [clock clock-color]
     (let [time-str (-> @clock .toTimeString (clojure.string/split " ") first)]
       [:div.clock
        {:style {:color @clock-color}}
        time-str]))

   (defn change-clock-color-view [clock-color]
     [:div.color-input
      "Clock color: "
      [:input {:type "text"
               :value @clock-color
               :on-change #(reset! clock-color (-> % .-target .-value))}]])

   (defn simple-clock-view []
     (js/setInterval #(reset! clock (js/Date.)) 1000)
     (fn []
       [:div
        [greeting-view "hi there! it's clock here"]
        [clock-view clock clock-color]
        [change-clock-color-view clock-color]])

   (mount-to-root [simple-clock-view]))))
