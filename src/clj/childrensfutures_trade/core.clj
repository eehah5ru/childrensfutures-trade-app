(ns childrensfutures-trade.core
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            ;; [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.logger.onelog :as logger.onelog]
            [clj-logging-config.log4j :refer [set-logger!]]
            [clojure.tools.logging :refer [info]]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(def ^:dynamic *server*)

;;;
;;; play with ring handlers
;;;
(defn what-is-my-ip [request]
  (println "what-is-my-ip")
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (:remote-addr request)})

(defroutes routes
  (GET "/my-ip"
       request
       (what-is-my-ip request))
  (GET "/js/*" _
    {:status 404})
  (GET "/*" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))}))

(def http-handler
  (-> routes
      (wrap-defaults site-defaults)
      logger.onelog/wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (set-logger!)
  (let [port (Integer. (or port (env :port) 6655))]
    (alter-var-root (var *server*)
                    (constantly (run-server http-handler {:port port :join? false})))))

(defn stop-server []
  (*server*)
  (alter-var-root (var *server*) (constantly nil)))

(defn restart-server []
  (stop-server)
  (-main))

(comment
  (restart-server)
  (-main)
  (stop-server))
