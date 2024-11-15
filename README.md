!["LIGHTWEIGHT, baby!!! -- Ronnie Coleman"](https://bafkreics5ptu5524ld2wxc2bfjt2vuzmovpmgcheo2fihned2rz5rtgm2q.ipfs.flk-ipfs.xyz/)

# Sqeave - SolidJS meets Squint-cljs
This is a fullstack framework heavily inspired by the great Fulcro (and it's predecessor OM) trying to bring Clojure(Script)-syntax into the SolidJS-world (or maybe vice-versa). 

The philosophy of Sqeave is to be as lightweight as possible and to not overdo features. The main focus is on Components and State management.

The strength of SolidJS of course lies in its signalling system and general nice way/Best Practices for structring Applications. To be honest, with the introduction of signals for React and with lighter React frameworks like Preact, one could probably achieve much of what this library tries to do using Fulcro and a preact/signalling helper. But here we are, VDOM-less, signals-based, ClojureScript syntax, fullstack library with Components and state management a'la Fulcro/OM.

Why squint-cljs and not ClojureScript? The main plus is that we want to have more native integration with npm-world and a more lightweight output packaging. Using vite. Now since squint uses shallow-copy normal js Datastructures we do not have to do conversion with "clj->js" in javascript library heavy interop code because the data is just native javascript objects anyway.

> :warning: This project is in alpha stage.
> Use at your own risk.

## Quickstart

``` shell
$ mkdir sqeave-app && cd sqeave-app
$ npm init -y
$ npm install @w3t/sqeave vite
```

add an App.cljs file with a basic root component:

``` clojure
(ns state
  (:require ["solid-js" :refer [createContext  createSignal]]
            ["solid-js/store" :refer [createStore]]
            ["sqeave/comp" :as comp]))
            
(def AppContext (createContext))

(defc Root [this {:keys []}]
  (let [store [store setStore] (createStore {})]
    #jsx [AppContext.Provider {:value ctx}
           [:div "Hello"]]))
```
the Root Component is described below.


add a vite.config.js:

``` javascript

```

Now run:
``` shell
$ npm run vite
```

## Components
A simple component is defined using the defc macro as:

```Clojure
(defc Simple [this {:simple/keys [id title content]
                    :or {id (comp/uuid) :tite "Default" :content "Yada."}}]
    #jsx [:div {}
          [:h1 {} (title)]
           [:p {} (content)]])
           
(def-factory UiSimple Simple AppContext SimpleFn)
```
notice that the title and content are used within the render function as functions "()". This is because they are solid-js signals and will react to updates (mutations) of the state atom accordingly (examples below).

The defc macro actually creates a Javascript class behind the scenes which is why you see "this" in defc's signature. This is helpful for accessing things like a component's data ident (where the Component's data is located in the sate atom) with `this.ident` or its top level data signal `(this.data)`.

## State
The main idea with Fulcro (and other state management frameworks like Redux etc.) is that the Application State is stored in a top level atom and then mutations on that atom controls the behaviour of the Application. In our case the top level atom is a SolidJS store and then new state changes are [reconciled](https://docs.solidjs.com/reference/store-utilities/reconcile) on that store. Each Component has a Query defined in its defc signature which pulls data from the top state to be used within its render function.

Mostly the idea is that instead of excessive use of hooks to create local state we instead use the built in signals created from the `defc` macro and the query to get data into the Component.

The state is stored in a top level SolidJS Context-map which looks like this: `{:store store :setStore setStore}`. This context is added to all Component instances and can be accessed using `this.-ctx` or simply `ctx`.

### Transactions
There are some helper functions in the `sqeave/transact` namespace to support updating the state:

- `set-field!` 
- `add!` 
- `add-ident!` 
- `remove-ident!` 

You can also access component (`this`) based versions of the transact functions available in the `sqeave/comp` namespace directly for conveniance:

- `set!` mutate a field on 
- `mutate!` 
- `new-data` create a new data map adhering to the `:or`-map in the Components `defc` signature: `(defc Simple [this {:simple/keys [id title] :or {id (comp/uuid) :tite "Default" }}] ...`


