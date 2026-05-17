(ns sqeave.devtools
  (:require ["solid-js" :refer [Show createSignal onMount DEV]]
            ["solid-js/web" :refer [Dynamic]]
            ["../main/log.mjs" :as log]))

(defn DevTools [{:keys [ctx]}]
  (let [[Comp setComp] (createSignal nil)
        dev? DEV]
    (onMount
     (fn []
       (when dev?
         (-> (js/import "./overlay.jsx")
             (.then
              (fn [m]
                (setComp (fn [] (.-DevOverlay m)))
                (log/debug "We loaded DevOverlay: " (Comp))))))))
    #jsx
    [Show {:when (and dev? (Comp))}
     [Dynamic {:component (Comp)
               :ctx ctx}]]))
