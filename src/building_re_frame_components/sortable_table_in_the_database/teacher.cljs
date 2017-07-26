(ns building-re-frame-components.sortable-table-in-the-database.teacher
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
 :teacher/initialize
 (fn [db _]
   (assoc db :teacher/tables {:new-hope {:header (first data)
                        :rows (rest data)}})))

(rf/reg-sub
 :teacher/table
 (fn [db [_ key]]
   (get-in db [:teacher/tables key])))

(rf/reg-sub
 :teacher/table-sorted
 (fn [[_ key] _]
   (rf/subscribe [:teacher/table key]))
 (fn [table]
   (let [key (:sort-key table)
         dir (:sort-direction table)
         rows (cond->> (:rows table)
                key (sort-by #(nth % key))
                (= :ascending dir) reverse)]
     (assoc table :rows rows))))

(rf/reg-event-db
 :teacher/table-sort-by
 (fn [db [_ key i dir]]
   (update-in db [:teacher/tables key]
              assoc :sort-key i :sort-direction dir)))

(rf/reg-event-db
 :teacher/table-clear-sort
 (fn [db [_ key]]
   (update-in db [:teacher/tables key]
              dissoc :sort-key :sort-direction)))

(rf/reg-event-fx
 :teacher/table-rotate-sort
 (fn [{:keys [db]} [_ key i]]
   (let [{:keys [sort-key sort-direction]} (get-in db [:teacher/tables key])
         sorts [sort-key sort-direction]]
     {:dispatch (cond
                  (= [i :ascending] sorts)
                  [:teacher/table-clear-sort key]
                  
                  (= [i :descending] sorts)
                  [:teacher/table-sort-by key i :ascending]
                  
                  :else
                  [:teacher/table-sort-by key i :descending])})))

(defn sortable-table [table-key]
  (let [table @(rf/subscribe [:teacher/table-sorted table-key])
        sorts [(:sort-key table) (:sort-direction table)]]
    [:table {:style {:font-size "80%"}}
     [:tr
      (for [[i h] (map vector (range) (:header table))]
        [:th
         {:on-click #(rf/dispatch [:teacher/table-rotate-sort table-key i])
          :style {:cursor :default}}
         [:div {:style {:display :inline-block}}
          h]
         [:div {:style {:display :inline-block
                        :line-height :1em
                        :font-size :60%}}
          [:div
           {:style {:color (if (= [i :descending] sorts)
                             :black
                             "#aaa")}}
           "▲"]
          [:div
           {:style {:color (if (= [i :ascending] sorts)
                             :black
                             "#aaa")}}
           "▼"]]])]
     (for [row (:rows table)]
       [:tr
        (for [v row]
          [:td v])])]))

(defn ui []
  [:div
   [sortable-table :new-hope]
   [sortable-table :new-hope]])

(when-some [el (js/document.getElementById "sortable-table-in-the-database--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))