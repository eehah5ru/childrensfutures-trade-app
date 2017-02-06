(ns childrensfutures-trade.core
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.util.request :refer [body-string]]
            ;; [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.logger.onelog :as logger.onelog]
            [clj-logging-config.log4j :refer [set-logger!]]
            [clojure.tools.logging :refer [info]]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server with-channel send! on-close on-receive websocket? close]]
            [cognitect.transit :as transit]
            [clojure.edn :as edn]

            [childrensfutures-trade.utils :as u]

            [childrensfutures-trade.in-memory-storage :as db]
            )
  (:gen-class))

(import [java.io ByteArrayInputStream ByteArrayOutputStream])

(def ^:dynamic *server*)

;; (defn print-body [req]
;;   (let [;;in (ByteArrayInputStream. (.getBytes db))
;;         ;; reader (transit/reader in :json)
;;         parsed (edn/read-string (:db (:params req)))
;;         ]

;;     (println "\n\n")
;;     ;; (println parsed)
;;     (clojure.pprint/pprint parsed)
;;     (println "\n\n")
;;     (clojure.pprint/pprint (keys parsed))
;;     ;; (println (body-string request))
;;     ;; (prn (transit/read reader))
;;     (println "\n\n")

;;     #_(prn request))

;;   {:status 200
;;    :headers {"Content-Type" "text/plain"}
;;    :body "ok"})

;;;
;;; refresh db
;;;
(defn refresh-db [req]
  (let [raw-db (get-in req [:params :db] db/default-db)
        parsed-db (edn/read-string raw-db)
        new-db-version (get parsed-db :db-version -1)]
    ;; (println "\n\n")
    ;; (clojure.pprint/pprint raw-db)
    ;; (println "\n\n")

    (println (str "Trying to refresh db (version " (db/db-version) ") with version " new-db-version))

    (if (db/refresh-db parsed-db)
      (println (str "refreshed. new version " (db/db-version)))
      (println "not refreshed")))
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "ok"})

;;;
;;; fetch db
;;;
(defn fetch-db [req]
  (let [remote-db-version (u/parse-int (get-in req [:params :db-version] "0"))
        need-to-fetch? (> (db/db-version) remote-db-version)
        result (if need-to-fetch?
                 (db/get-db)
                 {})]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (pr-str result)}))

(defroutes routes
  (wrap-multipart-params
   (POST "/refresh-db"
         req
         (refresh-db req)))

  (GET "/fetch-db"
       req
       (fetch-db req))

  ;; (wrap-multipart-params
  ;;  (POST "/print-body"
  ;;        req
  ;;        (print-body req)))

  ;; (GET "/my-ip"
  ;;      request
  ;;      (what-is-my-ip request))

  (GET "/js/*" _
    {:status 404})
  (GET "/*" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))}))

(def http-handler
  (-> routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
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
