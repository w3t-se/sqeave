(ns normad.vitest-test
  (:require ["vitest" :refer [describe test expect]]
            ["solid-js" :as solid]
            ["@w3t-ab/sqeave/comp" :as comp]
            ["@w3t-ab/sqeave/normad" :as n]))

#_(set! (.-env js/import.meta)  {:DEV true})

;; +++ UPDATE: real ctx from Solid createStore via your init-ctx!
(defn make-ctx []
  (comp/init-ctx! (solid/createContext)))

;; -----------------------
;; Fixtures
;; -----------------------

(def user-1   {:user/id 1 :user/name "A"})
(def user-1b  {:user/id 1 :user/email "a@example.com"})
(def user-2   {:user/id "u2" :user/name "B"})

(def org-10
   {:organization/id 10
       :organization/name "Org10"
       :organization/users  [user-1 user-2]})

;; -----------------------
;; Vitest tests
;; -----------------------

(describe "normad"
  (test "single entity: returns ident and stores row"
    #(let [ctx   (make-ctx)
          res   (n/add ctx user-1)
          store (:store ctx)]
      (.toEqual (expect res)  [:user/id 1])
      (.toEqual (expect (aget (aget store :user/id) 1))
                 {:user/id 1 :user/name "A"})))

  (test "merge same id"
    #(let [ctx (make-ctx)]
      (n/add ctx user-1)
      (n/add ctx user-1b)
      (let [row (get-in (.-store ctx) [:user/id 1])]
        (.toEqual (expect row)
                   {:user/id 1 :user/name "A" :user/email "a@example.com"}))))

  (test "nested refs normalize users and store org with ident refs"
    #(let [ctx   (make-ctx)
          res   (n/add ctx org-10)
          store (:store ctx)]
      (.toEqual (expect (get-in store [:organization/id 10]))
                 {:organization/id 10
                  :organization/name "Org10"
                  :organization/users  [[:user/id 1] [:user/id "u2"]]})

      (.toEqual (expect (aget (aget store :user/id) 1))
                 {:user/id 1 :user/name "A"})

      (.toEqual (expect (aget (aget store :user/id) "u2"))
                 {:user/id "u2" :user/name "B"})

      (.toEqual (expect res)  [:organization/id 10])))

  (test "add without arg is safe (does not throw)"
    #(let [ctx (make-ctx)]
      (n/add ctx user-1)
      (n/add ctx user-2)
      (.toThrow (.-not (expect (fn [] (n/add ctx)))))))

  (test "pull round-trip resolves requested nested shape"
    #(let [ctx   (make-ctx)
          _     (n/add ctx org-10)
          store (:store ctx)
          out   (n/pull store
                         [:organization/id 10]
                         [:organization/name
                          {:organization/users [:user/id :user/name]}])]
      (.toEqual (expect out)
                 {:organization/name "Org10"
                  :organization/users [{:user/id 1 :user/name "A"}
                                       {:user/id "u2" :user/name "B"}]}))))

;; -----------------------
;; Optional benches as Vitest tests (gate with env var)
;; RUN_BENCH=1 vitest
;; -----------------------

(defn now-ms [] (.now js/Date))

(def RUN_BENCH
  (boolean (or (aget js/process "env" "RUN_BENCH")
               (aget (.-env js/import.meta) "RUN_BENCH"))))

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
       {:ms ms :ops ops :rows size})))

(defn bench-nested [n0]
  (let [ctx (make-ctx)
        t0  (now-ms)]
    (dotimes [i n0]
      (n/add ctx  {:organization/id i
                      :organization/name (str "O" i)
                      :organization/users
                      (clj->js (map (fn [j] {:user/id (+ (* i 10) j)
                                             :user/name (str "U" i "-" j)})
                                    (range 5)))}))
    (let [ms  (- (now-ms) t0)
          ops (Math/round (* 1000 (/ n0 (max 1 ms))))
          uct (.-length (js/Object.keys (aget (:store ctx) :user/id)))]
       {:ms ms :ops ops :users uct})))

(defn bench-pull [n0]
  (let [ctx (make-ctx)]
    (dotimes [i n0]
      (n/add ctx  {:organization/id i
                      :organization/name (str "O" i)
                      :organization/users
                      (map (fn [j] {:user/id (+ (* i 10) j)
                                    :user/name (str "U" i "-" j)})
                           (range 5))}))
    (let [store (:store ctx)
          t0    (now-ms)]
      (dotimes [i n0]
        (let [oid (mod (* 1103515245 i) n0)]
          (n/pull store  [:organization/id oid]
                  [:organization/name {:organization/users [:user/id :user/name]}])))
      (let [ms  (- (now-ms) t0)
            ops (Math/round (* 1000 (/ n0 (max 1 ms))))]
         {:ms ms :ops ops :pulls n0}))))

(describe "bench"
  (test "bench add"
    #(when RUN_BENCH
      (let [r (bench-add 10000)]
        (.toBeGreaterThanOrEqual (expect (aget r "ms")) 0))))

  (test "bench nested"
    #(when RUN_BENCH
      (let [r (bench-nested 1000)]
        (.toBeGreaterThanOrEqual (expect (aget r "ms")) 0))))

  (test "bench pull"
    #(when RUN_BENCH
      (let [r (bench-pull 5000)]
        (.toBeGreaterThanOrEqual (expect (aget r "ms")) 0)))))
