(ns app.ui.experiments
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [taoensso.timbre :as log]))

(defn set-increment* [sign {:keys [count increment-count] :or {count 0 increment-count 1} :as counter}]
  (assoc counter :count (sign count increment-count)))

(defmutation increment [_]
  (action [{:keys [state]}]
          (swap! state update-in [:component/id :counter] (partial set-increment* +))))

(defmutation decrement [_]
  (action [{:keys [state]}]
          (swap! state update-in [:component/id :counter] (partial set-increment* -))))

(defmutation set-increment [{:keys [increment]}]
  (action [{:keys [state]}]
          (swap! state assoc-in [:component/id :counter :increment-count] increment)))

(defsc Counter [this {:keys [count] :or {count 0}}]
  {:query [:count :increment-count]
   :ident (fn [] [:component/id :counter])
   :initial-state {:count :param/count :increment-count :param/increment-count}}
  (letfn [(decrement-handler [] (comp/transact! this [(decrement {})]))
          (increment-handler [] (comp/transact! this [(increment {})]))]
    (dom/div :.ui.centered.card
             (dom/button :.ui.button.red.icon {:onClick decrement-handler} (dom/i :.minus.icon))
             (dom/div :.content.center.aligned
                      (dom/div :.ui.tiny.header "The count is " count))
             (dom/button :.ui.button.green.icon {:onClick increment-handler} (dom/i :.plus.icon)))))

(def ui-counter (comp/factory Counter))

(defsc Experiments [this {:keys [counter]}]
  {:query [{:counter (comp/get-query Counter)}]
   :ident (fn [] [:component/id :experiments])
   :initial-state (fn [_] {:counter (comp/get-initial-state Counter)})
   :route-segment ["experiments"]}
  (letfn [(set-increment-handler [increment] (comp/transact! this [(set-increment {:increment increment})]))]
    (dom/div :.ui.container.segment
             (dom/h3 "Main")
             (dom/button :.ui.button.primary {:onClick #(set-increment-handler 1)} "Set 1 Increment")
             (dom/button :.ui.button.primary {:onClick #(set-increment-handler 10)} "Set 10 Increment")
             (dom/button :.ui.button.primary {:onClick #(set-increment-handler 100)} "Set 100 Increment")
             (ui-counter counter))))
