(ns de.zalf.berest.client.hoplon.state
  (:require-macros
    [tailrecursion.javelin :refer [defc defc= cell=]])
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [tailrecursion.javelin :as j :refer [cell]]
            [tailrecursion.castra  :as c :refer [mkremote]]
            [tailrecursion.hoplon :as h]))

(enable-console-print!)

;stem cell
(defc state {})
#_(cell= (println "state: \n" (pr-str state)))

(defc pwd-update-success? nil)

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
(def full-route (h/route-cell routeHash #(reset! routeHash %)))
(defc= route+params (str/split full-route #"\?|\&|="))
(defc= route (first route+params))
#_(cell= (println "route+params: " (pr-str route+params)))
(defc= route-params (into {} (for [[k v] (partition 2 (rest route+params))]
                               [(keyword k) v])))
#_(cell= (println "route-params: " (pr-str route-params)))
(defn set-route-params
  [& params]
  (->> (merge @route-params (into {} (for [[k v] (partition 2 params)] [k v])))
       (map (fn [[k v]] (str (name k) "=" v)) ,,,)
       (str/join "&" ,,,)
       (str @route "?" ,,,)
       (reset! routeHash ,,,)))

(defc error nil)
(defc loading [])

(def clear-error!   #(reset! error nil))

(defc csv-result nil)
(cell= (println "csv-result: " (pr-str csv-result)))
(defc calc-error nil)
(defc calculating [])

(defc= user (:user-credentials state))

(defc= lang (:language state))
#_(cell= (println "lang: " (pr-str lang)))

(defc= loaded?      (not= {} state))
(defc= loading?     (seq loading))

(defc= logged-in?   (not (nil? user)))
#_(cell= (println "logged-in?: "(pr-str logged-in?)))

(defc= admin-logged-in? (and (not (nil? user))
                             ((:user/roles user) :user.role/admin)))

(defc= show-login?  (and #_loaded? (not logged-in?)))
#_(cell= (println "show-login?: " show-login?))

(defc= show-content?  (and loaded? logged-in?))


(def clear-error!   #(reset! error nil))

(def login! (mkremote 'de.zalf.berest.web.castra.api/login state error loading))
(def logout! (mkremote 'de.zalf.berest.web.castra.api/logout state error loading))
(def get-state (mkremote 'de.zalf.berest.web.castra.api/get-berest-state state error loading))
(def get-full-selected-crops (mkremote 'de.zalf.berest.web.castra.api/get-state-with-full-selected-crops state error loading))
(def calculate-csv (mkremote 'de.zalf.berest.web.castra.api/calculate-csv csv-result calc-error calculating))
(def simulate-csv (mkremote 'de.zalf.berest.web.castra.api/simulate-csv csv-result calc-error calculating))

(defn calculate-from-db
  [result-cell plot-id until-abs-day year]
  ((mkremote 'de.zalf.berest.web.castra.api/calculate-from-db result-cell error loading) plot-id until-abs-day year))

(defn load-weather-station-data
  [result-cell weather-station-id years]
  ((mkremote 'de.zalf.berest.web.castra.api/get-weather-station-data
             result-cell error loading) weather-station-id years))

#_(def load-minimal-all-crops
  (mkremote 'de.zalf.berest.web.castra.api/get-minimal-all-crops minimal-all-crops error loading))

(def load-static-state
  (mkremote 'de.zalf.berest.web.castra.api/get-static-state static-state error loading))

(defn load-crop-data
  [result-cell crop-id]
  ((mkremote 'de.zalf.berest.web.castra.api/get-crop-data
             result-cell error loading) crop-id))

(def create-new-farm (mkremote 'de.zalf.berest.web.castra.api/create-new-farm state error loading))
(def create-new-plot (mkremote 'de.zalf.berest.web.castra.api/create-new-plot state error loading))
(def create-new-plot-annual (mkremote 'de.zalf.berest.web.castra.api/create-new-plot-annual state error loading))

(def create-new-user (mkremote 'de.zalf.berest.web.castra.api/create-new-user state error loading))
(def set-new-password (mkremote 'de.zalf.berest.web.castra.api/set-new-password pwd-update-success? error loading))
(def update-user-roles (mkremote 'de.zalf.berest.web.castra.api/update-user-roles state error loading))
(def add-user-weather-stations (mkremote 'de.zalf.berest.web.castra.api/add-user-weather-stations state error loading))
(def remove-user-weather-stations (mkremote 'de.zalf.berest.web.castra.api/remove-user-weather-stations state error loading))

(def create-new-farm-address (mkremote 'de.zalf.berest.web.castra.api/create-new-farm-address state error loading))

(def create-new-soil-data-layer (mkremote 'de.zalf.berest.web.castra.api/create-new-soil-data-layer state error loading))
(def set-substrate-group-fcs-and-pwps (mkremote 'de.zalf.berest.web.castra.api/set-substrate-group-fcs-and-pwps state error loading))

(def create-new-donation (mkremote 'de.zalf.berest.web.castra.api/create-new-donation state error loading))
(def create-new-crop-instance (mkremote 'de.zalf.berest.web.castra.api/create-new-crop-instance state error loading))
(def create-new-dc-assertion (mkremote 'de.zalf.berest.web.castra.api/create-new-dc-assertion state error loading))
(def create-new-weather-data (mkremote 'de.zalf.berest.web.castra.api/create-new-weather-data state error loading))

(def update-db-entity (mkremote 'de.zalf.berest.web.castra.api/update-db-entity state error loading))
(def retract-db-value (mkremote 'de.zalf.berest.web.castra.api/retract-db-value state error loading))

(def delete-db-entity (mkremote 'de.zalf.berest.web.castra.api/delete-db-entity state error loading))
(def delete-db-entities (mkremote 'de.zalf.berest.web.castra.api/delete-db-entity state error loading))


;TODO: can't update weather-stations easily as they're actually shared most of the time, do this later properly
#_(def update-weather-station (mkremote 'de.zalf.berest.web.castra.api/update-weather-station state error loading))


(defn init-after-login []
  #_(when-not @minimal-all-crops (load-minimal-all-crops))
  #_(get-state)
  #_(js/setInterval #(if @logged-in? (get-state)) 100))







