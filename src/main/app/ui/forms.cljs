(ns app.ui.forms
  (:require [clojure.pprint :as pprint]
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :refer [defmutation]]
            [com.fulcrologic.fulcro.mutations :as m]))

(defsc PhoneForm [this props]
  {:query [:db/id :phone/number fs/form-config-join]
   :ident [:phone/id :db/id]
   :form-fields #{:phone/number}}
  (dom/pre (js/JSON.stringify (clj->js props))))

(def ui-phone-form (comp/factory PhoneForm))

(defsc PersonForm [this props]
  {:query [:db/id :person/name {:person/phone-numbers (comp/get-query PhoneForm)}
           fs/form-config-join]
   :ident [:person/id :db/id]
   :form-fields #{:person/name :person/phone-numbers}}
  (dom/div
   (dom/dl
    (letfn [(info [msg value]
              (comp/fragment
               (dom/dt msg)
               (dom/dd (str value))))]
      (comp/fragment
       (info "Valid Spec?" (fs/valid-spec? props))
       (info "Person name valid?" (fs/valid-spec? props :person/name))
       (info "Person name invalid?" (fs/invalid-spec? props :person/name))
       (info "Checked?" (fs/checked? props))
       (info "Dirty?" (fs/dirty? props))))

    (dom/form
     {:onSubmit (fn [e] (.preventDefault e))}
     (dom/h1 "THE FORM")
     (dom/input {:onChange #(m/set-string! this :person/name :event %)
                 :value (:person/name props)})
     (dom/button {:onClick (fn [] (comp/transact! this [(fs/mark-complete! {})]))} "Validate")
     (when (fs/invalid-spec? props :person/name)
       (dom/span "Invalid username!")))
    (dom/pre (with-out-str (pprint/pprint props)) #_(js/JSON.stringify (clj->js props))))))

(def ui-person-form (comp/factory PersonForm))

(def person-tree
  {:db/id (tempid/tempid)
   :person/name "Joe"
   :person/phone-numbers [{:db/id (tempid/tempid) :phone/number "555-1212"}]}) (def person-tree-with-form-support (fs/add-form-config PersonForm person-tree #_{:destructive? true})) (defn new-person [app]
                                                                                                                                                                                        (merge/merge-component! app PersonForm person-tree-with-form-support
                                                                                                                                                                                                                :replace [:root/person-form]))

(defmutation new-person-mutation [{:person/keys [id]}]
  (action [{:keys [state]}]
          (swap! state (fn [s]
                         (-> s
                             (fs/add-form-config* PersonForm [:person/id id]))))))
