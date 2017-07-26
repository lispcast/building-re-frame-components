(ns building-re-frame-components.progress-bar.student
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
  :initialize
  (fn [db _]
    (select-keys db (filter #(= "teacher" (namespace %)) (keys db)))))

(defn ui []
  [:div
   "23.3%"])

(when-some [el (js/document.getElementById "progress-bar--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))