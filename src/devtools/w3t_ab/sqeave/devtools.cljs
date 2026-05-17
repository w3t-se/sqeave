(ns sqeave.devtools
  (:require ["solid-js" :refer [Show createSignal onMount]]))

(def dev? (.-DEV (.-env js/import.meta)))

(defn DevTools []
  (let [[Comp setComp] (createSignal nil)]
    (onMount
     (fn []
       (when dev?
         (-> (js/import "./overlay.cljs")
             (.then
              (fn [m]
                (setComp (fn [] (.-DevOverlay m)))))))))
    #jsx
    [Show {:when (and dev? (Comp))}
     (fn []
       #jsx [(Comp)])]))
