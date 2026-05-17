(ns )


(ns overlay
  (:require ["solid-js" :refer [createSignal Show createEffect]]
            ["~/components/ui/resizable.tsx"
             :refer [Resizable ResizableHandle ResizablePanel]]
            ["./jsonviewer.cljs" :as jsonviewer]))

(defn DevOverlay [{:keys [ctx data kind]}]
  (let [[open? setOpen!] (createSignal false)
        [dock setDock!] (createSignal :right)
        [json-version setJsonVersion] (createSignal 0)]
    (createEffect
     (fn []
       (when-let [store (:store ctx)]
         (js/JSON.stringify store)
         (setJsonVersion inc))))

    #jsx
    [:<> {}

     [:button
      {:class "fixed bottom-4 right-20 z-[2147483647] rounded-full shadow-lg bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 px-3 py-2 text-sm font-medium hover:scale-105 transition-transform"
       :style {:z-index 2147483647}
       :onPointerDown (fn [e] (.stopPropagation e))
       :onClick (fn [e]
                  (.preventDefault e)
                  (.stopPropagation e)
                  (setOpen! (not (open?))))}
      (if (open?) "Close State" "State")]

     [Show {:when (open?)}
      [:div {:class "fixed inset-0 z-[2147483646] pointer-events-none"}

       [Show {:when (= (dock) :right)
              :fallback
              (fn []
                #jsx
                [Resizable {:orientation "vertical"
                            :class "fixed inset-0 pointer-events-auto"}
                 [ResizablePanel {:initialSize 60 :minSize 20}
                  [:div {:class "h-full pointer-events-none"}]]
                 [ResizableHandle]
                 [ResizablePanel {:initialSize 40 :minSize 20}
                  [:div {:class "h-full flex flex-col bg-white dark:bg-zinc-950 border-t dark:border-zinc-800 shadow-2xl"}
                   [:div {:class "flex items-center gap-2 px-3 py-2 border-b dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900"}
                    [:div {:class "font-semibold text-sm flex-1"} "Squeave State"]
                    [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                              :onClick #(setDock! :right)}
                     "dock right"]
                    [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                              :onClick #(setOpen! false)}
                     "✕"]]
                   [:div {:class "flex-1 overflow-hidden"}
                    [:span {:class "p-1 text-xs"} (str "Version: " (json-version))]
                    [:div {:class "h-full overflow-auto"}
                     [jsonviewer/JsonView {:data data
                                           :kind kind
                                           :version json-version
                                           :default-expanded false
                                           :max-depth 12}]]]]]])}

        [Resizable {:orientation "horizontal"
                    :class "fixed inset-0 pointer-events-auto"}
         [ResizablePanel {:initialSize 70 :minSize 20}
          [:div {:class "h-full pointer-events-none"}]]
         [ResizableHandle]
         [ResizablePanel {:initialSize 30 :minSize 20}
          [:div {:class "h-full flex flex-col bg-white dark:bg-zinc-950 border-l dark:border-zinc-800 shadow-2xl"}
           [:div {:class "flex items-center gap-2 px-3 py-2 border-b dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900"}
            [:div {:class "font-semibold text-sm flex-1"} "Squeave State"]
            [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                      :onClick #(setDock! :bottom)}
             "dock bottom"]
            [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                      :onClick #(setOpen! false)}
             "✕"]]
           [:div {:class "flex-1 overflow-hidden"}
            [:span {:class "p-1 text-xs"} (str "Version: " (json-version))]
            [:div {:class "h-full overflow-auto"}
             [jsonviewer/JsonView {:data data
                                   :kind kind
                                   :version json-version
                                   :default-expanded false
                                   :max-depth 12}]]]]]]]]]))
(ns overlay
  (:require ["solid-js" :refer [createSignal Show createEffect]]
            ["./jsonviewer.cljs" :as jsonviewer]))

(defn DevOverlay [{:keys [ctx data]}]
  (let [[open? setOpen!] (createSignal false)
        [dock setDock!] (createSignal :right)
        [json-version setJsonVersion] (createSignal 0)]
    (createEffect
     (fn []
       (js/JSON.stringify (:store ctx))
       (setJsonVersion inc)))
    #jsx
    [:<> {}
     
     ;; floating button
     [:button
      {:class "
        fixed bottom-4 right-20 z-[99999]
        rounded-full shadow-lg
        bg-zinc-900 dark:bg-zinc-100
        text-white dark:text-zinc-900
        px-3 py-2 text-sm font-medium
        hover:scale-105 transition-transform"
        :style {:z-index 2147483647}
       :onClick #(setOpen! (not (open?)))}
      (if (open?) "Close State" "State")]

     ;; overlay
     [Show {:when (open?)}
      [:div
       {:class
        (str
          "fixed z-[99998] bg-white dark:bg-zinc-950 border dark:border-zinc-800 shadow-2xl flex flex-col "

          (case (dock)
            :right
            "top-0 right-0 h-full w-[500px] border-l"

            :bottom
            "bottom-0 left-0 w-full h-[40vh] border-t"

            "top-0 right-0 h-full w-[500px]"))}

       ;; header
       [:div
        {:class "
          flex items-center gap-2
          px-3 py-2 border-b dark:border-zinc-800
          bg-zinc-50 dark:bg-zinc-900"}

        [:div {:class "font-semibold text-sm flex-1"}
         "Squeave State"]

        [:button
         {:class "
           text-xs px-2 py-1 rounded
           hover:bg-zinc-200 dark:hover:bg-zinc-800"
          :onClick #(setDock!
                     (if (= (dock) :right)
                       :bottom
                       :right))}
         (if (= (dock) :right)
           "dock bottom"
           "dock right")]

        [:button
         {:class "
           text-xs px-2 py-1 rounded
           hover:bg-zinc-200 dark:hover:bg-zinc-800"
          :onClick #(setOpen! false)}
         "✕"]]

       [:div {:class "flex-1 overflow-hidden"}
        [:span {:class "p-1"}
         (str "Version: " (json-version))]
        [:div {:class "overflow-auto"}
         [jsonviewer/JsonView {:data data
                               :version json-version
                               :default-expanded false
                               :max-depth 12}]]]]]]))
