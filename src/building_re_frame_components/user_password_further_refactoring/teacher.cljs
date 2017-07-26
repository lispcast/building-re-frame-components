(ns building-re-frame-components.user-password-further-refactoring.teacher
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

(rf/reg-event-db
 :teacher/initialize
 (fn [_ _]
   {}))

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
   {:timeout (if (>= (count username) 3)
               [:teacher/check-username 300 [:teacher/check-username username]]
               ;; will remove timeout
               [:teacher/check-username 0 nil])}))

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
                           validations validation-args]
                    :or {on-change (fn [])
                         type :text}}]
  (let [info @state

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
              :on-focus #(swap! state assoc :focus? true)
              :on-blur  #(swap! state assoc :dirty? true)
              :on-change (fn [e]
                           (let [v (-> e .-target .-value)]
                             (on-change v)
                             (swap! state assoc
                                    :dirty? true
                                    :value v)))}]
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

(defn password-box []
  (let [s (reagent/atom {})]
    (fn []
      (labeled-box {:label "Password"
                    :validations password-validations
                    :state s
                    :type (if (:show? @s) :text :password)
                    :extra [:label [:input {:type :checkbox
                                            :checked (:show? @s)
                                            :on-change #(swap! s assoc
                                                               :show? (-> % .-target .-checked))}]
                            " Show password?"]}))))

(defn username-box []
  (let [s (reagent/atom {})
        cache (rf/subscribe [:teacher/username-cache])]
    (fn []
      (labeled-box {:label "Username"
                    :type :text
                    :state s
                    :validations username-validations
                    :validation-args [@cache]
                    :on-change #(rf/dispatch [:teacher/check-username-debounce %])}))))

(defn user-registration []
  [:div
   [username-box]
   [password-box]])

(defn ui []
  [:div
   [user-registration]])

(when-some [el (js/document.getElementById "user-password-further-refactoring--teacher")]
  (defonce _init (rf/dispatch-sync [:teacher/initialize]))
  (reagent/render [ui] el))