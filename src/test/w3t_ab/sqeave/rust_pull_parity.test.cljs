(ns test.rust-pull-parity
  (:require ["vitest" :refer [describe test expect]]
            ["node:fs" :as fs]
            ["solid-js" :as solid]
            ["@w3t-ab/sqeave/comp" :as comp]
            ["solid-js/store" :refer [createStore]]
            ["consola/browser" :refer [consola]]
            ["@w3t-ab/sqeave/normad" :as n]
            ["../../../../../pkg/seebra_state_wasm.js" :as wasm]
            ))

(defn make-ctx []
  (comp/init-ctx! (solid/createContext)))

(def WASM_PATH
  "./pkg/seebra_state_wasm_bg.wasm")

(defn read-json [path]
  (-> (.readFileSync fs path "utf-8")
      (js/JSON.parse)))

(defn now-ms []
  (if (exists? js/performance)
    (.now js/performance)
    (js/Date.now)))

(def RUN_BENCH
  (boolean (or (aget js/process "env" "RUN_BENCH")
               (aget (.-env js/import.meta) "RUN_BENCH"))))

(def JSON_PATH
  (or (aget js/process "env" "LARGE_DB_JSON")
      "./dist/src/test/w3t-ab/sqeave/data/large_db.json"))

(def entity
   [:file/id "64c81bc9-5836-4186-9c0e-3a5a76762782"])

(def query
  [:file/id
   {:file/dashboard
    [:dashboard/id :dashboard/name
     {:datasources [:datasource/id :datasource/name :datasource/node]}]}])

(defn make-sqeave-store [data]
  (let [[store setStore] (createStore data)]
    {:store store :setStore setStore}))

(defn json-roundtrip [x]
  (-> x js/JSON.stringify js/JSON.parse))

(defn normalize-for-compare [x]
  ;; Removes Solid proxies / cljs wrappers.
  (json-roundtrip x))

(defn assert-same-json! [a b]
  (.toEqual (expect a) b))

(defonce wasm-ready (atom nil))

(defn ensure-wasm! []
  (or @wasm-ready
      (let [bytes (.readFileSync fs WASM_PATH)
            p ((.-default wasm) bytes)]
        (reset! wasm-ready p)
        p)))

(def user-1   {:user/id 1 :user/name "A"})
(def user-1b  {:user/id 1 :user/email "a@example.com"})
(def user-2   {:user/id "u2" :user/name "B"})

(def org-10
   {:organization/id 10
    :organization/name "Org10"
    :organization/users  [user-1]})


(defn make-rust-state! []
  (-> (ensure-wasm!)
      (.then
       (fn []
         (let [st (new (.-SeebraState wasm))]
           #_(.add st data)
           st)))))

(describe "rust pull parity"
          (test "correctness: Sqeave add == Rust/WASM add"
                (fn []
                  (let [
                        ctx (make-ctx)
                        _ (n/add ctx org-10)]
                    (-> (make-rust-state!)
                        (.then
                         (fn [rust-state]
                           (.add rust-state org-10)
                           (assert-same-json! (:store ctx) (.snapshot rust-state))
                           (consola.success "✅ Sqeave/Rust add parity OK")))))))
          
          (test "correctness: Sqeave pull == Rust/WASM pull"
                (fn []
                  (let [data (read-json JSON_PATH)
                        {:keys [store]} (make-sqeave-store data)
                        sqeave-res (n/pull (:store store) entity query)]
                    (-> (make-rust-state!)
                        (.then
                         (fn [rust-state]
                           (.replace_state rust-state data)
                           (let [rust-res (.pull rust-state entity query)]
                             (assert-same-json! sqeave-res (json-roundtrip rust-res))
                             (consola.success "✅ Sqeave/Rust pull parity OK"))))))))

          (test "bench: Sqeave pull vs Rust/WASM pull x2000"
                (fn []
                  (when RUN_BENCH
                    (let [data (read-json JSON_PATH)
                          {:keys [store]} (make-sqeave-store data)
                          reps 2000]
                      (-> (make-rust-state! data)
                         (.then
                          (fn [rust-state]
                            ;; Warmup
                            (dotimes [_ 50]
                              (n/pull store entity query)
                              (.pull rust-state entity query))

                            (let [t0 (now-ms)]
                              (dotimes [_ reps]
                                (n/pull store entity query))
                              (let [t1 (now-ms)
                                    sqeave-dt (- t1 t0)
                                    t2 (now-ms)]
                                (dotimes [_ reps]
                                  (.pull rust-state entity query))
                                (let [t3 (now-ms)
                                      rust-dt (- t3 t2)]
                                  (println
                                   (str "⏱ Sqeave pull x" reps ": "
                                        (js/Math.round sqeave-dt) " ms ("
                                        (.toFixed (/ sqeave-dt reps) 3) " ms/call)"))

                                  (println
                                   (str "⏱ Rust/WASM pull x" reps ": "
                                        (js/Math.round rust-dt) " ms ("
                                        (.toFixed (/ rust-dt reps) 3) " ms/call)"))

                                  (.toBeGreaterThanOrEqual (expect sqeave-dt) 0)
                                  (.toBeGreaterThanOrEqual (expect rust-dt) 0))))))))))))
