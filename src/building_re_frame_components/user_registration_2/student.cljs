(ns building-re-frame-components.user-registration-2.student
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
      (if (>= (count s) 12)
        :pass
        :fail))]
   ["At least 50% unique characters."
    (fn [s]
      (if (-> s
              set
              count
              (/ (count s))
              (>= 0.5))
        :pass
        :fail))]])

(defn status->color [status]
  (case status
    :pass "green"
    :fail "red"
    :loading nil))

(defn validate [validations value args]
  (let [v (for [[desc f] validations]
            [desc (apply f value args)])
        s (reduce combine-statuses :pass (map second v))]
    {:validations v
     :status      s}))

(defn status-line [description status show-status?]
  (let [color (when show-status?
                (status->color status))
        icon (when show-status?
               (status->icon status))]
    [:div {:key description :style {:color color}}
     icon description]))

(defn render-validations [validations show-status?]
  (doall
   (for [[desc status] validations]
     (status-line desc status show-status?))))

(defn status->icon [status]
  (case status
    :pass    [:span [:i.fa.fa-check]           " "]
    :fail    [:span [:i.fa.fa-remove]          " "]
    :loading [:span [:i.fa.fa-spinner.fa-spin] " "]))

(defn labeled-box [{:keys [label state type extra on-change
                           validations validation-args
                           key]
                    :or {on-change (fn [])
                         type :text}}]
  (let [info (get @state key)

        {:keys [validations status]}
        (validate validations (:value info) validation-args)

        color (when (:dirty? info)
                (status->color status))]
    [:div
     [:label {:style {:color color}}
      (when (:dirty? info)
        (status->icon status))
      label]
     [:input {:type type
              :style {:width "100%"
                      :border (str "1px solid " color)}
              :value (:value info)
              :on-focus #(swap! state assoc-in [key :focus?] true)
              :on-blur  #(swap! state assoc-in [key :dirty?] true)
              :on-change (fn [e]
                           (let [v (-> e .-target .-value)]
                             (on-change v)
                             (swap! state
                                    #(-> %
                                         (assoc-in [key :dirty?] true)
                                         (assoc-in [key :value ] v)))))}]
     extra
     (when (:focus? info)
       (render-validations validations (:dirty? info)))]))

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

(defn password-box [s]
  (labeled-box {:label "Password"
                :validations password-validations
                :state s
                :key :password
                :type (if (:show? (:password @s)) :text :password)
                :extra [:label [:input {:type :checkbox
                                        :checked (:show? (:password @s))
                                        :on-change #(swap! s assoc-in
                                                           [:password :show?] (-> % .-target .-checked))}]
                        " Show password?"]}))

(defn username-box [s]
  (labeled-box {:label "Username"
                :type :text
                :state s
                :key :username
                :validations username-validations
                :validation-args [@(rf/subscribe [:username-cache])]
                :on-change #(rf/dispatch [:check-username-debounce %])}))

(defn user-registration []
  (let [s (reagent/atom {})]
    (fn []
      [:form
       [username-box s]
       [password-box s]])))

(rf/reg-sub
 :world
 (fn [db] db))

(defn ui []
  [:div
   [:button
    {:on-click #(rf/dispatch-sync [:initialize])}
    "Start Over"]
   [:hr]
   [user-registration]
   [:hr]
   [:div (pr-str @(rf/subscribe [:world]))]])

(when-some [el (js/document.getElementById "user-registration-2--student")]
  (defonce _init (rf/dispatch-sync [:initialize]))
  (reagent/render [ui] el))