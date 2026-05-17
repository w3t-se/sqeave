(ns main.utils.log)

(def ^:dynamic *scope* "sqeave")

(defn set-scope! [scope]
  (set! *scope* scope))

(defn info [& xs]
  (apply js/console.info
         (str "[" *scope* "]") xs))

(defn debug [& xs]
  (when (.-DEV js/import.meta.env)
    (apply js/console.debug
           (str "[" *scope* "]") xs)))

(defn warn [& xs]
  (apply js/console.warn
         (str "[" *scope* "]") xs))

(defn error [& xs]
  (apply js/console.info
         (str "[" *scope* "]") xs))
