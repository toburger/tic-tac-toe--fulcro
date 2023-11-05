(ns app.ui.root
  (:require [app.ui.forms :as form]
            [app.ui.tic-tac-toe :refer [TicTacToe ui-tic-tac-toe]]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.algorithms.tempid :as tempid]))

(defn new-person [app]
  (form/new-person app)
  #_(comp/transact! app [(form/new-person {:person/id (tempid/tempid)})] {:target [:root/person-form]}))

(defsc Root [this {:root/keys [tic-tac-toe person-form]}]
  {:query         [{:root/tic-tac-toe (comp/get-query TicTacToe)}
                   {:root/person-form (comp/get-query form/PersonForm)}]
   :initial-state (fn [_] {:root/tic-tac-toe (comp/get-initial-state TicTacToe)
                           :root/person-form (comp/get-initial-state form/PersonForm)})}
  (comp/fragment
   (dom/div
    #_(dom/button {:onClick (fn [] (form/new-person this))} "Load Person")
    #_(dom/button {:onClick (fn [] (new-person this))} "Load Person")
    (when person-form
      (form/ui-person-form person-form))
    #_(ui-tic-tac-toe tic-tac-toe))))
