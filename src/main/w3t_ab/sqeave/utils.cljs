(ns utils
  (:require [squint.string :as string]
            ["./log.mjs" :as log]))

(defn object? [o]
  (= (js/typeof o) "object"))

(def ^:private id-suffix "/id")

(defn get-ident [data]
  (when (some? data)
    (let [ks  (js/Object.keys data)
          len (.-length ks)]
      (loop [i 0]
        (when (< i len)
          (let [k (aget ks i)]
            (if (.endsWith k id-suffix)
              #js [k (aget data k)]            ;; return first matching ident
              (recur (inc i)))))))))

(defn get-ns [k]
  (first (string/split (first k) "/")))

(defn remove-ident [ident v]
  (filterv (fn [y] (not (= (second y)
                           (second ident)))) v))

#_(defn ident?
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

(defn remove-item [v item]
  (vec (filter #(not= % item) v)))

(defn uuid [] (js/crypto.randomUUID))

(defn ident? [x]
  (and (vector? x) (= (count x) 2)
       (let [k (aget x 0)] (and (string? k) (.endsWith k id-suffix)))))

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
  #_(log/debug "this is a map...:" (map? m) " " m)
  (cond (vector? m) (f (mapv #(distribute f %) m))
        (or (map? m) (object? m)) (f (zipmap (keys m) (mapv #(distribute f %) (vals m))))

        :else m))

(defn nsd [data ns]
  (zipmap (mapv (fn [x] (str ns "/" x)) (keys data)) (vals data))
  #_(into {} (mapv (fn [[k v]] [(str ns "/" k) v]) data)))

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
    (catch js/Error e (log/error (str "could net get item: " key " ") e) nil)))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))

(defn set-session-item!
  "Set `key' in browser's localStorage to `val`."
  [key val]
  (.setItem (.-sessionStorage js/window) key (js/JSON.stringify val)))

(defn get-session-item
  "Returns value of `key' from browser's localStorage."
  [key]
  (try
    (js/JSON.parse (.getItem (.-sessionStorage js/window) key))
    (catch js/Error e (log/error (str "could net get item: " key " ") e) nil)))

(defn remove-session-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-sessionStorage js/window) key))

(defn distinct-second-elements [coll]
  (vec (let [seen (atom #{})] ;; Use a volatile set to track seen second elements
         (filter
          (fn [[_ second]]
            (if (contains? @seen second)
              false
              (do (swap! seen conj second) true)))
          coll))))

(defn is-uuid? [val]
  (re-matches #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" val))

(defn unwrap-proxy [data]
  (cond
    (array? data) (vec (map unwrap-proxy data))
    (vector? data) (mapv unwrap-proxy data)
    (map? data) (into {} (map (fn [[k v]] [k (unwrap-proxy v)]) data))
    (object? data) (into {} (map (fn [[k v]] [k (unwrap-proxy v)]) data))
    :else data))

;; const timeZone = 'America/New_York';
;; const zonedDate = utcToZonedTime(now, timeZone);
;;                                         ;
;; (defn convert-time [zone]
;;   )
