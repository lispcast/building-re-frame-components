(ns building-re-frame-components.tag-editor.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :teacher/initialize
 (fn [db _]
   (select-keys db (remove #(= "teacher" (namespace %)) (keys db)))))

(rf/reg-event-db
 :teacher/save-tag
 (fn [db [_ s]]
   (let [s (-> s
               .trim
               .toLowerCase)]
     (update db :teacher/tags (fnil conj #{}) s))))

(rf/reg-event-db
 :teacher/remove-tag
 (fn [db [_ s]]
   (update db :teacher/tags (fn [tags]
                              (vec (remove #{s} tags))))))
 
(rf/reg-sub
 :teacher/tags-raw
 (fn [db _]
   (:teacher/tags db)))

(rf/reg-sub
 :teacher/tags-sorted
 (fn [] (rf/subscribe [:teacher/tags-raw]))
 (fn [tags]
   (sort tags)))

(rf/reg-event-db
 :teacher/save-tag
 (fn [db [_ s]]
   (update db :teacher/tags (fnil conj []) s)))

(rf/reg-event-db
 :teacher/remove-tag
 (fn [db [_ s]]
   (update db :teacher/tags (fn [tags]
                               (vec (remove #{s} tags))))))

(rf/reg-sub
 :teacher/tags
 (fn [db _]
   (:teacher/tags db [])))

(defn tag-editor []
  (let [s (reagent/atom "")
        k (reagent/atom "")]
    (fn []
      [:div
       [:p "s: " @s]
       [:p "k: " @k]
       [:input {:type :text
                :style {:width "100%"}
                :value @s
                :on-change #(reset! s (-> % .-target .-value))
                :on-key-up (fn [e]
                             (reset! k (-> e .-key))
                             (when (or (= " " (-> e .-key))
                                       (= "Enter" (-> e .-key)))

                               (rf/dispatch [:teacher/save-tag (.trim @s)])
                               (reset! s "")))}]
       [:div
        "Tags: "
        (doall
         (for [tag @(rf/subscribe [:teacher/tags])]
           [:div {:style {:display :inline-block
                          :background-color :gray
                          :color :white
                          :margin-right "8px"}}
            tag

            [:a {:href "#"
                 :style {:margin-left "4px"}
                 :on-click (fn [e]
                             (.preventDefault e)
                             (rf/dispatch [:teacher/remove-tag tag]))}
             [:i.fa.fa-times]]]))]])))

(defn ui []
  [:div
   [tag-editor]])

(when-some [el (js/document.getElementById "tag-editor--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))