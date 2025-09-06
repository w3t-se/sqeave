(ns test.large-pull
  (:require ["node:fs" :as fs]
            ["solid-js/store" :refer [createStore]]
            ["consola/browser" :refer [consola]]
            ["../main/w3t-ab/sqeave/normad.mjs" :as n]))  ;; adjust path to where `pull`/`ident?` live

(defn read-json [path]
  (-> (.readFileSync fs path "utf-8")
      (js/JSON.parse)))

(defn now-ms []
  (if (exists? js/performance)
    (.now js/performance)
    (js/Date.now)))

(defn make-ctx-from-json [path]
  (let [data (read-json path)
        [store setStore] (createStore data)]
    {:store store :setStore setStore}))

(defn assert-not-ident [v msg]
  (when (n/ident? v)
    (throw (js/Error. (str msg " — got ident " (pr-str v))))))

(defn test-large-pull-and-bench
  "Load a large JSON DB, run a nested pull, assert correctness, then benchmark.
   `json-path` -> path to the JSON file shown in the prompt."
  [json-path]
  (let [{:keys [store]} (make-ctx-from-json json-path)
        ;; we start from the FILE and jump (via path) straight into its dashboard entity
        entity  [:file/id "64c81bc9-5836-4186-9c0e-3a5a76762782" :file/dashboard]
        ;; Nested pull: dashboard -> queries -> each query's datasource (resolve ident!)
        query   [:dashboard/id
                 :dashboard/name
                 {:queries [:query/id
                            :query/name
                            {:query/datasource [:datasource/id :datasource/name]}]}]
        res     (n/pull store entity query)]

    ;; --- correctness checks ---
    (when (nil? res)
      (throw (js/Error. "pull returned nil")))
    ;; dashboard shape
    (doseq [k ["dashboard/id" "queries"]]
      (when (nil? (get res k))
        (throw (js/Error. (str "missing key in dashboard: " k)))))

    ;; queries should be a vector of maps; their datasources should be maps (not idents)
    (let [qs (get res "queries")]
      (when-not (vector? qs)
        (throw (js/Error. (str "queries is not a vector: " (pr-str (type qs))))))
      (doseq [q-ent qs]
        (when (nil? (get q-ent "query/id"))
          (throw (js/Error. "query missing :query/id")))
        (let [ds (get q-ent "query/datasource")]
          (assert-not-ident ds "datasource was not resolved")
          (when (or (nil? (get ds "datasource/id"))
                    (nil? (get ds "datasource/name")))
            (throw (js/Error. (str "datasource missing fields: " (pr-str ds))))))))

    (consola.success "✅ large pull correctness OK")

    ;; --- benchmark ---
    (let [reps 2000
          t0   (now-ms)]
      (dotimes [_ reps]
        (n/pull store entity query))
      (let [t1 (now-ms)
            dt (- t1 t0)
            per (/ dt reps)]
        (consola.info (str "⏱  pull x" reps " took " (js/Math.round dt) " ms (" (.toFixed per 3) " ms/call)"))))

    res))

;; Example usage from a test runner or REPL:
(test-large-pull-and-bench "./large_db.json")
