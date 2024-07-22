(ns normad
  (:require ["./utils.cljs" :as u]
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
                       #_(println "try add: " ident " " new-val)
                       (swap! setStore #(update-in % ident (fn [v] (merge v new-val))))
                       ident)
                     new-val))

    :else item))

(def acc (atom {}))

(defn add [{:keys [store setStore] :as ctx} & data]
  (let [res (traverse-and-transform (or (first data) store) acc)]
    (println "rr: " res)
    (println "rr:acc " @acc)

    ;; (mapv (fn [v] (println " v " v) (setStore (first v) (reconcile (second v) {:merge true}))) @acc)
    ;; (mapv (fn [v] (println " v " v) (setStore (first v) (reconcile (second v) {:merge true}))) res)

    (if-not (first data)
      (setStore (reconcile (merge-with merge res @acc)))
      (reduce-kv (fn [m k v] (println "set: " k " " v ) (setStore k #(merge-with merge % v #_{:key k :merge true})))
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
    #_(println "store" store)
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
                       data (get entity nk)]
                   {nk (pull store data (vals query))})

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
