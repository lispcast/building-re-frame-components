(ns building-re-frame-components.draggable-list.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :teacher/initialize
 (fn [_ _]
   {}))

(defn put-before [items pos item]
  (let [items (remove #{item} items)
        head (take pos items)
        tail (drop pos items)]
    (concat head [item] tail)))

(defn draggable-list [{:keys [on-reorder]
                       :or {on-reorder (fn [])}} & items]
  (let [items (vec items)
        s (reagent/atom {:order (range (count items))})]
    (fn []
      [:ul
       (doall
        (for [[i pos] (map vector (:order @s) (range))]
          [:li
           {:key i
            :style {:border (when (= i (:drag-index @s))
                              "1px solid blue")}
            :draggable true
            :on-drag-start #(swap! s assoc :drag-index i)
            :on-drag-over (fn [e]
                            (.preventDefault e)
                            (swap! s assoc :drag-over pos)
                            (swap! s update :order put-before pos (:drag-index @s)))
            :on-drag-leave #(swap! s assoc :drag-over :nothing)
            :on-drag-end (fn []
                           (swap! s dissoc :drag-over :drag-index)
                           (on-reorder (map items (:order @s))))}
           (get items i)]))])))

(defn ui []
  (let [s (reagent/atom {})]
    (fn []
      [:div
       (pr-str (:order @s))
       [draggable-list
        {:on-reorder (fn [item-order]
                       (swap! s assoc :order item-order))}
        "a"
        "b"
        "c"
        "d"]])))

(when-some [el (js/document.getElementById "draggable-list--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))