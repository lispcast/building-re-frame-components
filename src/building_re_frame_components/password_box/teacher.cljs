(ns building-re-frame-components.password-box.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :teacher/initialize
 (fn [_ _]
   {}))

(def password-validations
  [["At least 12 characters."
    (fn [s]
      (>= (count s) 12))]
   ["At least 50% unique characters."
     (fn [s]
       (-> s
           set
           count
           (/ (count s))
           (>= 0.5)))
     ]])

(defn password-box [pw]
  (let [s (reagent/atom {:value pw})]
    (fn []
      (let [validations (for [[desc f] password-validations]
                          [desc (f (:value @s))])
            valid? (every? identity (map second validations))
            color (when (:dirty? @s) (if valid? "green" "red"))]
      [:form 
       [:label {:style {:color color}} "Password"]
      [:input {:type (if (:show? @s) :text :password)
               :style {:width "100%"
                       :border (str "1px solid " color)}
               :value (:value @s)
               :on-focus #(swap! s assoc :focus? true)
               :on-blur #(swap! s assoc :dirty? true)
               :on-change #(swap! s assoc
                                  :dirty?
                                  true
                                  :value
                                  (-> % .-target .-value))}]
       [:label [:input {:type :checkbox
                        :checked (:show? @s)
                        :on-change #(swap! s assoc
                                           :show?
                                           (-> % .-target .-checked))}]
        " Show password?"]
       (for [[desc valid?] validations]
         (when (:focus? @s)
           [:div
          {:style {:color (when (:dirty? @s) (if valid? "green" "red"))}}
          (when (:dirty? @s) (if valid? "✔ " "✘ ")) desc]))
       ] 
      ))))

(defn ui []
  [:div
   [password-box ""]])

(when-some [el (js/document.getElementById "password-box--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))