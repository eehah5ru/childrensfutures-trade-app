(ns childrensfutures-trade.pages
  (:require
   [bidi.bidi :as bidi]

    [cljs-react-material-ui.reagent :as ui]
    [cljs-react-material-ui.icons :as icons]

    ;;
    ;; pages
    ;;
    [childrensfutures-trade.components.home-page :refer [home-page]]
    [childrensfutures-trade.components.pulse-page :refer [pulse-page]]
    ;; [childrensfutures-trade.components.how-to-play-page :refer [how-to-play-page]]
    [childrensfutures-trade.components.about-page :refer [about-page]]
    ;; [childrensfutures-trade.components.my-events-page :refer [my-events-page]]
    ;; [childrensfutures-trade.components.latest-events-page :refer [latest-events-page]]

    ))


(def page-defs
  [
   ;; about
   {:key :about
    :route true
    :menu-icon icons/action-info
    :human-readable "about"
    :page-view about-page
    }
   ])

;;;
;;;
;;; bidi/pushy routes
;;;
;;;
(def routes
  ["/" (let [pages (filter #(not= (:route %) true) page-defs)
             default (filter #(= (:route %) true) page-defs)]
         (map #(vector (:route %) (:key %)) (concat pages default)))])

;;;
;;;
;;; pages with components to render
;;;
;;;
(def pages
  (reduce merge
          {}
          (map #(hash-map (:key %) (:page-view %)) page-defs)))

;;;
;;;
;;; drawer menu items
;;;
;;;
(def menu-pages
  (map #(vector (:key %)
              (:human-readable %)
              (:menu-icon %))
       page-defs))

;;;
;;; human readable for page
;;;
(defn human-readable [page-key]
  (->> page-defs
   (filter #(= (:key %) page-key))
   first
   :human-readable))

;;;
;;;
;;; UTILS
;;;
;;;
(def path-for (partial bidi/path-for routes))
