(ns building-re-frame-components.sortable-table.student
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(def data
  [["Name" "Weapon" "Side" "Height (m)"]
   ["Luke Skywalker" "Blaster" "Good" 1.72]
   ["Leia Organa" "Blaster" "Good" 1.5]
   ["Han Solo" "Blaster" "Good" 1.8]
   ["Obi-Wan Kenobi" "Light Saber" "Good" 1.82]
   ["Chewbacca" "Bowcaster" "Good" 2.28]
   ["Darth Vader" "Light Saber" "Bad" 2.03]
   ])

(rf/reg-event-db
 :initialize
 (fn [db _]
   {}))

(defn ui []
  [:div
   "Put table here"])

(when-some [el (js/document.getElementById "sortable-table--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))