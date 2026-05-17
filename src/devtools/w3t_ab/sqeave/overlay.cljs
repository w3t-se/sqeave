(ns overlay
  (:require ["solid-js" :refer [createSignal createMemo Show createEffect onMount]]
            ["solid-js/store" :refer [unwrap createStore]]
            ["@corvu/resizable" :as cr]
            ["@solid-primitives/deep" :refer [captureStoreUpdates trackStore]]
            ["../../../../assets/images/sqeave_logo.png" :as sqeaveUrl]
            ["../components/jsonviewer.jsx" :as jsonviewer]
            ["../main/export/index.mjs" :as sqeave]
            ["../main/utils.mjs" :as utils]
            ["../main/log.mjs" :as log])
  (:require-macros [sqeave :refer [defc]]))

(def Resizable (:default cr))
(def sqeaveImg (:default sqeaveUrl))

(defc DevOverlay [this {:keys []}]
  (let [[open? setOpen!] (createSignal (:open? (utils/get-item "sqeave-overlay-state")))
        [dock setDock!] (createSignal :right)
        [version setVersion] (createSignal 1)
        [selectedVersion setSelectedVersion] (createSignal 1)
        [cursor setCursor] (createSignal "")
        getDelta (captureStoreUpdates (:store ctx))
        delta (createMemo (fn [] (getDelta)))
        [storeClone setStoreClone] (createStore (utils/unwrap-proxy (:store ctx)))
        [deltaStore setDelta] (createStore {})
        [history setHistory] (createSignal [{:delta {}
                                             :store (utils/unwrap-proxy (:store ctx))}])]

    (createEffect (fn []
                    (log/debug "update " (history))
                    (setDelta (:delta (nth (history) (- (selectedVersion) 1))))
                    (setStoreClone (:store (nth (history) (- (selectedVersion) 1))))))
    (createEffect
     (fn []
       #_(trackStore (:store ctx))
       (setHistory (fn [x]
                     (conj x {:delta (delta)
                              :store (utils/unwrap-proxy (:store ctx))})))
       (setVersion inc)
       (setSelectedVersion (version))))

    #jsx
    [:<>
     [:button
      {:class (str
               "fixed bottom-4 right-20 z-[2147483647] "

               ;; round floating button
               "w-14 h-14 rounded-full "

               ;; centering
               "flex items-center justify-center "

               ;; styling
               "bg-zinc-900 dark:bg-zinc-100 "
               "text-white dark:text-zinc-900 "

               ;; effects
               "shadow-2xl ring-1 ring-black/10 dark:ring-white/10 "
               "hover:scale-105 active:scale-95 "
               "transition-all duration-200 "

               ;; glow
               "hover:shadow-cyan-400/30 ")

       ;:title (if (open?) "Close State" "Open State")

       :onPointerDown (fn [e]
                        (.stopPropagation e))

       :onClick (fn [e]
                  (let [st (not (open?))]
                    (.preventDefault e)
                    (.stopPropagation e)
                    (setOpen! st)
                    (sqeave/set-item! "sqeave-overlay-state"
                                      {:open? st})))}

      [:img
       {:src sqeaveImg
        :class "w-8 h-8 object-contain pointer-events-none select-none"
        :draggable false}]]

     [Show {:when (open?)}
      [:div {:class "fixed inset-0 z-[2147483646] pointer-events-none"}

       [Resizable {:class "size-full pointer-events-none"
                   :orientation (if (= (dock) :bottom) :vertical :horizontal)}
        [Resizable.Panel
         {:initialSize 0.7
          :minSize 0.1
          :class "pointer-events-none"}]

        [Resizable.Handle
         {:aria-label "Resize Handle"
          :class "group basis-3 px-0.75 pointer-events-auto cursor-col-resize"}
         [:div {:class "size-full rounded-sm transition-colors group-data-active:bg-sky-400/60 group-data-dragging:bg-sky-400/40"}]]

        [Resizable.Panel
         {:initialSize 0.3
          :minSize 0.2
          :class "pointer-events-auto bg-white dark:bg-zinc-950 border-l dark:border-zinc-800 shadow-2xl flex flex-col"}

         [:div {:class "flex items-center gap-2 px-3 py-2 border-b dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900"}
          [:div {:class "font-semibold text-sm flex-1"} "Squeave State"]


          [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                    :onClick #(when (> (selectedVersion) 1) (setSelectedVersion dec))}
           "<"]

          [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                    :onClick #(when (< (selectedVersion) (version)) (setSelectedVersion inc))}
           ">"]

          [Show {:when (= (dock) :right)
                 :fallback (fn [] #jsx [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                                                 :onClick #(setDock! :right)}
                                        "right"])}
           [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                     :onClick #(setDock! :bottom)}
            "bottom"]]

          [:button {:class "text-xs px-2 py-1 rounded hover:bg-zinc-200 dark:hover:bg-zinc-800"
                    :onClick #(setOpen! false)}
           "✕"]]

         [:div {:class "flex-1 overflow-hidden"}
          [:div {:class "px-2 py-1 text-xs text-zinc-500 dark:text-zinc-400"}
           (str "Version: " (selectedVersion) "/" (version))]

          [:div {:class "h-full overflow-auto pb-8"}
           [jsonviewer/JsonView
            {:data storeClone
             :kind :store
             :on-cursor-change setCursor
             :default-expanded true
             :max-depth 12}]

           [jsonviewer/JsonView
            {:data deltaStore
             :kind :store
             :on-cursor-change setCursor
             :default-expanded true
             :max-depth 12}]]]]]]]]))
