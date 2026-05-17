(ns main.utils.log
  (:require ["solid-js" :refer [DEV]]))

(def ^:dynamic *scope* "sqeave")

(defn set-scope! [scope]
  (set! *scope* scope))

(defn info [& xs]
  (apply js/console.info
         (str "[" *scope* "]") xs))

(defn debug [& xs]
  (when DEV
    (apply js/console.debug
           (str "[" *scope* "]") xs)))

(defn warn [& xs]
  (apply js/console.warn
         (str "[" *scope* "]") xs))

(defn error [& xs]
  (apply js/console.info
         (str "[" *scope* "]") xs))
