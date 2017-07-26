(ns building-re-frame-components.username-and-password-refactoring.student
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {}))

(rf/reg-sub
 :username-cache
 (fn [db _]
   (get db :username-cache {})))

(rf/reg-event-fx
 :console-log
 (fn [_ [_ data]]
   (js/console.log data)
   {}))

(rf/reg-event-db
 :save-username
 (fn [db [_ {:keys [username exists]}]]
   (assoc-in db [:username-cache username]
             (if exists :taken :free))))

(rf/reg-event-fx
 :check-username
 (fn [ctx [_ username]]
   {:http-xhrio {:uri "https://whispering-cove-34851.herokuapp.com/users"
                 :params {:u username}
                 :method :get
                 :timeout 10000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:save-username]
                 :on-failure [:console-log]}}))

(defonce timeouts (atom {}))

(rf/reg-fx
 :timeout
 (fn [[key time event]]
   (when-some [to (get @timeouts key)]
     (js/clearTimeout to)
     (swap! timeouts dissoc key))
   (when (some? event)
     (swap! timeouts assoc key
            (js/setTimeout
             #(rf/dispatch event)
             time)))))

(rf/reg-event-fx
 :check-username-debounce
 (fn [ctx [_ username]]
   {:timeout (if (>= (count username) 3)
               [:check-username 300 [:check-username username]]
               ;; will remove timeout
               [:check-username 0 nil])}))

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
          (>= 0.5)))]])

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
              (when (:dirty? @s) (if valid? "✔ " "✘ ")) desc]))]))))

(def username-validations
  [["At least 3 characters."
    (fn [username _]
      (if (>= (count username) 3)
        :pass
        :fail))]
   ["Username must not be taken."
    (fn [username cache]
      (case (get cache username :unknown)
        :unknown
        :loading
        :taken
        :fail
        :free
        :pass))]])

(defn combine-statuses [statusa statusb]
  (cond
    (= :fail statusa)
    :fail
    (= :fail statusb)
    :fail
    (= :loading statusa)
    :loading
    (= :loading statusb)
    :loading
    :else
    :pass))

(defn username-box [username]
  (let [s (reagent/atom {:value username})
        cache (rf/subscribe [:username-cache])]
    (fn []
      (let [validations (for [[desc f] username-validations]
                          [desc (f (:value @s) @cache)])
            status (reduce combine-statuses :pass (map second validations))
            color (when (:dirty? @s)
                    (case status
                      :pass "green"
                      :fail "red"
                      :loading nil))]
        [:form
         [:label {:style {:color color}} "Username"]
         [:input {:type :text
                  :style {:width "100%"
                          :border (str "1px solid " color)}
                  :value (:value @s)
                  :on-focus #(swap! s assoc :focus? true)
                  :on-blur #(swap! s assoc :dirty? true)
                  :on-change (fn [e]
                               (let [v (-> e .-target .-value)]
                                 (rf/dispatch [:check-username-debounce v])
                                 (swap! s assoc
                                        :dirty? true
                                        :value v)))}]
         (doall
          (for [[desc status] validations]
            (when (:focus? @s)
              [:div
               {:key desc
                :style {:color (when (:dirty? @s)
                                 (case status
                                   :pass "green"
                                   :fail "red"
                                   :loading nil))}}
               (when (:dirty? @s)
                 (case status
               :pass [:span [:i.fa.fa-check] " "]
               :fail [:span [:i.fa.fa-remove] " "]
               :loading [:span [:i.fa.fa-spinner.fa-spin] " "]))
               desc])))]))))

(defn ui []
  [:div
   [username-box ""]
   [password-box ""]])

(when-some [el (js/document.getElementById "username-and-password-refactoring--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))