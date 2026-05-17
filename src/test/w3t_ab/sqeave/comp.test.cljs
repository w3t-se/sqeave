(ns index
  (:require ["vitest" :refer [describe it expect afterEach]]
            ["@solidjs/testing-library" :refer [render screen cleanup]]
            ["./Root.jsx" :refer [Root]])
  (:require-macros [sqeave :refer [defc]]))

(afterEach cleanup)

(describe "normad"
          (fn []
            (it "ensure we can create a root component"
                (fn []
                  (render (fn [] #jsx [Root {}]))
                  (expect (.getByText screen "I am root."))))))
