(ns app.ui.forms
  (:require [com.fulcrologic.fulcro.algorithms.form-state :as fs]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :refer [defmutation]]
            [taoensso.timbre :as log]))

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
  (log/info :person-form props)
  (dom/pre (str props) #_(js/JSON.stringify (clj->js props))))

(def ui-person-form (comp/factory PersonForm))

(def person-tree
  {:db/id (tempid/tempid)
   :person/name "Joe"
   :person/phone-numbers [{:db/id (tempid/tempid) :phone/number "555-1212"}]})

(def person-tree-with-form-support (fs/add-form-config PersonForm person-tree))

(defn new-person [app]
  (merge/merge-component! app PersonForm person-tree-with-form-support
                          :replace [:root/person-form]))

(defmutation new-person-mutation [{:person/keys [id]}]
  (action [{:keys [state]}]
          (swap! state (fn [s]
                         (-> s
                             (fs/add-form-config* PersonForm [:person/id id]))))))
