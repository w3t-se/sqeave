(ns jsonviewer
  (:require ["solid-js" :refer [For Index Show createSignal createMemo Switch Match createEffect]]
            ["solid-icons/ai" :refer [AiOutlineCopy]]
            ["../main/export/index.mjs" :as sqeave]
            ["../main/utils.mjs" :as utils]
            ["solid-transition-group" :refer [TransitionGroup]]
            ;["./styles.module.css" :refer [styles]]
            )
  (:require-macros [sqeave :refer [defc]]))

(defn object-like? [x]
  (and x (= "object" (js/typeof x)) (not (utils/ident? x))))

(defn array? [x]
  (js/Array.isArray x))

(defn read-root [data kind]
  (case kind
    :memo   (data)
    :signal (data)
    :store  data
    :value  data
    ;; auto fallback
    (if (= "function" (js/typeof data))
      (data)
      data)))

(defn object-keys [x]
  (if (object-like? x)
    (js/Object.keys x)
    #js []))

(defn get-in-js [root path]
  (let [n (.-length path)]
    (loop [x root
           i 0]
      (if (or (nil? x) (>= i n))
        x
        (recur (aget x (aget path i))
               (inc i))))))

(defn child-path [path k]
  (.concat path [k]))

(defn Preview [{:keys [data]}]
  #jsx [Switch {:fallback (fn [] #jsx (str "Unknown"))}
        [Match {:when (nil? (data))}
         "null"]
        [Match {:when (utils/ident? (data))}
         (str "[" (first (data)) " " (second (data)) "]")]
        [Match {:when (array? (data))}
         (str "Array(" (.-length (data)) ")")]
        [Match {:when (object-like? (data))}
         (str "Object(" (.-length (object-keys (data))) ")")]
        [Match {:when (= "number" (js/typeof (data)))}
         data]
        [Match {:when (= "string" (js/typeof (data)))}
         data]
        [Match {:when (= "boolean" (js/typeof (data)))}
         (str (data))]
        [Match {:when (= "function" (js/typeof (data)))}
         "[Function]"]])

(defn value-class [x]
  (cond
    (nil? x)
    "text-zinc-500 dark:text-zinc-400"

    (= "string" (js/typeof x))
    (if (utils/is-uuid? x)
      "text-yellow-600 dark:text-yellow-400"
      "text-emerald-600 dark:text-emerald-400 truncate")

    (= "number" (js/typeof x))
    "text-sky-600 dark:text-sky-400"

    (= "boolean" (js/typeof x))
    "text-violet-600 dark:text-violet-400"

    (utils/ident? x) "text-yellow-600 dark:text-yellow-400"
    
    (= "function" (js/typeof x))
    "text-amber-600 dark:text-amber-400"

    :else
    "text-zinc-700 dark:text-zinc-300"))

(defn val-of [x]
  (if (= "function" (js/typeof x))
    (x)
    x))

(defn path-key [path]
  (.join path "."))

(defn child-path [path k]
  (.concat path [k]))

(defn ident? [x]
  (and (array? x)
       (= 2 (.-length x))
       (= "string" (js/typeof (aget x 0)))))

(defn ident-key [ident]
  (when (utils/ident? ident)
    (str (aget ident 0) "|" (aget ident 1))))

(defn path-ident-key [path]
  ;; entity object path like ["data/id" "abc"]
  (when (>= (.-length path) 2)
    (str (aget path 0) "|" (aget path 1))))

(defn entity-id-row-key [path k v]
  ;; row like: data/id: "abc"
  ;; inside entity object under path ["data/id" "abc"]
  (when (and k
             (= "string" (js/typeof v))
             (>= (.-length path) 2)
             (= k (aget path 0))
             (= v (aget path 1)))
    (str (aget path 0) "|" (aget path 1))))

(defn path->str [path]
  (.join path " → "))

(defn path-prefix? [cursor-path path]
  (and (<= (.-length path) (.-length cursor-path))
       (loop [i 0]
         (cond
           (= i (.-length path)) true
           (not= (aget path i) (aget cursor-path i)) false
           :else (recur (inc i))))))

(defn JsonNode [{:keys [k root path kind depth default-expanded max-depth expanded-map setExpandedMap hover-key setHoverKey cursor-path setCursorPath]
                 :or {depth 0 max-depth 8 parent false default-expanded false}}]
  (let [pkey (path-key path)
        
        value
        (createMemo
         (fn []
           (let [r (read-root root kind)]
             (get-in-js r path))))

        expanded? #(let [m (expanded-map)]
                     (if (some? (aget m pkey))
                       (aget m pkey)
                       (and default-expanded (< depth 1))))


        toggle!
        (fn []
          (setExpandedMap
           (fn [old]
             (let [next (js/Object.assign #js {} old)
                   current (aget old pkey)]
               (aset next pkey
                     (not
                      (if (some? current)
                        current
                        (and default-expanded (< depth 1)))))
               next))))

        row-ident-key (createMemo
                       #(or (ident-key (value))
                            (entity-id-row-key path k (value))))
        highlighted? (createMemo
                      #(and (hover-key)
                            (row-ident-key)
                            (= (hover-key) (row-ident-key))))

        is-cursor-prefix? (createMemo
                           #(and (hover-key)
                                 (path-prefix? (cursor-path) path)))
        
        keys-memo (createMemo (fn []
                                (object-keys (value))))
        
        expandable? (createMemo #(and (object-like? (value))
                                      (< depth max-depth)
                                      (> (.-length (keys-memo)) 0)))]
    #jsx
    [:div {:class "font-mono text-xs leading-5"}
     [:div {:class (str "group grid grid-cols-[auto_auto_minmax(0,1fr)_auto] w-full max-w-full items-start gap-1 rounded px-1 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
                        (when (highlighted?)
                          " bg-yellow-100 dark:bg-yellow-900/40 ring-1 ring-yellow-400 "))
            :onMouseEnter
            (fn [_]
              (setCursorPath (path->str path))
              (when-let [ik (row-ident-key)]
                (setHoverKey ik)))

            :onMouseLeave
            (fn [_]
              (when-let [ik (row-ident-key)]
                (setHoverKey nil)))}
      
      [Show {:when (expandable?)
             :fallback (fn [] #jsx [:span {:class "w-4 text-zinc-300 dark:text-zinc-700"} ""])}
       [:button {:class "w-4 text-zinc-500 hover:text-zinc-900 dark:text-zinc-500 dark:hover:text-white transition-colors"
                 :type "button"
                 :onClick (fn [e]
                            (.stopPropagation e)
                            (toggle!))}
        [Show {:when (expanded?) :fallback (fn [] #jsx [:span {:class "text-md"} "▸"])}
         [:span {:class "text-md"} "▾"]]]]

      [Show {:when (some? k)}
       [:span {:class (str
                       "font-semibold "
                       (if (is-cursor-prefix?)
                         "text-sky-600 dark:text-sky-300 underline decoration-sky-400 "
                         "text-zinc-800 dark:text-zinc-100"))}
        (str k ":")]]

      [:span {:class (str "min-w-0 flex-1 max-w-[calc(100vw-6rem)] break-all whitespace-pre-wrap "
                          (value-class (value)))}
       [Preview {:data value}]]

      [:span {:class "ml-auto opacity-0 group-hover:opacity-100 transition-opacity align-center flex"}
       [:button {:class "text-[10px] rounded items-center text-zinc-500 hover:text-zinc-900 dark:hover:text-white"
                 :type "button"
                 :onClick (fn [e]
                            (.stopPropagation e)
                            (let [text (try
                                         (if (object-like? (value))
                                           (js/JSON.stringify (value) nil 2)
                                           (str (value)))
                                         (catch :default _
                                           (str (value))))]
                              (.. js/navigator -clipboard (writeText text))))}
        [AiOutlineCopy {:class "w-3 h-3"}]]]]

     [Show {:when (expanded?) :keyed true}
      (fn []
        #jsx
        [:div {:class "ml-4 border-l border-zinc-200 dark:border-zinc-800 pl-2"}
         [TransitionGroup {:name "json-node"}
          [For {:each (keys-memo)}
           (fn [child-key _]
             #jsx
             [:div {:class "json-node-item"}
              [JsonNode {:k child-key
                         :path (child-path path child-key)
                         :root root
                         :kind kind
                         :expanded-map expanded-map
                         :setExpandedMap setExpandedMap
                         :cursor-path cursor-path
                         :setCursorPath setCursorPath
                         :hover-key hover-key
                         :setHoverKey setHoverKey
                         :depth (inc depth)
                         :max-depth max-depth
                         :default-expanded false}]])]]])]]))

(defn JsonView [{:keys [data kind default-expanded max-depth on-cursor-change]
                 :or {kind :store
                      default-expanded true
                      max-depth 16}}]
  (let [[expanded-map setExpandedMap] (createSignal {})
        [hover-key setHoverKey] (createSignal nil)
        [cursor-path setCursorPath] (createSignal "")]
    (createEffect
     (fn []
       (when on-cursor-change
         (on-cursor-change (cursor-path)))))
    #jsx
    [:div {:class "w-full h-fit overflow-auto rounded border dark:border-zinc-800 bg-white dark:bg-zinc-950 p-2 shadow"}
     [JsonNode {:k nil
                :root data
                :kind kind
                :expanded-map expanded-map
                :setExpandedMap setExpandedMap
                :hover-key hover-key
                :setHoverKey setHoverKey
                :cursor-path cursor-path
                :setCursorPath setCursorPath
                :path []
                :depth 0
                :max-depth max-depth
                :default-expanded default-expanded}]]))
