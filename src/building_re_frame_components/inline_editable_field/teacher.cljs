(ns building-re-frame-components.inline-editable-field.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
  :teacher/initialize
  (fn [db _]
    (assoc db :teacher/movies {"tt0095989"
              {:title "Return of the Killer Tomatoes!"
               :description "Crazy old Professor Gangreen has developed a way to make tomatoes look human for a second invasion."}})))

(rf/reg-sub
  :teacher/movies
  (fn [db _]
    (:teacher/movies db)))

(rf/reg-event-db
  :teacher.movie/title
  (fn [db [_ id title]]
    (assoc-in db [:teacher/movies id :title] title)))

(rf/reg-event-db
  :teacher.movie/description
  (fn [db [_ id description]]
    (assoc-in db [:teacher/movies id :description] description)))

(defn inline-editor [txt on-change]
  (let [s (reagent/atom {})]
    (fn [txt on-change]
      [:span
       (if (:editing? @s)
         [:form {:on-submit #(do
                               (.preventDefault %)
                               (swap! s dissoc :editing?)
                               (when on-change
                                 (on-change (:text @s))))}
           [:input {:type :text :value (:text @s)
                    :on-change #(swap! s assoc 
                                     :text (-> % .-target .-value))}]
          [:button "Save"]
          [:button {:on-click #(do
                                 (.preventDefault %)
                                 (swap! s dissoc :editing?))}
           "Cancel"]]
         [:span 
           {:on-click #(swap! s assoc
                            :editing? true
                            :text txt)}
            txt [:sup "✎"]])])))

(defn ui []
  [:div
   (for [[movie-id movie] @(rf/subscribe [:teacher/movies])]
     [:div {:key movie-id}
      [:h3  [inline-editor (:title movie)
             #(rf/dispatch [:teacher.movie/title movie-id %])]]
      [:div [inline-editor (:description movie)
             #(rf/dispatch [:teacher.movie/description movie-id %])]]])])

(when-some [el (js/document.getElementById "inline-editable-field--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))