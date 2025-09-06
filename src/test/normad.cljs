
(ns normad
  (:require ["solid-js" :as solid]
            ["consola/browser" :refer [consola]]
            ["../main/w3t-ab/sqeave/comp.mjs" :as comp]
            ["../main/w3t-ab/sqeave/normad.mjs" :as n]))

(set! (.-env js/import.meta) #js {:DEV true})

;; +++ UPDATE: real ctx from Solid createStore via your init-ctx!
(defn make-ctx []
  (comp/init-ctx! (solid/createContext)))

;; -----------------------
;; Deep structural equality (arrays/objects/primitive)
;; -----------------------

(defn ^boolean plain-object? [x]
  (and (some? x)
       (= (.-constructor x) (.-constructor #js {}))))

(defn deepEqual [a b]
  (cond
    (identical? a b) true
    ;; Handle NaN
    (and (number? a) (number? b) (js/isNaN a) (js/isNaN b)) true
    ;; Primitives / functions
    (or (string? a) (number? a) (boolean? a) (nil? a) (undefined? a) (fn? a))
    (= a b)

    ;; Arrays
    (and (array? a) (array? b))
    (let [len (.-length a)]
      (when-not (= len (.-length b)) false)
      (loop [i 0]
        (if (< i len)
          (if (deepEqual (aget a i) (aget b i))
            (recur (inc i))
            false)
          true)))

    ;; Plain objects
    (and (plain-object? a) (plain-object? b))
    (let [ka (js/Object.keys a)
          kb (js/Object.keys b)]
      (when-not (= (.-length ka) (.-length kb)) false)
      (loop [i 0]
        (if (< i (.-length ka))
          (let [k (aget ka i)]
            (if (and (js/Object.hasOwn b k)
                     (deepEqual (aget a k) (aget b k)))
              (recur (inc i))
              false))
          true)))

    ;; Fallback strict equality
    :else (= a b)))

(defn show [x]
  (try
    (js/JSON.stringify x nil 2)
    (catch js/Error _ (str x))))

(defn assert-deep= [label got expect]
  (when-not (deepEqual got expect)
    (throw (js/Error. (str "ASSERT FAIL: " label
                           "\n  got:\n"    (show got)
                           "\n  expect:\n" (show expect))))))

(defn now-ms [] (.now js/Date))

;; -----------------------
;; Solid-like store mock for setStore call shapes
;; -----------------------

#_(defn make-ctx []
  (let [store-atom (atom #js {})]
    (letfn [(setStore
              ;; (setStore f)
              ([f] (swap! store-atom #(f %)))
              ;; (setStore k f)
              ([k f]
               (swap! store-atom
                      (fn [st]
                        (let [cur (or (aget st k) #js {})]
                          (aset st k (f cur))
                          st))))
              ;; (setStore k id f)
              ([k id f]
               (swap! store-atom
                      (fn [st]
                        (let [tbl (or (aget st k) #js {})
                              cur (or (aget tbl id) #js {})]
                          (aset tbl id (f cur))
                          (aset st k tbl)
                          st)))))]
      #js {:store store-atom
           :setStore setStore})))

;; If your real code expects n/get-ident, provide a minimal one for tests
#_(defn get-ident [m]
  (when (plain-object? m)
    (let [ks (js/Object.keys m)
          ln (.-length ks)]
      (loop [i 0]
        (if (< i ln)
          (let [k (aget ks i)]
            (if (.endsWith k "/id")
              #js [k (aget m k)]
              (recur (inc i))))
          nil)))))

;; Patch for tests if normad expects it here
#_(set! n/get-ident get-ident)

;; -----------------------
;; Fixtures
;; -----------------------

(def user-1 #js {:user/id 1 :user/name "A"})
(def user-1b #js {:user/id 1 :user/email "a@example.com"})
(def user-2 #js {:user/id "u2" :user/name "B"})

(def org-10
  #js {:organization/id 10
       :organization/name "Org10"
       :organization/users #js [user-1 user-2]})

;; -----------------------
;; Correctness tests (use deep equality)
;; -----------------------

(defn test-single-entity []
  (let [ctx (make-ctx)
        res (n/add ctx user-1)
        store (:store ctx)]
    ;; expect ident vector like [:user/id 1] in Squint => JS array
    (assert-deep= "return ident" res #js [:user/id 1])
    (assert-deep= "user name"
                  (aget (aget store :user/id) 1) #js {:user/id 1 :user/name "A"})
    (consola.success "✅ test-single-entity passed")))

(defn test-merge-same-id []
  (let [ctx (make-ctx)]
    (n/add ctx user-1)
    (n/add ctx user-1b)
    (let [row (aget (aget (.-store ctx) :user/id) 1)]
      (assert-deep= "merged row"
        row #js {:user/id 1 :user/name "A" :user/email "a@example.com"})
      (consola.success "✅ test-merge-same-id passed"))))

(defn test-nested-refs []
  (let [ctx (make-ctx)
        res (n/add ctx org-10)
        store (:store ctx)]
    ;; top entity exists
    (assert-deep= "org row"
      (aget (aget store :organization/id) 10)
      #js {:organization/id 10
           :organization/name "Org10"
           :organization/users #js [#js [:user/id 1] #js [:user/id "u2"]]})
    ;; users normalized
    (assert-deep= "user 1 row"
      (aget (aget store :user/id) 1)
      #js {:user/id 1 :user/name "A"})
    (assert-deep= "user 2 row"
      (aget (aget store :user/id) "u2")
      #js {:user/id "u2" :user/name "B"})
    ;; return value is ident
    (assert-deep= "return ident org" res #js [:organization/id 10])
    (consola.success "✅ test-nested-refs passed")))

(defn test-add-without-arg-safe []
  (let [ctx (make-ctx)]
    (n/add ctx user-1)
    (n/add ctx user-2)
    (let [res (n/add ctx)]
      ;; Just ensure it doesn't throw and result is something (nil / {} / ident / vec)
      (consola.success (str "✅ test-add-without-arg-safe returned: " (show res))))))

;; -----------------------
;; Micro benchmarks
;; -----------------------

(defn bench-add [n0]
  (let [ctx (make-ctx)
        t0  (now-ms)]
    (dotimes [i n0]
      (n/add ctx {:user/id i
                  :user/name (str "U" i)
                  :user/email (str "u" i "@x.test")}))
    (let [ms  (- (now-ms) t0)
          ops (Math/round (* 1000 (/ n0 (max 1 ms))))
          size (.-length (js/Object.keys (aget (:store ctx) :user/id)))]
      (consola.info (str "add x" n0 " -> " ms " ms (" ops " ops/sec), rows=" size))
      #js {:ms ms :ops ops :rows size})))

(defn bench-nested [n0]
  (let [ctx (make-ctx)
        t0  (now-ms)]
    (dotimes [i n0]
      (n/add ctx #js {:organization/id i
                      :organization/name (str "O" i)
                      :organization/users
                      (clj->js (map (fn [j] {:user/id (+ (* i 10) j)
                                             :user/name (str "U" i "-" j)})
                                    (range 5)))}))
    (let [ms  (- (now-ms) t0)
          ops (Math/round (* 1000 (/ n0 (max 1 ms))))
          uct (.-length (js/Object.keys (aget (:store ctx) :user/id)))]
      (consola.info (str "add (nested) x" n0 " -> " ms " ms (" ops " orgs/sec), users=" uct))
      #js {:ms ms :ops ops :users uct})))


;; -----------------------
;; NEW: pull round-trip test
;; -----------------------

(defn test-pull-roundtrip []
  (let [ctx   (make-ctx)
        ;; seed nested graph
        _     (n/add ctx org-10)  ;; {:organization/id 10 ... users [user-1 user-2]}
        store (:store ctx)
        ;; Pull back a shaped view
        out   (n/pull store #js [:organization/id 10]
                      #js [:organization/name
                           #js { :organization/users #js [:user/id :user/name]}])]
    ;; Expect the pulled shape to contain resolved users (not idents) for requested keys
    (assert-deep= "pull round-trip"
      out
      #js {:organization/name "Org10"
           :organization/users #js [#js {:user/id 1   :user/name "A"}
                                    #js {:user/id "u2" :user/name "B"}]})
    (consola.success "✅ test-pull-roundtrip passed")))

;; -----------------------
;; NEW: pull benchmark
;; -----------------------

(defn bench-pull [n0]
  ;; Build a store with many orgs & users, then measure pulls/s
  (let [ctx (make-ctx)]
    ;; seed
    (dotimes [i n0]
      (n/add ctx #js {:organization/id i
                      :organization/name (str "O" i)
                      :organization/users
                      (map (fn [j] {:user/id (+ (* i 10) j)
                                    :user/name (str "U" i "-" j)})
                           (range 5))}))
    (let [store (:store ctx)
          t0 (now-ms)]
      ;; do N pulls over random org ids
      (dotimes [i n0]
        (let [oid (mod (* 1103515245 i) n0)] ; cheap LCG-ish spread
          (n/pull store #js [:organization/id oid]
                  #js [:organization/name
                       #js { :organization/users #js [:user/id :user/name]}])))
      (let [ms  (- (now-ms) t0)
            ops (Math/round (* 1000 (/ n0 (max 1 ms))))]
        (consola.info (str "pull x" n0 " -> " ms " ms (" ops " pulls/sec)"))
        #js {:ms ms :ops ops :pulls n0}))))

;; -----------------------
;; Runner
;; -----------------------

(defn run-tests! []
  (try
    (test-single-entity)
    (test-merge-same-id)
    (test-nested-refs)
    (test-add-without-arg-safe)
    (test-pull-roundtrip)
    
    (consola.start "🏁 correctness OK — running perf…")
    (bench-add 10000)
    (bench-nested 1000)
    (bench-pull 5000)
    (consola.success "✅ all tests done")
    (catch js/Error e
      (consola.error e)
      (throw e))))

(run-tests!)
