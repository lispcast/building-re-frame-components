(ns building-re-frame-components.progress-bar.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
  :teacher/initialize
  (fn [_ _]
    {:teacher/done 0 :teacher/total 100}))

(rf/reg-event-db
  :teacher/set-total
  (fn [db [_ total]]
    (assoc db :teacher/total total :teacher/done 0)))

(rf/reg-event-db
  :teacher/inc-done
  (fn [db [_ done]]
    (if (>= (+ done (:teacher/done db)) (:teacher/total db))
      (assoc db :teacher/done (:teacher/total db))
      (update db :teacher/done + done))))

(rf/dispatch-sync [:teacher/set-total 100])

(defonce _interval (js/setInterval
                    #(rf/dispatch-sync [:teacher/inc-done 3])
                    1000))

(rf/reg-sub
  :teacher/total
  (fn [db] (:teacher/total db)))

(rf/reg-sub
  :teacher/done
  (fn [db] (:teacher/done db)))
  
(defn progress [done]
  (let [s (reagent/atom {})]
    (fn [done]
      (let [done (str (.toFixed (* 100 done) 1) "%")]
        [:div {:style {:position :relative
                       :line-height "1.3em"}}
         [:div {:style {:background-color :green 
                        :top 0
                        :bottom 0
                        :transition "width 0.1s"
                        :width done
                        :position :absolute
                        :overflow :hidden}}
          [:span {:style {:margin-left (:left @s)
                          :color :white}}
           done]]
         [:div {:style {:text-align :center}}
          [:span 
           {:ref #(if %
                    (swap! s assoc :left (.-offsetLeft %))
                    (swap! s assoc :left 0))}
           done]]]))))

(defn ui []
  [:div
   [progress (/ @(rf/subscribe [:teacher/done]) @(rf/subscribe [:teacher/total]))]])

(when-some [el (js/document.getElementById "progress-bar--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))