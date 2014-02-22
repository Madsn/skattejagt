(ns clojure-server.models.db
    (:require [monger.core :as mg]
              [monger.collection :as mc]
              [monger.operators :refer :all]))

;; Tries to get the Mongo URI from the environment variable
;; MONGOHQ_URL, otherwise default it to localhost
(let [uri (get (System/getenv) "MONGOHQ_URL" "mongodb://admin:d3vpa55@troup.mongohq.com:10099/skattejagt")]
  (mg/connect-via-uri! uri))

(defn create-user [user]
  (mc/insert "users" user))

(defn update-user [id first-name last-name email]
  (mc/update "users" {:id id}
             {$set {:first_name first-name
                    :last_name last-name
                    :email email}}))

(defn get-user [id]
  (mc/find-one-as-map "users" {:id id}))
