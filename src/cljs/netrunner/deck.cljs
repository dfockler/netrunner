(ns netrunner.deck
  (:require [clojure.string :refer [split split-lines join]]
            [netrunner.cardbrowser :as cb]))

(defn identical-cards? [cards]
  (let [name (:title (first cards))]
    (every? #(= (:title %) name) cards)))

(defn found? [query cards]
  (some #(if (= (.toLowerCase (:title %)) query) %) cards))

(defn match [query cards]
  (filter #(if (= (.indexOf (.toLowerCase (:title %)) query) -1) false true) cards))

(defn lookup [query]
  (let [q (.toLowerCase query)
        cards (:cards @cb/app-state)]
    (if-let [card (some #(when (= (-> % :title .toLowerCase) q) %) cards)]
      card
      (loop [i 2 matches cards]
        (let [subquery (subs q 0 i)]
         (cond (zero? (count matches)) query
               (or (= (count matches) 1) (identical-cards? matches)) (first matches)
               (found? subquery matches) (found? subquery matches)
               (<= i (count query)) (recur (inc i) (match subquery matches))
               :else query))))))

(defn parse-line [line]
  (let [tokens (split line " ")
        qty (js/parseInt (first tokens))
        cardname (join " " (rest tokens))]
    (when-not (js/isNaN qty)
      {:qty qty :card (lookup cardname)})))

(defn parse-deck [deck]
  (reduce #(if-let [card (parse-line %2)] (conj %1 card) %1) [] (split-lines deck)))

(defn check-deck [deck])
