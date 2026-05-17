(ns overlay
  (:require ["solid-js" :refer [createSignal Show createEffect]]
            ["@corvu/resizable" :as cr]
            ["./jsonviewer.cljs" :as jsonviewer]))

(defn DevOverlay [{:keys [ctx data kind]}]
  (let [[open? setOpen!] (createSignal false)
        [dock setDock!] (createSignal :right)
        [json-version setJsonVersion] (createSignal 0)]

    (createEffect
     (fn []
       (when-let [store (:store ctx)]
         ;; force dependency on deep store updates
         (js/JSON.stringify store)
         (setJsonVersion inc))))

    #jsx
    [:<> {}

     ;; floating toggle button
     [:button
      {:class "
        fixed bottom-4 right-20
        z-[2147483647]
        rounded-full shadow-lg
        bg-zinc-900 dark:bg-zinc-100
        text-white dark:text-zinc-900
        px-3 py-2 text-sm font-medium
        hover:scale-105 transition-transform"
       :onPointerDown (fn [e]
                        (.stopPropagation e))
       :onClick (fn [e]
                  (.preventDefault e)
                  (.stopPropagation e)
                  (setOpen! (not (open?))))}
      (if (open?) "Close State" "State")]

     [Show {:when (open?)}

      [Show
       {:when (= (dock) :right)

        :fallback
        (fn []
          #jsx
          [(:Root cr)
           {:orientation "vertical"
            :class "
             fixed inset-0
             z-[2147483646]
             pointer-events-none"}

           ;; spacer
           [(:Panel cr)
            {:initialSize 65
             :minSize 10
             :class "pointer-events-none"}]

           ;; resize handle
           [(:Handle cr)
            {:class "
              pointer-events-auto
              h-1
              cursor-row-resize
              bg-transparent
              hover:bg-sky-500/60
              transition-colors"}]

           ;; overlay panel
           [(:Panel cr)
            {:initialSize 35
             :minSize 15
             :class "
              pointer-events-auto
              min-h-[180px]
              bg-white dark:bg-zinc-950
              border-t dark:border-zinc-800
              shadow-2xl
              flex flex-col"}

            ;; header
            [:div
             {:class "
               flex items-center gap-2
               px-3 py-2
               border-b dark:border-zinc-800
               bg-zinc-50 dark:bg-zinc-900"}

             [:div {:class "font-semibold text-sm flex-1"}
              "Squeave State"]

             [:button
              {:class "
                text-xs px-2 py-1 rounded
                hover:bg-zinc-200 dark:hover:bg-zinc-800"
               :onClick #(setDock! :right)}
              "dock right"]

             [:button
              {:class "
                text-xs px-2 py-1 rounded
                hover:bg-zinc-200 dark:hover:bg-zinc-800"
               :onClick #(setOpen! false)}
              "✕"]]

            ;; body
            [:div {:class "flex-1 overflow-hidden"}
             [:div {:class "px-2 py-1 text-xs text-zinc-500 dark:text-zinc-400"}
              (str "Version: " (json-version))]

             [:div {:class "h-full overflow-auto"}
              [jsonviewer/JsonView
               {:data data
                :kind kind
                :version json-version
                :default-expanded false
                :max-depth 12}]]]]])}

       ;; RIGHT DOCK
       [(:Root cr)
        {:orientation "horizontal"
         :class "
          fixed inset-0
          z-[2147483646]
          pointer-events-none"}

        ;; spacer
        [(:Panel cr)
         {:initialSize 70
          :minSize 10
          :class "pointer-events-none"}]

        ;; resize handle
        [(:Handle cr)
         {:class "
           pointer-events-auto
           w-1
           cursor-col-resize
           bg-transparent
           hover:bg-sky-500/60
           transition-colors"}]

        ;; overlay panel
        [(:Panel cr)
         {:initialSize 30
          :minSize 15
          :class "
            pointer-events-auto
            min-w-[320px]
            bg-white dark:bg-zinc-950
            border-l dark:border-zinc-800
            shadow-2xl
            flex flex-col"}

         ;; header
         [:div
          {:class "
            flex items-center gap-2
            px-3 py-2
            border-b dark:border-zinc-800
            bg-zinc-50 dark:bg-zinc-900"}

          [:div {:class "font-semibold text-sm flex-1"}
           "Squeave State"]

          [:button
           {:class "
             text-xs px-2 py-1 rounded
             hover:bg-zinc-200 dark:hover:bg-zinc-800"
            :onClick #(setDock! :bottom)}
           "dock bottom"]

          [:button
           {:class "
             text-xs px-2 py-1 rounded
             hover:bg-zinc-200 dark:hover:bg-zinc-800"
            :onClick #(setOpen! false)}
           "✕"]]

         ;; body
         [:div {:class "flex-1 overflow-hidden"}
          [:div {:class "px-2 py-1 text-xs text-zinc-500 dark:text-zinc-400"}
           (str "Version: " (json-version))]

          [:div {:class "h-full overflow-auto"}
           [jsonviewer/JsonView
            {:data data
             :kind kind
             :version json-version
             :default-expanded false
             :max-depth 12}]]]]]]]]]))
