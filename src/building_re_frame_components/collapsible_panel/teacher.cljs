(ns building-re-frame-components.collapsible-panel.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))
 
(rf/reg-event-db
  :teacher/initialize
  (fn [_ _]
    {}))

(rf/reg-event-db
  :teacher/toggle-panel
  (fn [db [_ id]]
    (update-in db [:panels id] not)))

(rf/reg-sub
  :teacher/panel-state
  (fn [db [_ id]]
    (get-in db [:panels id])))

(defn example-component []
  (let [s (reagent/atom 0)]
    (js/setInterval #(swap! s inc) 1000)
    (fn []
      [:div @s])))

(defn panel [id title & children]
  (let [s (reagent/atom {:open false})]
    (fn [id title & children]
      (let [open? @(rf/subscribe [:teacher/panel-state id])
            child-height (:child-height @s)]
        [:div
         [:div {:on-click #(rf/dispatch [:teacher/toggle-panel id])
                :style {:background-color "#ddd"
                        :padding "0 1em"}}
          [:div {:style {:float "right"}}
           (if open? "-" "+")]
          title]
         [:div {:style  {:overflow "hidden"
                         :transition "max-height 0.8s"
                         :max-height (if open? child-height 0)}}
          [:div {:ref #(when %
                         (swap! s assoc :child-height (.-clientHeight %)))
                 :style {:background-color "#eee"
                         :padding "0 1em"}
                 }
           children]]]))))

(defn ui []
  [:div
   [panel :ex-1 "Example component" [example-component]]])

(when-some [el (js/document.getElementById "collapsible-panel--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))