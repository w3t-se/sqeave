(ns test.large-pull
  (:require ["vitest" :refer [describe test expect]]
            ["node:fs" :as fs]
            ["solid-js/store" :refer [createStore]]
            ["consola/browser" :refer [consola]]
            ["@w3t-ab/sqeave/normad" :as n]))

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

(defn assert-not-ident! [v msg]
  (when (n/ident? v)
    (throw (js/Error. (str msg " — got ident " (pr-str v))))))

(def RUN_BENCH
  (boolean (or (aget js/process "env" "RUN_BENCH")
               (aget (.-env js/import.meta) "RUN_BENCH"))))

(def JSON_PATH
  (or (aget js/process "env" "LARGE_DB_JSON")
      "./dist/src/test/w3t-ab/sqeave/data/large_db.json"))

(describe "large pull"
  (test "correctness: dashboard -> queries -> datasource resolved"
    (fn []
      (let [{:keys [store]} (make-ctx-from-json JSON_PATH)

            ;; start from FILE and jump to its dashboard entity
            entity  #js [:file/id "64c81bc9-5836-4186-9c0e-3a5a76762782" :file/dashboard]

            ;; Nested pull: dashboard -> queries -> each query's datasource (resolve ident!)
            query   #js [:dashboard/id
                         :dashboard/name
                         #js {:queries #js [:query/id
                                           :query/name
                                           #js {:query/datasource #js [:datasource/id :datasource/name]}]}]

            res     (n/pull store entity query)]

        ;; pull returned something
        (.not.toBeNull (expect res))
        (.not.toBeUndefined (expect res))

        ;; dashboard shape
        (doseq [k ["dashboard/id" "queries"]]
          (.not.toBeNull (expect (get res k)))
          (.not.toBeUndefined (expect (get res k))))

        ;; queries is vector, datasource is map (not ident)
        (let [qs (get res "queries")]
          (.toBe (expect (vector? qs)) true)

          (doseq [q-ent qs]
            (.not.toBeNull (expect (get q-ent "query/id")))
            (.not.toBeUndefined (expect (get q-ent "query/id")))

            (let [ds (get q-ent "query/datasource")]
              (assert-not-ident! ds "datasource was not resolved")
              (.not.toBeNull (expect (get ds "datasource/id")))
              (.not.toBeUndefined (expect (get ds "datasource/id")))
              (.not.toBeNull (expect (get ds "datasource/name")))
              (.not.toBeUndefined (expect (get ds "datasource/name"))))))

        (consola.success "✅ large pull correctness OK"))))

  (test "bench: pull x2000"
    (fn []
      (when RUN_BENCH
        (let [{:keys [store]} (make-ctx-from-json JSON_PATH)
              entity  #js [:file/id "64c81bc9-5836-4186-9c0e-3a5a76762782" :file/dashboard]
              query   #js [:dashboard/id
                           :dashboard/name
                           #js {:queries #js [:query/id
                                             :query/name
                                             #js {:query/datasource #js [:datasource/id :datasource/name]}]}]
              reps    2000
              t0      (now-ms)]
          (dotimes [tmp reps]
            (n/pull store entity query))
          (let [t1  (now-ms)
                dt  (- t1 t0)
                per (/ dt reps)]
            (consola.info
              (str "⏱  pull x" reps " took " (js/Math.round dt) " ms ("
                   (.toFixed per 3) " ms/call)"))
            ;; keep Vitest happy that the test actually ran
            (.toBeGreaterThanOrEqual (expect dt) 0)))))))
