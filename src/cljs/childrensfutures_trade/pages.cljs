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


;;;
;;;
;;; bidi/pushy routes
;;;
;;;
(def routes
  ["/" {"about" :about
        "latest" :latest
        "my" :my
        "pulse" :pulse
        ;; "contact" :contact
        ;; "sponsor" :sponsor
        ;; "how-to-play" :how-to-play
        ;; ["players/" :address] :player-profile
        true :home}])

;;;
;;;
;;; pages with components to render
;;;
;;;
(def pages
  {:home home-page
   :about about-page
   :pulse pulse-page
   ;; :latest latest-events-page
   ;; :my my-events-page
   ;; :how-to-play how-to-play-page
   })

;;;
;;;
;;; drawer menu items
;;;
;;;
(def menu-pages
  [[:home "Home" icons/action-home]
   [:pulse "Pulse" icons/action-stars]
   [:how-to-play "How To Play" icons/action-help]
   [:about "About" icons/action-info]])

;;;
;;;
;;; UTILS
;;;
;;;
(def path-for (partial bidi/path-for routes))
