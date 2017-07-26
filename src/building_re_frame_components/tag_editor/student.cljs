(ns building-re-frame-components.tag-editor.student
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize
 (fn [db _]
   (select-keys db (filter #(= "teacher" (namespace %)) (keys db)))))

(defn ui []
  [:div
   "Put tag editor here."])

(when-some [el (js/document.getElementById "tag-editor--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))