(ns normad
  (:require ["./utils.mjs" :as u]
            ["consola/browser" :refer [consola]]
            ["solid-js/store" :refer [reconcile]]))

(defn ident? [data]
  (if (vector? data)
    (if (string? (first data))
      (and
       (re-find #"/id$" (first data))
       (= (count data) 2)
       (or (number? (second data)) (string? (second data)) (undefined? (second data))))
      false)
    false))

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

(def acc (atom {}))

(defn add [{:keys [store setStore] :as ctx} & data]
  (let [res (traverse-and-transform (or (first data) store) acc)]
    (consola.debug "rr: " res)
    (consola.debug "rr:acc " @acc)

    ;; (mapv (fn [v] (consola.debug " v " v) (setStore (first v) (reconcile (second v) {:merge true}))) @acc)
    ;; (mapv (fn [v] (consola.debug " v " v) (setStore (first v) (reconcile (second v) {:merge true}))) res)

    (if-not (first data)
      (do (consola.debug "merge-data: " (merge-with merge res @acc)) (setStore (reconcile (merge-with merge res @acc))))
      (reduce-kv (fn [m k v] (consola.debug "set: " k " " v) (consola.debug "set2: " (merge-with merge (get store k) v #_{:key k :merge true}) ) (setStore k #(merge-with merge % v #_{:key k :merge true})))
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

(defn pull [store entity query]
  (cond
    (ident? entity) (pull store (get-in store entity) query)

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
