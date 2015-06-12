(ns de.zalf.berest.client.hoplon.state
  (:require-macros
    [tailrecursion.javelin :refer [defc defc= cell=]])
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [tailrecursion.javelin :as j :refer [cell]]
            [tailrecursion.castra  :as c #_:refer #_[mkremote]]
            [tailrecursion.hoplon :as h]))

(defn jq-cred-ajax [async? url data headers done fail always]
  (.. js/jQuery
      (ajax (clj->js {"async"       async?
                      "contentType" "application/json"
                      "data"        data
                      "dataType"    "text"
                      "headers"     headers
                      "processData" false
                      "xhrFields"   {"withCredentials" true}
                      "type"        "POST"
                      "url"         url}))
      (done (fn [_ _ x] (done (aget x "responseText"))))
      (fail (fn [x _ _] (fail (aget x "responseText"))))
      (always (fn [_ _] (always)))))

(def server-url (condp = (-> js/window .-location .-hostname)
                  "" "http://localhost:3000/"
                  "localhost" "http://localhost:3000/"
                  "http://irrigama-web.elasticbeanstalk.com/"))
#_(println "server-url: " server-url)

(defn mkremote [& args]
  (apply c/mkremote (flatten [args server-url jq-cred-ajax]))
  #_(apply c/mkremote (flatten [args "http://localhost:3000/" jq-cred-ajax]))
  #_(apply c/mkremote (flatten [args "http://irrigama-web.elasticbeanstalk.com/" jq-cred-ajax])))

(enable-console-print!)

;stem cell
(defc state {})
#_(cell= (println "state: \n" (pr-str state)))

(defc pwd-update-success? nil)
(defc climate-data-import-time-update-success? nil)
(defc climate-data-import-success? nil)

;cell holding static app state, which will hardly change
(defc static-state nil)
#_(cell= (println "static-state:\n " (pr-str static-state)))

(defc= slopes (:slopes static-state))
(defc= stts (:stts static-state))
(defc= substrate-groups (into {} (map (juxt :soil.substrate/key identity) (:substrate-groups static-state))))
(defc= ka5-soil-types (into {} (map (juxt :soil.type.ka5/name identity) (:ka5-soil-types static-state))))
(defc= crop->dcs (:crop->dcs static-state))
#_(defc= stt-descriptions (into {} (map (juxt :soil.stt/key :soil.stt/description) stts)))
(defc= minimal-all-crops (:minimal-all-crops static-state))
(defc= all-weather-stations (:all-weather-stations static-state))

;local state
(defc weather-station-data {})
#_(cell= (println "weather-station-data: " (pr-str weather-station-data)))

(defc crop-state nil)
#_(cell= (when crop-state (println "crop-state: " (pr-str crop-state))))

(defc= processed-crop-data (:processed crop-state))
(defc= raw-crop-data (:raw crop-state))
#_(defc= user-crop? (= (:crop-type crop-state) :user))

;derived state

(defc= farms (:farms state))

(defc= users (:users state))

(defc= user-weather-stations (:weather-stations state))
#_(cell= (println "user-weather-stations: " (pr-str user-weather-stations)))

(defc= technology-cycle-days (-> state :technology :technology/cycle-days))
(defn set-technology-cycle-days
  [value]
  (swap! state update-in [:technology :technology/cycle-days] value))

(defc= technology-outlet-height (-> state :technology :technology/outlet-height))
(defn set-technology-cycle-days
  [value]
  (swap! state update-in [:technology :technology/cycle-days] value))


(defc routeHash (.. js/window -location -hash))
#_(cell= (println "routeHash: " (pr-str routeHash)))
(def full-route (h/route-cell routeHash #(reset! routeHash %)))
(defc= route+params (str/split full-route #"\?|\&|="))
(defc= route (first route+params))
#_(cell= (println "route+params: " (pr-str route+params)))
(defc= route-params (into {} (for [[k v] (partition 2 (rest route+params))]
                               [(keyword k) v])))
#_(cell= (println "route-params: " (pr-str route-params)))

(defc= route-params-str
       (->> route-params
            (map (fn [[k v]] (when v (str (name k) "=" v))) ,,,)
            (remove nil? ,,,)
            (str/join "&" ,,,)))
#_(cell= (println "route-params-str: " (pr-str route-params-str)))

(defn clear-route+params
  []
  (reset! routeHash ""))

(defn set-route+params
  [& [route* & params]]
  (->> (merge @route-params (into {} (for [[k v] (partition 2 params)] [k v])))
       (map (fn [[k v]] (when v (str (name k) "=" v))) ,,,)
       (remove nil? ,,,)
       (str/join "&" ,,,)
       (str (or route* @route) "?" ,,,)
       (reset! routeHash ,,,)))

(defn clear-route-params
  []
  #_(println "route: " (pr-str @route) " route-params: " (pr-str @route-params)
           " map: " (pr-str (map (fn [[k _]] [k nil]) @route-params))
           " flatten: " (pr-str (flatten (map (fn [[k _]] [k nil]) @route-params))))
  (apply set-route+params @route (flatten (map (fn [[k _]] [k nil]) @route-params))))

(defn set-route-params
  [& params]
  (apply set-route+params nil params))

(defn set-route
  [route*]
  (set-route+params route*))

(defc error nil)
(defc loading [])

(def clear-error!   #(reset! error nil))

(defc csv-result nil)
#_(cell= (println "csv-result: " (pr-str csv-result)))
(defc calc-error nil)
(defc calculating [])

(defc= user (:user-credentials state))
#_(cell= (println "user-creds: " (pr-str user)))

(defc= lang (:language state))
#_(cell= (println "lang: " (pr-str lang)))

(defc= loaded?      (not= {} state))
(defc= loading?     (seq loading))

(defc= logged-in?   (not (nil? user)))
#_(cell= (println "logged-in?: "(pr-str logged-in?)))


(defn has-role
  [user role]
  (let [r (if (namespace role)
            role
            (keyword "user.role" (name role)))]
    (and (not (nil? user))
         ((:user/roles user) r))))

(defn has-user-role
  [role]
  (has-role @user role))

(defc= admin-logged-in? (has-role user :admin))
(defc= consultant-logged-in? (has-role user :consultant))

(defc= show-login?  (and #_loaded? (not logged-in?)))
#_(cell= (println "show-login?: " show-login?))

(defc= show-content?  (and loaded? logged-in?))


(def clear-error!   #(reset! error nil))



(def login! (mkremote 'de.zalf.berest.web.castra.api/login state error loading))
(def logout! (mkremote 'de.zalf.berest.web.castra.api/logout state error loading))
(def get-state (mkremote 'de.zalf.berest.web.castra.api/get-berest-state state error loading))
#_(def get-full-selected-crops (mkremote 'de.zalf.berest.web.castra.api/get-state-with-full-selected-crops state error loading))
(def calculate-csv (mkremote 'de.zalf.berest.web.castra.api/calculate-csv csv-result calc-error calculating))
(def simulate-csv (mkremote 'de.zalf.berest.web.castra.api/simulate-csv csv-result calc-error calculating))

(defn calculate-from-db
  [result-cell plot-id until-abs-day year]
  ((mkremote 'de.zalf.berest.web.castra.api/calculate-from-db result-cell error loading) plot-id until-abs-day year))

(def load-static-state
  (mkremote 'de.zalf.berest.web.castra.api/get-static-state static-state error loading))
(cell= (when logged-in? #_(and s/logged-in? (not s/static-state))
         (load-static-state)))

;plot
(def create-new-plot (mkremote 'de.zalf.berest.web.castra.api/create-new-plot state error loading))
(def create-new-plot-annual (mkremote 'de.zalf.berest.web.castra.api/create-new-plot-annual state error loading))

;user
(def create-new-user (mkremote 'de.zalf.berest.web.castra.api/create-new-user state error loading))
(def set-new-password (mkremote 'de.zalf.berest.web.castra.api/set-new-password pwd-update-success? error loading))
(def update-user-roles (mkremote 'de.zalf.berest.web.castra.api/update-user-roles state error loading))

;weather-data
(def load-weather-station-data (mkremote 'de.zalf.berest.web.castra.api/get-weather-station-data weather-station-data error loading))
(def create-new-local-user-weather-station (mkremote 'de.zalf.berest.web.castra.api/create-new-local-user-weather-station state error loading))
(def add-user-weather-stations (mkremote 'de.zalf.berest.web.castra.api/add-user-weather-stations state error loading))
(def remove-user-weather-stations (mkremote 'de.zalf.berest.web.castra.api/remove-user-weather-stations state error loading))
(def import-weather-data (mkremote 'de.zalf.berest.web.castra.api/import-weather-data state error loading))
(def create-new-weather-data (mkremote 'de.zalf.berest.web.castra.api/create-new-weather-data state error loading))

;farms
(def create-new-farm (mkremote 'de.zalf.berest.web.castra.api/create-new-farm state error loading))
(def create-new-farm-address (mkremote 'de.zalf.berest.web.castra.api/create-new-farm-address state error loading))
(def create-new-farm-contact (mkremote 'de.zalf.berest.web.castra.api/create-new-farm-contact state error loading))

;soils
(def create-new-soil-data-layer (mkremote 'de.zalf.berest.web.castra.api/create-new-soil-data-layer state error loading))
(def set-substrate-group-fcs-and-pwps (mkremote 'de.zalf.berest.web.castra.api/set-substrate-group-fcs-and-pwps state error loading))
(def create-new-soil-moisture (mkremote 'de.zalf.berest.web.castra.api/create-new-soil-moisture state error loading))
(def create-new-donation (mkremote 'de.zalf.berest.web.castra.api/create-new-donation state error loading))
(def create-new-crop-instance (mkremote 'de.zalf.berest.web.castra.api/create-new-crop-instance state error loading))
(def create-new-dc-assertion (mkremote 'de.zalf.berest.web.castra.api/create-new-dc-assertion state error loading))

;crops
(def create-new-crop (mkremote 'de.zalf.berest.web.castra.api/create-new-crop static-state error loading))
(def copy-crop (mkremote 'de.zalf.berest.web.castra.api/copy-crop static-state error loading))
(def delete-crop (mkremote 'de.zalf.berest.web.castra.api/delete-crop static-state error loading))
(def load-crop-data (mkremote 'de.zalf.berest.web.castra.api/get-crop-data crop-state error loading))
(def update-crop-db-entity (mkremote 'de.zalf.berest.web.castra.api/update-crop-db-entity crop-state error loading))
(def delete-crop-db-entity (mkremote 'de.zalf.berest.web.castra.api/delete-crop-db-entity crop-state error loading))
(def create-new-crop-kv-pair (mkremote 'de.zalf.berest.web.castra.api/create-new-crop-kv-pair crop-state error loading))

;common
(def update-db-entity (mkremote 'de.zalf.berest.web.castra.api/update-db-entity state error loading))
(def retract-db-value (mkremote 'de.zalf.berest.web.castra.api/retract-db-value state error loading))
(def delete-db-entity (mkremote 'de.zalf.berest.web.castra.api/delete-db-entity state error loading))
(def delete-db-entities (mkremote 'de.zalf.berest.web.castra.api/delete-db-entity state error loading))

(def create-new-com-con (mkremote 'de.zalf.berest.web.castra.api/create-new-com-con state error loading))

;admin
(def set-climate-data-import-time
  (mkremote 'de.zalf.berest.web.castra.api/set-climate-data-import-time
            climate-data-import-time-update-success?
            error loading))

(def bulk-import-dwd-data-into-datomic
  (mkremote 'de.zalf.berest.web.castra.api/bulk-import-dwd-data-into-datomic
            climate-data-import-success?
            error loading))








