(ns building-re-frame-components.externally-managed-components.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :teacher/initialize
 (fn [_ _]
   {}))

(defn create-codemirror [elem options]
  (js/CodeMirror.
   elem
   (clj->js options)))

(defn codemirror [initial-value options on-blur]
  (let [s (reagent/atom {:value initial-value})]
    (reagent/create-class
     {:reagent-render (fn [] [:div])
      :component-did-mount
      (fn [this]
        (let [editor (create-codemirror (reagent/dom-node this)
                                        (assoc options
                                               :value initial-value))]
          (when on-blur
            (.on editor "blur"
                 #(on-blur (:value @s))))
          (.on editor "change"
               #(swap! s assoc
                       :value (.getValue editor)))))})))

(defn ui []
  [:div
   [codemirror "This is a CodeMirror editor.

Try focusing then blurring."
    {:lineNumbers true} println]])

(when-some [el (js/document.getElementById "externally-managed-components--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))