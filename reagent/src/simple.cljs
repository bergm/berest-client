(ns simple
  (:require [reagent.core :as r]
            [ajax.core :as ajax]))

#_(set! cljs.core/*print-fn* #(.log js/console %))
(enable-console-print!)

(defn by-id [id]
  (.getElementById js/document id))

(def state (r/atom {:plot-ids ["b" "c" "d"]
                  :selected-plot-id "ccccc"
                  :until-day-month [10 10]
                  :weather-year 1993
                  :irrigation-data [[1 4 22] [2 5 10] [11 7 30]]}))
(add-watch state :s #(println "state: " state))

(def temp-irrigation-data (r/atom [nil nil nil]))
(add-watch temp-irrigation-data :tid #(println "temp-irrigation-data: " temp-irrigation-data))

(defn is-leap-year [year]
  (= 0 (rem (- 2012 year) 4)))

(defn indexed [col]
  (->> col
       (interleave (range) ,,,)
       (partition 2 ,,,)))

(defn val-event [event]
  (-> event .-target .-value))

(defn val-id [event]
  (-> event .-target .-id))

(defn calc-and-download []
  #_(let [[day month] (:until-day-month @state)
        wy (by-id "weather-year")
        url (str "rest/farms/111/plots/" (:selected-plot-id @state) ".csv"
                 ;"?format=csv"
                 "?sim=false&until-day=" day "&until-month=" month
                 "&weather-year=" (js/parseInt (.-value (.item (.-options wy) (.-selectedIndex wy)))) #_(:weather-year state)
                 "&irrigation-data=" (prn-str (:irrigation-data state)))]
    (-> js/window
        (.open ,,, url))))

(defn sim-and-download []
  #_(let [[day month] (:until-day-month @state)
        wy (by-id "weather-year")
        url (str "rest/farms/111/plots/" (:selected-plot-id @state) ".csv"
                 ;"?format=csv"
                 "?sim=true&until-day=" day "&until-month=" month
                 "&weather-year=" (js/parseInt (.-value (.item (.-options wy) (.-selectedIndex wy))))) #_(:weather-year state)]
    (-> js/window
        (.open ,,, url))))

(defn remove-irrigation-row [row-no]
  (swap! state assoc :irrigation-data
         (->> (:irrigation-data @state)
              (keep-indexed #(when-not (= %1 row-no) %2) ,,,)
              (into [] ,,,))))

(defn add-irrigation-row [_]
  (swap! state update-in [:irrigation-data] conj @temp-irrigation-data)
  (reset! temp-irrigation-data [nil nil nil]))


(ajax/GET "http://localhost:3000/data/farms/zalf/plots/"
          {:headers {"Accept" "application/edn"}
           :handler #(swap! state assoc :plot-ids %)
           :error-handler #(js/alert (str "Error: " %))})



(defn create-option
  [value & [display-value]]
  [:option {:key value :value value}
   (or display-value value)])

(defn create-irrigation-inputs
  [& [row-no day month amount]]
  [:div {:key row-no}
   [:input {:type "number"
            :placeholder "Tag"
            :value day
            :data-id "day"
            :data-row-no row-no
            :on-change #(if row-no
                          (swap! state assoc-in [:irrigation-data row-no 0] (val-event %))
                          (swap! temp-irrigation-data assoc-in [0] (val-event %)))}]
   [:input {:type "number"
            :placeholder "Monat"
            :value month
            :data-id "month"
            :data-row-no row-no
            :on-change #(if row-no
                          (swap! state assoc-in [:irrigation-data row-no 1] (val-event %))
                          (swap! temp-irrigation-data assoc-in [1] (val-event %)))}]
   [:input {:type "number"
            :placeholder "Menge [mm]"
            :value amount
            :data-id "amount"
            :data-row-no row-no
            :on-change #(if row-no
                          (swap! state assoc-in [:irrigation-data row-no 2] (val-event %))
                          (swap! temp-irrigation-data assoc-in [2] (val-event %)))}]
   [:input {:type "button"
            :data-row-no row-no
            :value (if row-no "Zeile entfernen" "Zeile hinzufügen")
            :on-click (if row-no (partial remove-irrigation-row row-no) add-irrigation-row)}]])

(js/setTimeout #(reset! temp-irrigation-data [55 66 77]) 1000)
(js/setTimeout #(reset! temp-irrigation-data [nil 88 99]) 2000)


(defn simple []
  [:form {:name "test-data-form"}
   [:fieldset
    [:legend "Schlag Id"]
    [:select {:value (:selected-plot-id @state)
              :on-change #(swap! state assoc :weather-year 0)}
     (for [pid (:plot-ids @state)]
       [create-option pid])]]

   [:fieldset
    [:legend "Rechnen bis Datum"]
    [:input {:id "day"
             :type "number"
             :placeholder "Tag"
             :value (-> @state :until-day-month first)
             :on-change #(swap! state assoc-in [:until-day-month 0] (val-id "day"))}]
    [:input {:id "month"
             :type "number"
             :placeholder "Monat"
             :value (-> @state :until-day-month second)
             :on-change #(swap! state assoc-in [:until-day-month 1] (val-id "month"))}]]

   [:fieldset
    [:legend "Wetterdaten für Jahr"]
    [:select {:id "weather-year"
              :value (:weather-year @state)}
     (for [y (range 1993 (inc 1998))]
       [create-option y (str y)])]]


   [:fieldset
    [:legend "Beregnungsdaten"]
    (for [[row-no [day month amount]] (indexed (:irrigation-data @state))]
      [create-irrigation-inputs row-no day month amount])
    (apply create-irrigation-inputs nil @temp-irrigation-data)]

   [:input {:type "button"
            :on-click calc-and-download
            :value "Berechnen & CSV-Downloaden"}]
   [:input {:type "button"
            :on-click sim-and-download
            :value "Simulieren & CSV-Downloaden"}]

   [:a {:href "https://dl.dropboxusercontent.com/u/29574974/Weberest/output-analysis.xlsx"}
    "Analyse Excel-File herunterladen"]])









(def timer (r/atom (js/Date.)))
(def time-color (r/atom "#f34"))

(defn update-time [time]
  ;; Update the time every 1/10 second to be accurate...
  (js/setTimeout #(reset! time (js/Date.)) 100))

(defn greeting [message]
  [:h1 message])

(defn clock []
  (update-time timer)
  (let [time-str (-> @timer .toTimeString (clojure.string/split " ") first)]
    [:div.example-clock
     {:style {:color @time-color}}
     time-str]))

(defn color-input []
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value @time-color
            :on-change #(reset! time-color (-> % .-target .-value))}]])

(defn simple-example []
  [:div
   [greeting "Hello world, it is now"]
   [clock]
   [color-input]])

(defn ^:export run []
  (r/render-component [simple]
                      (.-body js/document)))
