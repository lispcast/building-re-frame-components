(ns building-re-frame-components.markdown-preview.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
  :teacher/initialize
  (fn [_ _]
    {}))

(defonce converter (new js/showdown.Converter))

(defn ->html [s]
  (.makeHtml converter s))

(defn markdown-section [s]
  [:div
    {:dangerouslySetInnerHTML {:__html (->html s)}}])

(defn markdown-editor-with-preview [initial-val]
  (let [s (reagent/atom {:value initial-val})]
    (fn []
      [:div
       [:textarea {:value (:value @s)
                   :on-change (fn [e]
                                (swap! s assoc
                                       :value (-> e .-target .-value)))}]
       [markdown-section (:value @s)]])))

(defn ui []
  [:div
   [markdown-editor-with-preview "# some markdown"]])

(when-some [el (js/document.getElementById "markdown-preview--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))