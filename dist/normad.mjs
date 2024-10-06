import * as squint_core from 'squint-cljs/core.js';
import * as u from './utils.mjs';
import { reconcile } from 'solid-js/store';
var ident_QMARK_ = function (data) {
if (squint_core.truth_(squint_core.vector_QMARK_(data))) {
if (squint_core.truth_(squint_core.string_QMARK_(squint_core.first(data)))) {
const and__24299__auto__1 = squint_core.re_find(/\/id$/, squint_core.first(data));
if (squint_core.truth_(and__24299__auto__1)) {
const and__24299__auto__2 = (squint_core.count(data)) === (2);
if (and__24299__auto__2) {
const or__24281__auto__3 = squint_core.number_QMARK_(squint_core.second(data));
if (squint_core.truth_(or__24281__auto__3)) {
return or__24281__auto__3;} else {
const or__24281__auto__4 = squint_core.string_QMARK_(squint_core.second(data));
if (squint_core.truth_(or__24281__auto__4)) {
return or__24281__auto__4;} else {
return (void 0 === squint_core.second(data));}}} else {
return and__24299__auto__2;}} else {
return and__24299__auto__1;}} else {
return false;}} else {
return false;}
};
var traverse_and_transform = function (item, setStore) {
if (squint_core.truth_(squint_core.vector_QMARK_(item))) {
return squint_core.mapv((function (_PERCENT_1) {
return traverse_and_transform(_PERCENT_1, setStore);
}), item);} else {
if (squint_core.truth_(squint_core.map_QMARK_(item))) {
const ident1 = u.get_ident(item);
const new_val2 = squint_core.zipmap(squint_core.keys(item), squint_core.mapv((function (_PERCENT_1) {
return traverse_and_transform(_PERCENT_1, setStore);
}), squint_core.vals(item)));
if (squint_core.truth_(ident_QMARK_(ident1))) {
squint_core.swap_BANG_(setStore, (function (_PERCENT_1) {
return squint_core.update_in(_PERCENT_1, ident1, (function (v) {
return squint_core.merge(v, new_val2);
}));
}));
return ident1;} else {
return new_val2;}} else {
if ("else") {
return item;} else {
return null;}}}
};
var acc = squint_core.atom(({  }));
var add = (() => {
const f7 = (function (var_args) {
const args81 = [];
const len__25081__auto__2 = arguments.length;
let i93 = 0;
while(true){
if ((i93) < (len__25081__auto__2)) {
args81.push((arguments[i93]));
let G__4 = (i93 + 1);
i93 = G__4;
continue;
};break;
}
;
const argseq__25399__auto__5 = ((1) < (args81.length)) ? (args81.slice(1)) : (null);
return f7.cljs$core$IFn$_invoke$arity$variadic((arguments[0]), argseq__25399__auto__5);
});
f7.cljs$core$IFn$_invoke$arity$variadic = (function (p__12, data) {
const map__67 = p__12;
const ctx8 = map__67;
const store9 = squint_core.get(map__67, "store");
const setStore10 = squint_core.get(map__67, "setStore");
const res11 = traverse_and_transform((() => {
const or__24281__auto__12 = squint_core.first(data);
if (squint_core.truth_(or__24281__auto__12)) {
return or__24281__auto__12;} else {
return store9;}
})(), acc);
squint_core.println("rr: ", res11);
squint_core.println("rr:acc ", squint_core.deref(acc));
if (squint_core.not(squint_core.first(data))) {
squint_core.println("merge-data: ", squint_core.merge_with(squint_core.merge, res11, squint_core.deref(acc)));
setStore10(reconcile(squint_core.merge_with(squint_core.merge, res11, squint_core.deref(acc))))} else {
squint_core.reduce_kv((function (m, k, v) {
squint_core.println("set: ", k, " ", v);
return setStore10(k, (function (_PERCENT_1) {
return squint_core.merge_with(squint_core.merge, _PERCENT_1, v);
}));
}), ({  }), (squint_core.not(squint_core.vector_QMARK_(res11))) ? (squint_core.merge(squint_core.deref(acc), res11)) : (squint_core.deref(acc)))};
squint_core.reset_BANG_(acc, ({  }));
return res11;
});
f7.cljs$lang$maxFixedArity = 1;
f7.cljs$lang$applyTo = (function (seq10) {
const G__1113 = squint_core.first(seq10);
const seq1014 = squint_core.next(seq10);
const self__25117__auto__15 = this;
return self__25117__auto__15.cljs$core$IFn$_invoke$arity$variadic(G__1113, seq1014);
});
return f7;
})();
var pull = function (store, entity, query) {
if (squint_core.truth_(ident_QMARK_(entity))) {
return pull(store, squint_core.get_in(store, entity), query);} else {
if (squint_core.truth_((() => {
const and__24299__auto__1 = (squint_core.count(entity)) > (0);
if (and__24299__auto__1) {
return squint_core.vector_QMARK_(entity);} else {
return and__24299__auto__1;}
})())) {
return squint_core.mapv((function (x) {
if (squint_core.truth_(ident_QMARK_(x))) {
return pull(store, x, query);} else {
return x;}
}), entity);} else {
if (squint_core.truth_((() => {
const and__24299__auto__2 = (squint_core.count(query)) > (1);
if (and__24299__auto__2) {
return squint_core.vector_QMARK_(query);} else {
return and__24299__auto__2;}
})())) {
const simple_keys3 = squint_core.filterv(squint_core.string_QMARK_, query);
const not_simple4 = squint_core.filterv((function (_PERCENT_1) {
return squint_core.not(squint_core.string_QMARK_(_PERCENT_1));
}), query);
return squint_core.into(squint_core.zipmap(simple_keys3, squint_core.mapv((function (_PERCENT_1) {
return pull(store, entity, _PERCENT_1);
}), simple_keys3)), squint_core.mapv((function (_PERCENT_1) {
return pull(store, entity, _PERCENT_1);
}), not_simple4));} else {
if (squint_core.truth_((() => {
const and__24299__auto__5 = (squint_core.count(query)) === (1);
if (and__24299__auto__5) {
return squint_core.vector_QMARK_(query);} else {
return and__24299__auto__5;}
})())) {
return pull(store, entity, squint_core.first(query));} else {
if (squint_core.truth_(squint_core.map_QMARK_(query))) {
const nk6 = squint_core.first(squint_core.keys(query));
const sub_query7 = squint_core.get(query, nk6);
const temp__23976__auto__8 = squint_core.get(entity, nk6);
if (squint_core.truth_(temp__23976__auto__8)) {
const data9 = temp__23976__auto__8;
squint_core.println("-------------:", nk6, ": ", data9, sub_query7);
const G__1310 = ({  });
(G__1310[nk6] = (squint_core.truth_(ident_QMARK_(data9))) ? (pull(store, data9, sub_query7)) : (squint_core.mapv((function (_PERCENT_1) {
return pull(store, _PERCENT_1, sub_query7);
}), data9)));
return G__1310;}} else {
if ("else") {
return squint_core.get(entity, query);} else {
return null;}}}}}}
};
var update_uuid_in_coll = function (coll, old_uuid, new_uuid) {
return squint_core.mapv((function (item) {
if (squint_core.truth_(squint_core.map_QMARK_(item))) {
return update_uuid_in_map(item, old_uuid, new_uuid);} else {
if (squint_core.truth_(squint_core.vector_QMARK_(item))) {
if ((squint_core.second(item)) === (old_uuid)) {
return squint_core.assoc(item, 1, new_uuid);} else {
return update_uuid_in_coll(item, old_uuid, new_uuid);}} else {
if ("else") {
return item;} else {
return null;}}}
}), coll);
};
var update_uuid_in_map = function (m, old_uuid, new_uuid) {
return squint_core.reduce_kv((function (acc, k, v) {
if (squint_core.truth_(squint_core.map_QMARK_(v))) {
return squint_core.assoc(acc, k, update_uuid_in_map(v, old_uuid, new_uuid));} else {
if (squint_core.truth_(squint_core.vector_QMARK_(v))) {
return squint_core.assoc(acc, k, update_uuid_in_coll(v, old_uuid, new_uuid));} else {
if (squint_core.truth_((() => {
const and__24299__auto__1 = squint_core.vector_QMARK_(v);
if (squint_core.truth_(and__24299__auto__1)) {
return (squint_core.second(v)) === (old_uuid);} else {
return and__24299__auto__1;}
})())) {
return [k, new_uuid];} else {
if ((v) === (old_uuid)) {
return [k, new_uuid];} else {
if ("else") {
return squint_core.assoc(acc, k, v);} else {
return null;}}}}}
}), ({  }), m);
};
var swap_uuids_BANG_ = function (p__14, old_uuid, new_id) {
const map__12 = p__14;
const ctx3 = map__12;
const store4 = squint_core.get(map__12, "store");
const setStore5 = squint_core.get(map__12, "setStore");
return setStore5((function (state) {
return update_uuid_in_map(state, old_uuid, new_id);
}));
};

export { ident_QMARK_, traverse_and_transform, acc, add, pull, update_uuid_in_coll, update_uuid_in_map, swap_uuids_BANG_ }
