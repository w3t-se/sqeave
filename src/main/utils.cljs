(ns utils
  (:require [squint.string :as string]
            ["lodash" :as l :refer [camelCase startCase kebabCase]]
            [squint.string :as str]))

(defn object? [o]
  (= (js/typeof o) "object"))

(defn get-ident [data]
  (if-let [ident-key (first (filter #(re-find #"/id$" %) (keys data)))]
    [ident-key (get data ident-key)]))

(defn get-ns [k]
  (first (str/split (first k) "/")))

(defn remove-ident [ident v]
  (filterv (fn [y] (not (= (second y)
                           (second ident)))) v))

(defn ident?
  "Check if x is a EQL ident."
  [x]
  (and (vector? x)
       (string? (first x))
       (= 2 (count x))
       (or (string? (second x)) (number? (second x)) (undefined? (second x)))))

(defn string? [thing]
  (= (js/typeof thing) "string"))

(defn uuid? [s]
  (re-matches #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$" s))

(defn stream-id? [s]
  (re-matches #"^kjz[a-zA-Z0-9]{43,}$" s))

(defn template [s]
  (l/template s))

(defn remove-item [v item]
  (vec (filter #(not= % item) v)))

(defn uuid [] (js/crypto.randomUUID))

(def camel-case camelCase)
(def kebab-case kebabCase)

(defn pascal-case [s]
  (startCase (camelCase s)))

(defn e->v [e]
  (-> e :target :value))

(defn remove-ns [thing]
  (cond (vector? thing) (mapv remove-ns thing)
        (string? thing) (or (second (string/split thing "/")) thing)
        (map? thing) (zipmap (mapv remove-ns (keys thing)) (remove-ns (vals thing)))))

(defn copy-to-clipboard [text-to-copy]
  (.writeText (.-clipboard js/navigator) text-to-copy))

(defn random-evm []
  (string/join "0x" (.toString (js/crypto.randomBytes 32) "hex")))

(defn drop-false [m]
  (into {} (filterv (fn [x] (and (not (false? (second x))) (not (nil? (second x))))) m)))

(defn trunc-id [s]
  (.substring s (- (aget s :length) 8)))

(defn distribute [f m]
  #_(println "this is a map...:" (map? m) " " m)
  (cond (vector? m) (f (mapv #(distribute f %) m))
        (or (map? m) (object? m)) (f (zipmap (keys m) (mapv #(distribute f %) (vals m))))

        :else m))

(defn nsd [data ns]
  (zipmap (mapv (fn [x] (str ns "/" x)) (keys data)) (vals data))
  #_(into {} (mapv (fn [[k v]] [(str ns "/" k) v]) data)))

(defn add-ns [data]
  (distribute (fn [e]
                #_(println "edges:1: " e (contains? e :edges))
                (cond
                  (contains? e :edges) (add-ns (vals (get e :edges)))
                  (contains? e :node) (add-ns (get e :node))
                  (contains? e :__typename) (let [n (kebab-case (:__typename e))]
                                              (nsd (dissoc e :__typename) n))
                  :else e)) data))

;; local storage

(defn set-item!
  "Set `key' in browser's localStorage to `val`."
  [key val]
  (.setItem (.-localStorage js/window) key (js/JSON.stringify val)))

(defn get-item
  "Returns value of `key' from browser's localStorage."
  [key]
  (try
    (js/JSON.parse (.getItem (.-localStorage js/window) key))
    (catch js/Error e (println (str "could net get item: " key " ") e) nil)))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))

;; const timeZone = 'America/New_York';
;; const zonedDate = utcToZonedTime(now, timeZone);
;;                                         ;
;; (defn convert-time [zone]
;;   )
