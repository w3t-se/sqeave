(ns normad
  (:require ["./utils.mjs" :as u]
            ["consola/browser" :refer [consola]]
            ["solid-js/store" :refer [reconcile]]
            ["solid-js" :refer [batch]]))

#_(defn ident? [data]
  (if (vector? data)
    (if (string? (first data))
      (and
       (re-find #"/id$" (first data))
       (= (count data) 2)
       (or (number? (second data)) (string? (second data)) (undefined? (second data))))
      false)
    false))

(def ^:private id-suffix "/id")

(defn ident? [x]
  (and (vector? x)
       (= (count x) 2)
       (let [k (first x) v (second x)]
         (and (string? k)
              (.endsWith k id-suffix)
              (or (number? v) (string? v) (undefined? v))))))


(defn traverse-and-transform [item setStore]
  (cond
    (vector? item) (mapv #(traverse-and-transform % setStore) item)
    (map? item)  (let [ident (u/get-ident item)
                       new-val (zipmap (keys item) (mapv #(traverse-and-transform % setStore) (vals item)))]
                   (if (ident? ident)
                     (do
                       #_(consola.debug "try add: " ident " " new-val)
                       (swap! setStore #(update-in % ident (fn [v] (merge v new-val))))
                       ident)
                     new-val))

    :else item))

;; stable, per-table accumulator
(defn- normalize* [x]
  (let [acc (js-obj)]
    (letfn [(put! [table id m]
              (let [id    (str id)             ;; **coerce id to string** to avoid 1 vs "1"
                    tbl   (or (aget acc table) (js-obj))
                    prev  (aget tbl id)
                    row   (js/Object.assign (js-obj) prev m)]
                (aset tbl id row)
                (aset acc table tbl)))
            (walk [v]
              (cond
                (vector? v) (mapv walk v)
                (map? v)
                (let [ident (u/get-ident v)    ;; returns [:comp/id 1] etc.
                      m     (reduce-kv (fn [o k val] (assoc o k (walk val))) {} v)]
                  (if (and (vector? ident) (= 2 (count ident)))
                    (do (put! (first ident) (second ident) m) ident)
                    m))
                :else v))]
      (let [root* (walk x)]
        [acc root*]))))

(defn add [{:keys [setStore store] :as ctx} & data]
  (when-let [input (first data)]
    (let [[acc root*] (normalize* input)]
      (batch
        (fn []
          (doseq [t (js/Object.keys acc)]
            (let [rows (aget acc t)]
              ;; 1) ensure table exists once (no copy of big table)
              (setStore t (fn [tbl] (or tbl #js {})))
              ;; 2) merge each id only (tiny copy of that row)
              (doseq [id (js/Object.keys rows)]
                (let [row (aget rows id)]
                  ;; either Object.assign:
                  #_(setStore t id (fn [prev] (js/Object.assign #js {} prev row)))
                  ;; or, if you prefer Solid's reconciler per row:
                  (setStore t id (reconcile row #js {:merge true}))))))))
      root*)))

#_(defn add [{:keys [setStore] :as ctx} & data]
  (when-let [input (first data)]
    (let [[acc root*] (normalize* input)]
      (batch
        (fn []
          (doseq [t (js/Object.keys acc)
                  :let [rows (aget acc t)]]
            ;; create-or-merge the whole table atomically
            (setStore t (reconcile rows #js {:merge true})))))
      root*)))

#_(defn add [{:keys [store setStore] :as ctx} & data]
  (let [input (or (first data) store)
        [acc root*] (normalize* input)]
    (batch
      (fn []
        (let [tables (js/Object.keys acc)]
          
          ;; Commit table by table
          (doseq [t tables
                  :let [rows (aget acc t)]]
            ;; commit each id for finer invalidation; or merge the whole table at once
            (doseq [id (js/Object.keys rows)]
              (let [row (aget rows id)]
                (setStore t id (fn [prev] (js/Object.assign (js-obj) prev row))))))))
      )
    root*))


(def acc (atom {}))

#_(defn add [{:keys [store setStore] :as ctx} & data]
  (let [res (batch #(traverse-and-transform (or (first data) store) acc))]
    (consola.debug "rr: " res)
    (consola.debug "rr:acc " @acc)

    ;; (mapv (fn [v] (consola.debug " v " v) (setStore (first v) (reconcile (second v) {:merge true}))) @acc)
    ;; (mapv (fn [v] (consola.debug " v " v) (setStore (first v) (reconcile (second v) {:merge true}))) res)

    (if-not (first data)
      (do (consola.debug "merge-data: " (merge-with merge res @acc)) (setStore (reconcile (merge-with merge res @acc))))
      (reduce-kv (fn [m k v] (consola.debug "set: " k " " v) #_(consola.debug "set2: " (merge-with merge (get store k) v #_{:key k :merge true})) (setStore k #(merge-with merge % v #_{:key k :merge true})))
                 {}
                 (if-not (vector? res)
                   (merge  @acc res)
                   @acc))
      #_(do
          (mapv (reconcile (merge-with merge res)))
          (mapv #(if (nil? (get store %))
                   (setStore % (fn [x] (get @acc %)))
                   (setStore % (fn [x] (merge-with merge x (get @acc %))))) (keys @acc)))
      #_(if (ident? res)
          (if (nil? (get-in store res))
            (setStore (first res) (fn [x] (merge x (get @acc (first res)))))
            (setStore (first res) (second res) (fn [x] (merge x (get-in @acc res)))))
          #_(keys @acc)
          #_{:merge true
             :key (first res)} #_(reconcile (get-in @acc res) {:merge true
                                                               :key (second res)})
          (setStore (fn [x] (merge x @acc)))
          #_(merge res @acc)))
    (reset! acc {})
    #_(js/chrome.runtime.sendMessage {:action "updateData" :data store})
    #_(consola.debug "store" store)
    res))

#_(defn pull [store entity query]
  (cond
    (or (nil? entity) (not entity)  (empty? entity)) entity

    (or (ident? entity) (ident? [(first entity) (second entity)])) (pull store (get-in store entity) query)

    (and (> (count entity) 0)
         (vector? entity)) (mapv (fn [x] (if (ident? x)
                                           (pull store x query)
                                           x)) entity)

    (and (> (count query) 1)
         (vector? query)) (let [simple-keys (filterv string? query)
                                not-simple  (filterv #(not (string? %)) query)]
                            (into (zipmap simple-keys (mapv #(pull store entity %) simple-keys))
                                  (mapv #(pull store entity %) not-simple)))

    (and (= (count query) 1)
         (vector? query)) (pull store entity (first query))

    (map? query) (let [nk (first (keys query))
                       sub-query (get query nk)]
                   (when-let [data (get entity nk)]
                     #_(consola.debug "-------------:" nk ": " data sub-query)
                     {nk (if (ident? data)
                           (pull store data sub-query)
                           (mapv #(pull store % sub-query) data))}))

    :else (get entity query)))


(defn- resolve-ident [st ident]
  (if (ident? ident) (get-in st ident) ident))

(defn- pull-one [st entity query]
  (cond
    (nil? entity) nil

    (ident? entity)
    (pull-one st (get-in st entity) query)

    (vector? entity)
    (mapv #(if (ident? %) (pull-one st % query) %) entity)

    (vector? query)
    (let [simple (filterv string? query)
          nested (filterv #(map? %) query)
          base   (into {} (map (fn [k] [k (pull-one st entity k)]) simple))]
      (reduce
        (fn [m mquery]
          (let [k (first (keys mquery))
                subq (get mquery k)
                v (get entity k)]
            (if (nil? v)
              m
              (assoc m k (if (ident? v)
                           (pull-one st v subq)
                           (mapv #(pull-one st % subq) v))))))
        base nested))


    (map? query)
    (let [nk (first (keys query))
          subq (get query nk)]
      (when-let [data (get entity nk)]
        {nk (cond
              (ident? data)  ;; follow ident
              (pull store (get-in store data) subq)

              (vector? data) ;; maybe a collection of idents
              (mapv #(if (ident? %)
                       (pull store (get-in store %) subq)
                       (pull store % subq))
                    data)

          :else
          (pull store data subq))}))

    (string? query)
    (get entity query)

    :else entity))

#_(defn pull [store entity query]
  (pull-one store entity query))


;; helpers
(def ^:private id-suffix "/id")
(defn ident? [x]
  (and (vector? x) (= (count x) 2)
       (let [k (aget x 0)] (and (string? k) (.endsWith k id-suffix)))))

(defn- resolve-entity [store entity]
  ;; Accepts:
  ;; - map (already resolved)
  ;; - ident [:foo/id "1"] -> get-in store
  ;; - path [:foo/id "1" :foo/bar ...] -> get-in from resolved base
  ;; - vector of idents -> leave as-is (handled later)
  (cond
    (nil? entity) nil

    ;; path: first 2 look like ident, rest are nested fields
    (and (vector? entity)
         (>= (count entity) 2)
         (let [k (aget entity 0)] (and (string? k) (.endsWith k id-suffix))))
    (let [tbl  (aget entity 0)
          id   (aget entity 1)
          base (get-in store [tbl id])
          rest (when (> (.-length entity) 2)
                 (.slice entity 2))]
      (if (and rest (> (.-length rest) 0))
        (get-in base rest)
        base))

    ;; ident of length 2
    (ident? entity)
    (get-in store entity)

    :else entity))

(defn pull [store entity query]
  (let [e (resolve-entity store entity)]
    (cond
      (nil? e) nil

      ;; NEW: if the resolved value is an ident vector, follow it first
      (ident? e)
      (pull store (get-in store e) query)

      ;; If the resolved value is a vector of idents/maps, map with same query
      (and (vector? e) (pos? (.-length e)))
      (mapv (fn [it] (if (ident? it) (pull store it query) (pull store it query))) e)

      ;; Vector query: [:a :b {:c [:d]}]
      (vector? query)
      (let [simple (vec (filter string? query))
            nested (vec (filter (fn [x] (not (string? x))) query))
            out    (reduce (fn [m k] (assoc m k (get e k))) {} simple)]
        (reduce
          (fn [m mquery]
            (let [k   (first (js/Object.keys mquery))
                  sub (aget mquery k)
                  v   (get e k)]
              (if (nil? v)
                m
                (assoc m k
                       (cond
                         (ident? v)   (pull store v sub)
                         (vector? v)  (mapv (fn [vv]
                                              (if (ident? vv)
                                                (pull store vv sub)
                                                (pull store vv sub))) v)
                         :else        (pull store v sub))))))
          out nested))

      ;; Map query: {:k subquery} -> return wrapped {k ...}
      (and (map? query) (pos? (.-length (js/Object.keys query))))
      (let [k   (first (js/Object.keys query))
            sub (aget query k)
            v   (get e k)]
        (when (some? v)
          {k (cond
               (ident? v)   (pull store v sub)
               (vector? v)  (mapv (fn [vv]
                                    (if (ident? vv)
                                      (pull store vv sub)
                                      (pull store vv sub))) v)
               :else        (pull store v sub))}))

      ;; Scalar key
      (string? query) (get e query)

      :else e)))

(declare update-uuid-in-map)

(defn update-uuid-in-coll [coll old-uuid new-uuid]
  (mapv (fn [item]
          (cond
            (map? item) (update-uuid-in-map item old-uuid new-uuid)
            (vector? item) (if (= (second item) old-uuid)
                             (assoc item 1 new-uuid)
                             (update-uuid-in-coll item old-uuid new-uuid))
            :else item))
        coll))

(defn update-uuid-in-map [m old-uuid new-uuid]
  (reduce-kv (fn [acc k v]
               (cond
                 (map? v) (assoc acc k (update-uuid-in-map v old-uuid new-uuid))
                 (vector? v) (assoc acc k (update-uuid-in-coll v old-uuid new-uuid))
                 (and (vector? v) (= (second v) old-uuid)) [k new-uuid]
                 (= v old-uuid) [k new-uuid]
                 :else (assoc acc k v)))
             {}
             m))

(defn swap-uuids! [{:keys [store setStore] :as ctx} old-uuid new-id]
  (setStore (fn [state]
              (update-uuid-in-map state old-uuid new-id))))

(defn unwrap-proxy [data]
  (cond
    (array? data) (vec (map unwrap-proxy data))

    (vector? data) (mapv unwrap-proxy data)

    (map? data) (into {} (map (fn [[k v]] [k (unwrap-proxy v)]) data))

    (object? data) (into {} (map (fn [[k v]] [k (unwrap-proxy v)]) data))

    :else data))
