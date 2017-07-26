(ns building-re-frame-components.username-box.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(rf/reg-event-db
 :teacher/initialize
 (fn [_ _]
   {}))

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

(rf/reg-sub
 :teacher/username-cache
 (fn [db _]
   (get db :teacher/username-cache {})))

(rf/reg-event-fx
 :teacher/console-log
 (fn [_ [_ data]]
   (js/console.log data)
   {}))

(rf/reg-event-db
 :teacher/save-username
 (fn [db [_ {:keys [username exists]}]]
   (assoc-in db [:teacher/username-cache username]
                (if exists :taken :free))))

(rf/reg-event-fx
 :teacher/check-username
 (fn [ctx [_ username]]
   {:http-xhrio {:uri "https://whispering-cove-34851.herokuapp.com/users"
                 :params {:u username}
                 :method :get
                 :timeout 10000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:teacher/save-username]
                 :on-failure [:teacher/console-log]}}))

(defonce timeouts (atom {}))

(rf/reg-fx
 :teacher/timeout
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
 :teacher/check-username-debounce
 (fn [ctx [_ username]]
   {:teacher/timeout (if (>= (count username) 3)
               [:teacher/check-username 300 [:teacher/check-username username]]
               ;; will remove timeout
               [:teacher/check-username 0 nil])}))

(defn username-box [username]
  (let [s (reagent/atom {:value username})
        cache (rf/subscribe [:teacher/username-cache])]
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
                                 (rf/dispatch [:teacher/check-username-debounce v])
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
                   :pass "✔ "
                   :fail "✘ "
                   :loading [:span [:i.fa.fa-spinner.fa-spin] " "]))
               desc])))]))))

(defn ui []
  [:div
   [username-box ""]])

(when-some [el (js/document.getElementById "username-box--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))