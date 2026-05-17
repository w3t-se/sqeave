(ns render-tests
  (:require ["vitest" :refer [describe it expect afterEach]]
            ["solid-js" :refer [createEffect createContext]]
            ["@solidjs/testing-library" :refer [render screen cleanup]]
            ["@w3t-ab/sqeave" :as sqeave])
  (:require-macros [sqeave :refer [defc]]))

(def TestContext (createContext))

(defc Child [this {:keys [label]
                   :or {label "child"}
                   :ctx (sqeave/init-ctx! TestContext)}]
  #jsx [TestContext.Provider {:value ctx}
        [:span {:data-testid "child"} label]])

(defc ListView [this {:keys [items onRender]
                      :or {items []}
                      :ctx (sqeave/init-ctx! TestContext)
                      :local {render-count 0}}]
  (createEffect (fn [] (when onRender (onRender))))
  #jsx [TestContext.Provider {:value ctx}
        [:ul {}
         (map (fn [item]
                #jsx [:li {:data-testid (str "item-" item)} item])
              items)]])

(afterEach cleanup)

(describe "rendering"
  (fn []
    (it "renders nested defc components"
      (fn []
        (render (fn [] #jsx [Child {:label "Nested child"}]))
        (let [el (.getByTestId screen "child")]
          (expect (.-textContent el) "Nested child"))))

    (it "renders large lists within budget"
      (fn []
        (let [items (range 750)
              start (.now js/performance)
              result (render (fn [] #jsx [ListView {:items items}]))
              elapsed (- (.now js/performance) start)]
          (expect (< elapsed 750))
          (expect (.-length (.querySelectorAll (.-container result) "li"))
                  (count items)))))

    (it "avoids extra renders when rerendering with new data"
      (fn []
        (let [render-count (atom 0)
              items (range 10)
              _initial (render (fn [] #jsx [ListView {:items items
                                                       :onRender #(swap! render-count inc)}]))]
          (cleanup)
          (render (fn [] #jsx [ListView {:items (map inc items)
                                         :onRender #(swap! render-count inc)}]))
          (expect (<= @render-count 5)))))))
