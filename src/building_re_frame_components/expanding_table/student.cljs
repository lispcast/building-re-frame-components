(ns building-re-frame-components.expanding-table.student
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(def data
  {"US" {:population 321000000
         :cities {"New York" 8500000
                  "Los Angeles" 3928000
                  "Chicago" 2722000}}
   "China" {:population 1400000000
            :cities {"Guangzhou" 20800000
                     "Shanghai" 24500000
                     "Beijing" 21500000}}
   "France" {:population 66800000
             :cities {"Paris" 2152000
                      "Marseille" 808000
                      "Lyon" 422000}}})

(rf/reg-event-db
 :initialize
 (fn [db _]
   (assoc db :tables {:population data})))

(rf/reg-sub
 :table
 (fn [db [_ key]]
   (get-in db [:tables key])))

(defn ui []
  [:div
   "Put table here"])

(when-some [el (js/document.getElementById "expanding-table--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))