(ns building-re-frame-components.draggable-list.student
  (:require [reagent.core :as reagent]
            [reagent.dom :as dom]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {}))

(defn ui []
  [:div
   [:div
    "a"
    "b"
    "c"
    "d"]])

(when-some [el (js/document.getElementById "draggable-list--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (dom/render [ui] el))