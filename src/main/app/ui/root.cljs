(ns app.ui.root
  (:require [app.ui.tic-tac-toe :refer [TicTacToe ui-tic-tac-toe]]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]))

(defsc Root [this {:root/keys [tic-tac-toe]}]
  {:query         [{:root/tic-tac-toe (comp/get-query TicTacToe)}]
   :initial-state (fn [_] {:root/tic-tac-toe (comp/get-initial-state TicTacToe)})}
  (ui-tic-tac-toe tic-tac-toe))
