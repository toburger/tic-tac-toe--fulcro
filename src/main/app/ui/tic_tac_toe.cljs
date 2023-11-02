(ns app.ui.tic-tac-toe
  (:require [app.game-logic :as game-logic]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :refer [defmutation]]
            [taoensso.timbre :as log]))

(def restart-image "./img/restart.png")
(def player-x-image "./img/PlayerX.svg")
(def player-o-image "./img/PlayerO.svg")

(def initial-state
  {:board          [[nil nil nil]
                    [nil nil nil]
                    [nil nil nil]]
   :current-player :x
   :winner         nil})

(defmutation restart-game [_]
  (action [{:keys [state ref]}]
          (swap! state assoc-in ref initial-state)))

(defmutation move [{:keys [x y]}]
  (action [{:keys [state ref]}]
          (letfn [(next-state [{:keys [board current-player]}]
                    (let [new-board          (game-logic/update-board board x y current-player)
                          new-winner         (game-logic/get-winner new-board)
                          new-current-player (game-logic/switch-player current-player)]
                      {:board          new-board
                       :current-player new-current-player
                       :winner         new-winner}))]
            (swap! state update-in ref next-state))))

(defn player-x [props]
  (dom/img :.PlayerX
           (merge
            {:alt "PlayerX"
             :src player-x-image}
            props)))

(defn player-o [props]
  (dom/img :.PlayerO
           (merge
            {:alt "PlayerO"
             :src player-o-image}
            props)))

(defn no-player []
  (dom/span :.NoPlayer))

(defn dispatch-player [player]
  (dom/span
   (case player
     :x (player-x {:className "PlayerX--Small"})
     :o (player-o {:className "PlayerO--Small"})
     (no-player))))

(defsc GameCell [_ {:keys [x y cell]} {:keys [onMove] :or {onMove identity}}]
  {:query [:x :y :cell]
   :ident (fn [] [::GameCell [x y]])
   :initial-state {}}
  (dom/button :.Cell
              {:disabled (some? cell)
               :onClick #(onMove {:x x :y y})}
              (case cell
                :x (player-x {})
                :o (player-o {})
                (no-player))))

(def ui-game-cell (comp/computed-factory GameCell {:keyfn (fn [{:keys [x y]}] (str x "-" y))}))

(defsc GameBoard [_ {:keys [board]} {:keys [onMove]}]
  {:query [{:board (comp/get-query GameCell)}]
   :initial-state {:board []}}
  (dom/div :.Board
           (map-indexed
            (fn [rowidx row]
              (dom/div :.Board__Row
                       {:key rowidx}
                       (map-indexed
                        (fn [colidx cell]
                          (ui-game-cell
                           {:x rowidx
                            :y colidx
                            :cell cell}
                           {:onMove onMove}))
                        row)))
            board)))

(def ui-game-board (comp/computed-factory GameBoard))

(defsc CurrentPlayer [this {:keys [current-player]}]
  {}
  (dom/div :.CurrentPlayer
           (dom/span :.CurrentPlayer__Text
                     "Player:"
                     (dispatch-player current-player))))

(def ui-current-player (comp/factory CurrentPlayer))

(defsc GameField [_ {:keys [board current-player]} {:keys [onMove]}]
  {}
  (dom/div
   (ui-game-board {:board board} {:onMove onMove})
   (ui-current-player {:current-player current-player})))

(def ui-game-field (comp/computed-factory GameField))

(defsc GameOver [_ {:keys [winner]} {:keys [onGameOver] :or {onGameOver identity}}]
  (dom/div :.GameOver
           (dom/img :.GameOver__Image
                    {:onClick #(onGameOver)
                     :src restart-image
                     :alt "Restart"})
           (dom/p :.GameOver__Text
                  (case winner
                    :draw "It's a draw!"
                    (dom/span "Player " (dispatch-player winner) " wins!")))))

(def ui-game-over (comp/computed-factory GameOver))

(defn prefetch-images []
  (for [img [player-x-image player-o-image restart-image]]
    (dom/link {:key img :rel "prefetch" :href img})))

(defsc TicTacToe [this {:keys [board current-player winner]}]
  {:ident (fn [] [:component/id :tic-tac-toe])
   :query [:board :current-player :winner]
   :initial-state (fn [_] initial-state)
   :route-segment ["tic-tac-toe"]}
  (dom/div :.Content
           (dom/div :.App
                    (prefetch-images)
                    (if winner
                      (ui-game-over {:winner winner}
                                    {:onGameOver (fn [] (comp/transact! this [(restart-game {})]))})
                      (ui-game-field {:board board
                                      :current-player current-player}
                                     {:onMove (fn [coords] (comp/transact! this [(move coords)]))})))))

(def ui-tic-tac-toe (comp/factory TicTacToe))