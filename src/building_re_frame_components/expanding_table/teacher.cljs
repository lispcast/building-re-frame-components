(ns building-re-frame-components.expanding-table.teacher
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
 :teacher/initialize
 (fn [db _]
   (assoc db :teacher/tables {:population data})))

(rf/reg-sub
 :teacher/table
 (fn [db [_ key]]
   (get-in db [:teacher/tables key])))

(defn nested-table [table-key]
  (let [s (reagent/atom {})]
    (fn [table-key]
      (let [table @(rf/subscribe [:teacher/table table-key])]
        [:div
         [:table {:style {:font-size "80%"}}
          [:tr
           (doall
            (for [h ["Country" "Population" "Cities"]]
              [:th
               {:key h}
               [:div {:style {:display :inline-block}}
                h]]))]
          (doall
           (for [[country data] table]
             (list
              [:tr {:key country}
               [:td country]
               [:td (:population data)]
               [:td
                (if (get-in @s [:expand country])
                  [:div
                   {:on-click #(swap! s update :expand dissoc country)}
                   "-"]
                  [:div
                   {:on-click #(swap! s assoc-in [:expand country] true)}
                   "+"])]]
              (when (get-in @s [:expand country])
                (doall
                 (for [[city pop] (:cities data)]
                   [:tr
                    {:key city
                     :style {:background-color "#888"
                             :line-height "1em"}}
                    [:td {:style {:padding-left "2em"}}
                     city]
                    [:td pop]
                    [:td]]))))))]
         (pr-str @s)]))))

(defn ui []
  [:div
   [nested-table :population]])

(when-some [el (js/document.getElementById "expanding-table--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))