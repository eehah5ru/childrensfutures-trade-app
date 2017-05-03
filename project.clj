(use '[clojure.java.shell :only [sh]])
;;;
;;;
;;; utils
;;;
;;;

;;;
;;; get git revision
;;;
(defn get-revision []
  (-> (sh "git" "log" "--pretty=format:'%H'" "-1")
      :out
      (.replaceAll "'" "")))

;;;
;;;
;;; project's config
;;;
;;;
(let [revision (get-revision)]
  (defproject childrensfutures-trade :lein-v
    :dependencies [[bidi "2.0.10"]
                   [bk/ring-gzip "0.1.1"]
                   [cljs-ajax "0.5.8"]
                   [cljs-react-material-ui "0.2.37"]
                   [cljs-web3 "0.18.2-0"]
                   [cljsjs/bignumber "2.1.4-1"]
                   [cljsjs/react-flexbox-grid "0.10.2-1" :exclusions [cljsjs/react cljsjs/react-dom]]
                   [com.andrewmcveigh/cljs-time "0.4.0"]
                   [compojure "1.6.0-beta1"]
                   [day8.re-frame/http-fx "0.0.4"]
                   [environ "1.0.3"]
                   [http-kit "2.2.0"]
                   [madvas.re-frame/google-analytics-fx "0.1.0"]
                   [madvas.re-frame/web3-fx "0.1.4"]
                   [medley "0.8.3"]
                   [kibu/pushy "0.3.6"]
                   [org.clojure/clojure "1.8.0"]
                   [org.clojure/clojurescript "1.9.229"]
                   [print-foo-cljs "2.0.3"]
                   [re-frame "0.8.0" :exclusions [cljsjs/react cljsjs/react-dom]]
                   [reagent "0.6.0" :exclusions [cljsjs/react org.clojure/tools.reader cljsjs/react-dom]]
                   ;; [ring.middleware.logger "0.5.0"]
                   [ring-logger-onelog "0.7.6"]
                   [clj-logging-config "1.9.12"]
                   [ring/ring-core "1.6.0-beta5"]
                   [ring/ring-defaults "0.3.0-beta1"]
                   [ring/ring-devel "1.6.0-beta5"]
                   [net.colourcoding/cljs-clavatar "0.2.1"]
                   [clavatar-js "0.1.0-SNAPSHOT"]
                   [cljsjs/clipboard "1.5.13-1"]
                   [com.cemerick/url "0.1.1"]
                   [com.cognitect/transit-cljs "0.8.239"]
                   [com.cognitect/transit-clj "0.8.297"]

                   ;; redis
                   [com.taoensso/carmine "2.15.1"]]


    :plugins [[lein-auto "0.1.2"]
              [lein-cljsbuild "1.1.4"]
              [lein-shell "0.5.0"]
              [deraen/lein-less4j "0.5.0"]
              [lein-file-replace "0.1.0"]
              [com.roomkey/lein-v "6.1.0"]
              [filegen-ng "0.1.0-SNAPSHOT"]]

    :min-lein-version "2.5.3"
    :main childrensfutures-trade.core

    :source-paths ["src/clj"]

    :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

    :figwheel {:css-dirs ["resources/public/css"]
               :server-port 6777
               :ring-handler user/http-handler
               :server-logfile "log/figwheel.log"}

    :auto {"compile-solidity" {:file-pattern #"\.(sol)$"
                               :paths ["resources/public/contracts/src"]}}

    :aliases {"compile-solidity" ["shell" "./compile-solidity.sh"]}

    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

    :less {:source-paths ["resources/public/less"]
           :target-path "resources/public/css"
           :target-dir "resources/public/css"
           :source-map true
           :compression true}

    :uberjar-name "childrensfutures-trade.jar"

    :prep-tasks [["v" "cache" "src"]]
    :release-tasks [["vcs" "assert-committed"]
                    ["v" "update"] ;; compute new version & tag it
                    ["vcs" "push"]]


    ;;
    ;;
    ;; build profiles
    ;;
    ;;
    :profiles
    {
     ;;
     ;; local development with figwheel
     ;;
     :dev
     {:dependencies [[binaryage/devtools "0.8.2"]
                     [com.cemerick/piggieback "0.2.1"]
                     [figwheel-sidecar "0.5.8"]
                     [org.clojure/tools.nrepl "0.2.11"]]

      :plugins [[lein-figwheel "0.5.8"]]
      :source-paths ["env/dev"]
      ;;
      ;; generate index.html
      ;;
      :filegen-ng [{:data {:tpl ~(slurp "resources/public/index_tpl.html")
                        :version ~(fn [p] nil)}
                 :template-fn ~(fn [p d] (:lein-v p))
                 :target "resources/public/index.html"}]
      ;;
      ;; build cljs
      ;;
      :cljsbuild {:builds [{:id "dev"
                            :source-paths ["src/cljs"]
                            :figwheel {:on-jsload "childrensfutures-trade.core/mount-root"}
                            :compiler {:main childrensfutures-trade.core
                                       :output-to "resources/public/js/compiled/app.js"
                                       :output-dir "resources/public/js/compiled/out"
                                       :asset-path "/js/compiled/out"
                                       :source-map-timestamp true
                                       :optimizations :none
                                       :closure-defines {childrensfutures-trade.utils.DEV true
                                                         goog.DEBUG false}
                                       :preloads [print.foo.preloads.devtools]}}]}}

     ;;
     ;; production
     ;;
     :uberjar {:hooks [leiningen.cljsbuild]
               :prep-tasks ["file-replace" ]
               :omit-source true
               :aot :all
               :main childrensfutures-trade.core
               :cljsbuild {:builds {:app {:id "uberjar"
                                          :source-paths ["src/cljs"]
                                          :compiler {:main childrensfutures-trade.core
                                                     :output-to ~(str "resources/public/js/compiled/app_" revision ".js")
                                                     :optimizations :advanced
                                                     :closure-defines {childrensfutures-trade.utils.DEV false
                                                                       goog.DEBUG false}
                                                     :pretty-print true
                                                     :pseudo-names true}}}}}}))
