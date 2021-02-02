(ns building-re-frame-components.externally-managed-components.student
  (:require [reagent.core :as reagent]
            [reagent.dom :as dom]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {}))


(defn ui []
  [:div
   "Put the CodeMirror editor here."])

(when-some [el (js/document.getElementById "externally-managed-components--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (dom/render [ui] el))