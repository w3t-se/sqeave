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
                       (keywordify %))
                    params)
        binding-ctx (:ctx (second bindings))
        fn-name (symbol (str name "Fn"))
        class-name (symbol (str name "Class"))
        factory-name (symbol (str name "Factory"))
        zip-args (list 'zipmap
                       (list 'conj val-keys :this :props :ctx)
                       (list 'conj 'val-vec 'this# 'props 'ctx))]
    (list 'do
          (list 'defn fn-name [{:keys (conj val-vec 'this 'props 'ctx)}]
                (list 'squint-compiler-jsx
                      ['sqeave/ErrorBoundary
                       {:fallback (list 'fn ['err 'reset]
                                        (list 'sqeave/warn 'err)
                                        (list 'sqeave/onMount (list 'fn []))
                                        (list 'squint-compiler-jsx
                                              [:div {:onClick (list 'fn ['_e] (list 'reset))}
                                               (list :message 'err)]))}
                       body]))

          (list 'defclass class-name
                (list 'extends 'sqeave/Comp)
                (list (with-meta 'field {:static true}) 'query query)
                (list 'field 'local)
                (list 'field 'set-local!)
                (list 'constructor ['this# 'ctx 'ident]
                      (list 'sqeave/debug "constructor: " ntmp " ctx: " 'ctx " ident: " 'ident)
                      (list 'super 'ctx 'ident)
                      (list 'set! 'this#.ident 'ident)
                      (list 'set! 'this#.render 'this#.constructor.prototype.render)
                      (list 'set! 'this#.new-data 'this#.constructor.new-data))

                'Object
                (list (with-meta 'new-data {:static true}) ['_ 'data] (list 'merge or-map 'data))

                (list 'first-render ['this# 'props]
                      (list 'let [(first bindings) 'this#
                                  'ident$ (list 'sqeave/createMemo
                                                (list 'fn []
                                                      (list 'let ['i (list 'get 'props :ident)]
                                                        (list 'if (list 'fn? 'i) (list 'i) 'i))))
                                  '_ (list 'set! 'this#.ident 'ident$)
                                  '_ (list 'sqeave/debug "render: " ntmp " props: " 'props)
                                  'ctx (list 'or binding-ctx (list 'sqeave/useContext 'this#.-ctx))
                                  '_ (list 'sqeave/debug ntmp ": p " 'props " i: " (list 'this#.ident) " q: " query " ctx:" 'ctx
                                             " exists:" (list 'js/Reflect.has (list 'get 'ctx :store) (list 'first (list 'this#.ident))))
                                  {:keys ['store 'setStore]} 'ctx
                                  'val-v (list 'mapv (list 'fn ['x]
                                                           (list 'if-not (list 'map? 'x)
                                                                 (list 'sqeave/createMemo (list 'fn [] (list 'get-in 'ctx [:store (list 'first (list 'this#.ident)) (list 'second (list 'this#.ident)) 'x])))
                                                                 (list 'sqeave/createMemo (list 'fn []
                                                                                                (list 'sqeave/pull (list 'get 'ctx :store)
                                                                                                      [(list 'first (list 'this#.ident)) (list 'second (list 'this#.ident)) (list 'first (list 'keys 'x))]
                                                                                                      (list 'first (list 'vals 'x)))))))
                                               query)
                                  'val-vec 'val-v
                                  ['local 'setLocal] (list 'sqeave/createSignal local-map)
                                  'local-map-k (vec (keys local-map))
                                  'local-map-k (mapv #(list 'fn [] (list % (list 'this#.local))) (keys local-map))]
                            (list 'set! 'this#.ctx 'ctx)
                            (list 'set! 'this#.local 'local)
                            (list 'set! 'this#.val-vec 'val-v)
                            (list 'set! 'this#.set-local! (list 'fn ['_this# 'data] (list 'setLocal (list 'merge 'local 'data))))
                            (list 'sqeave/debug "thiss: " 'this# "ctc: " 'ctx " m: " (list 'zipmap (list 'conj val-keys :this :props :ctx) (list 'conj 'val-vec 'this# 'props 'ctx)))
                            (list 'squint-compiler-jsx
                                  (list fn-name zip-args))))

                (list 'render ['this# 'body 'props]
                      (list 'let ['local-map-k (vec (keys local-map))
                                  'local-map-k (mapv #(list 'fn [] (list % (list 'this#.local))) (keys local-map))]
                            (list 'body (list 'zipmap (list 'conj val-keys :this :props :ctx) (list 'conj 'this#.val-vec 'this# 'props 'this#.ctx))))))

          (list 'defn factory-name ['ident 'props]
                (list 'let ['c (list 'new class-name 'sqeave/AppContext 'ident)]
                      (list '.first-render 'c 'props)))

          (list 'defn name ['props]
                (list 'let ['ident (list 'get 'props :ident)
                            'ident (list 'if-not (list 'fn? 'ident) 'ident (list 'ident))]
                      (list factory-name 'ident 'props))))))

;list 's/runWithOwner 'owner
