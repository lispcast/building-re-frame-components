(ns building-re-frame-components.accordion-component.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :teacher/initialize
 (fn [_ _]
   {}))

(defn accordion [options & children]
  (let [s (reagent/atom {:current (:active options)})]
    (fn [options & children]
      (let [pairs (partition-all 2 children)
            i-h-c (map conj pairs (range))]
        @s
        [:div
         (for [[i header content] i-h-c]
           [:div
            [:div {:style {:background-color "#aaa"}
                   :on-click (fn []
                               (swap! s update :current
                                      #(if (= i %) nil i)))}
             header]
            [:div
             {:style {:background-color "#ccc"
                      :height (if (= i (:current @s))
                                (when-let [el (get-in @s [:refs i])]
                                  (.-clientHeight el))
                                0)
                      :overflow :hidden
                      :transition "height 0.2s"}}
             [:div
              {:ref #(swap! s assoc-in [:refs i] %)}
              content]]])]))))

(defn ui []
  [:div
   [accordion {:active 1}
    "a" [:p "Choice A"]
    "b" [:p "Choice B"]
    "c" [:p "Choice C"]]])

(when-some [el (js/document.getElementById "accordion-component--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))