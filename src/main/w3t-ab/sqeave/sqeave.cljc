(ns sqeave)

(defn strip-ns [sym]
  (symbol (name sym)))

(defmacro def-factory [name cla ctx body]
  (list 'defn name ['props] (list 'let ['c (list 'new cla ctx)]
                                  (list '.render 'c body 'props))))

(defmacro defc [name bindings body]
  (let [ntmp (str name)
        n (namespace (first (keys (second bindings))))

        params (first (vals (second bindings)))
        val-vec (mapv #(if (map? %) (first (keys %)) %) params)
        val-vec (mapv strip-ns val-vec)

        keywordify (fn [x] (keyword (str (when n (str n "/")) x)))

        val-keys (mapv keyword val-vec)

        or-map (let [m (-> bindings second :or)]
                 (zipmap (mapv keywordify (keys m)) (vals m)))

        local-map (let [m (-> bindings second :local)]
                    (zipmap (mapv keyword (keys m)) (vals m)))

        query (mapv #(if (map? %)
                       {(keywordify (first (keys %))) (first (vals %))}
                       (keywordify %)) params)

        binding-ctx (:ctx (second bindings))]

    (list 'do
          (list 'defn (symbol (str name "Fn")) [{:keys (conj val-vec 'this 'props 'ctx)}]

                (list 'squint-compiler-jsx
                      ['sqeave/ErrorBoundary {:fallback (list 'fn ['err 'reset]
                                                              (list 'sqeave/warn 'err)
                                                              (list 'sqeave/onMount (list 'fn []
                                                                                          #_`(when (some? (.-hot js/import.meta))
                                                                                               (.accept (.-hot js/import.meta)
                                                                                                        (fn []
                                                                                                          ~(list 'reset)
                                                                                                          (sqeave/debug "🔄 Hot Reload detected!"))))))
                                                              (list 'squint-compiler-jsx
                                                                    [:div {:onClick (list 'fn ['e] (list 'reset))}
                                                                     (list :message 'err)]))}
                       (list 'sqeave/onMount (list 'fn []
                                                   (list 'sqeave/debug "I was mounted: " (list 'this.data) ": " (str name "Fn") " " 'this.ident)
                                                   (list 'sqeave/debug "owner1:" (list 'sqeave/getOwner))))
                       (list 'sqeave/onCleanup (list 'fn []
                                                     #_(list 'setRegistry (list 'update 'registry (str name "Class") 'dissoc (list 'second 'this.ident)))
                                                     (list 'sqeave/debug "I was cleaned up: " (str name "Fn") " " 'this.ident " " (list 'sqeave/getOwner))))
                       body]))

          (list 'defclass (symbol (str name "Class"))
                (list 'extends 'sqeave/Comp)

                (list (with-meta 'field {:static true}) 'query query)

                (list 'field 'local)
                (list 'field 'set-local!)

                (list 'constructor ['this# 'ctx 'ident] (list 'sqeave/debug "constructor: " ntmp " ctx: " 'ctx " ident: " 'ident)
                      (list 'super 'ctx 'ident)
                      (list 'set! 'this#.ident 'ident)
                      (list 'set! 'this#.render 'this#.constructor.prototype.render)
                      (list 'set! 'this#.new-data 'this#.constructor.new-data))

                'Object
                (list (with-meta 'new-data {:static true}) ['_ 'data] (list 'merge or-map 'data))

                (list 'first-render ['this# 'props]
                      (list 'let [;(first bindings) 'this#
                                  '_ (list 'sqeave/debug "render: " ntmp " props: " 'props)
                                  'ctx (list 'or binding-ctx (list `useContext 'this#.-ctx))

                                  '_ (list 'sqeave/debug ntmp ": p " 'props " i: " 'this#.ident " q: " query " ctx:" 'ctx)
                                  ;['force 'setForce] (list 'sqeave/createSignal false)
                                        ;'_ (list 'set! 'this#.ident 'ident)
                                  {:keys ['store 'setStore]} 'ctx

                                  'data (list 'sqeave/remove-nil (list 'sqeave/pull (list 'get 'ctx :store) (list 'if (list 'empty? 'this#.ident)
                                                                                                                  (list 'get 'ctx :store) 'this#.ident)
                                                                       query))

                                  '_ (list 'when (list 'empty? 'data)
                                                          (list 'sqeave/add! 'ctx (list 'this#.new-data (list 'if-not (list 'nil? (list 'second 'this#.ident))
                                                                                                              {(list 'first 'this#.ident) (list 'second 'this#.ident)}))))

                                  'data (list 'if-not (list 'empty? query)
                                              (list 'let ['data (list 'sqeave/createMemo (list 'fn []
                                                                                               #_(list 'force)
                                                                                               (list 'sqeave/debug "memo: " ntmp " ident: " 'this#.ident " query: " query)
                                                                                               (list 'let ['data (list 'sqeave/pull (list 'get 'ctx :store) (list 'if (list 'empty? 'this#.ident)
                                                                                                                                                                  (list 'get 'ctx :store) 'this#.ident) query)]
                                                                                                     (list 'sqeave/debug "memo: " ntmp " ident: " 'this#.ident "data: " 'data)
                                                                                                     'data)))]



                                                    #_(list 'sqeave/debug "nn:" (list 'data) ":" (list 'sqeave/remove-nil (list 'data)))

                                                    'data)
                                              (list 'fn [] 'props))
                                  'val-v (list 'mapv (list 'fn ['x] (list 'sqeave/createMemo (list 'fn [] (list 'get (list 'data) 'x)))) (mapv keywordify val-vec))
                                  val-vec 'val-v
                                  ['local 'setLocal] (list 'sqeave/createSignal local-map)]

                            (list 'set! 'this#.ctx 'ctx)
                            #_(list 'set! 'this#.force 'force)
                            #_(list 'set! 'this#.setForce 'setForce)
                            (list 'set! 'this#.local 'local)
                            (list 'set! 'this#.data 'data)
                            (list 'set! 'this#.val-vec 'val-v)
                            (list 'set! 'this#.set-local! (list 'fn ['this# 'data] (list 'setLocal (list 'merge (list 'local) 'data))))))

                (list 'render ['this# 'body 'props]
                      #_(list 'this#.setForce (list 'not (list 'this#.force)))
                      #_(list  'this#.val-vec val-keys)
                      (list 'sqeave/debug "this:d" (list 'this#.data))
                      (list 'let ['local-map-k (vec (keys local-map))
                                  'local-map-k (mapv #(list 'fn [] (list % (list 'this#.local))) (keys local-map))]
                            (list 'body (list 'zipmap (list 'conj val-keys :this :props :ctx) (list 'conj 'this#.val-vec 'this# 'props 'this#.ctx))))))

          (list 'defn (symbol (str name "Factory")) ['ident 'props]
                (list 'let [;'ctx (list 'or binding-ctx (list `useContext 'sqeave/AppContext))
                            ;'setRegistry (list 'get 'ctx :setRegistry)
                            ;'registry (list 'get 'ctx :registry)
                            ;'owner (list 'if-not binding-ctx (list 'sqeave/getOwner))
                            ]
                      #_(list 'swap! 'sqeave/ComponentRegistry 'assoc-in [(str name "Class") (list 'second 'ident)] 'c)

                      #_(when (.-hot js/import.meta)
                          (.accept (.-hot js/import.meta) (fn []
                                                            (list '.render 'c (symbol (str name "Fn")) 'props))))

                      (list 'let ['c (list 'new (symbol (str name "Class")) 'sqeave/AppContext 'ident)]
                            #_(list 'set! 'this#.owner 'owner)
                            #_(list 'setRegistry (list 'fn [] (list 'assoc-in 'registry [(str name "Class") (list 'second 'ident)] 'c)))
                            (list '.first-render 'c 'props)
                            (list '.render 'c (symbol (str name "Fn")) 'props)
                            #_(list 'let ['c (list 'get-in 'registry #_(list 'deref 'sqeave/ComponentRegistry) [(list 'str name "Class") (list 'second 'ident)])]
                                    (list '.render 'c (symbol (str name "Fn")) 'props)))))

          #_(list 'defn name ['props]
                  `(let [ident (get props :ident)
                         ident (if (fn? ident) (ident) ident)]
                     (sqeave/debug "idnet: " ident)
                     (when-not (deref sqeave/HOT_LOADED)
                       (if-let [c (get-in (deref sqeave/ComponentRegistry)
                                          [(str ~name "Class") (second ident)])]
                         (do (sqeave/debug "class: " c) (.render c ~(symbol (str name "Fn")) props))
                         (~(symbol (str name "Factory")) ident props)))))

          #_(defonce ~(symbol "HOT_LOADED") (atom false))

          #_(when (.-hot js/import.meta)
              (.accept (.-hot js/import.meta) (fn []
                                                (println "hot")
                                                (reset! ~(symbol "HOT_LOADED") true))))

          (list 'defn name ['props]
                (list 'let ['ident (list 'get 'props :ident)
                            'ident (list 'if-not (list 'fn? 'ident) 'ident (list 'ident))
                            ;'ctx (list 'or binding-ctx (list `useContext 'sqeave/AppContext))
                            ;'_ (list 'println 'ctx)
                            ;'registry (list 'get 'ctx :registry)
                            ;'owner (list 'if-not binding-ctx (list 'sqeave/getOwner))
                            ]
                      #_(list 'sqeave/debug "idnet: " 'ident " owner: " 'owner)

                      (list (symbol (str name "Factory")) 'ident 'props)

                      ;; use if comp registry
                      #_(list 'if-let ['c (list 'get-in 'registry [(str name "Class") (list 'second 'ident)])]
                              (list 'do
                                    (list 'aset 'c :ctx 'ctx)
                                    (list 'sqeave/debug "class: " 'c)
                                    (list 'sqeave/runWithOwner 'owner
                                          (list 'fn [] (list '.render 'c (symbol (str name "Fn")) 'props))))
                              (list (symbol (str name "Factory")) 'ident 'props)))))))

;list 's/runWithOwner 'owner
