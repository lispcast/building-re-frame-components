(ns building-re-frame-components.collapsible-panel.student
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {}))

(defn ui []
  [:div
   [:h1 "Edit this string in the code"]])

(when-some [el (js/document.getElementById "collapsible-panel--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))