import * as squint_core from 'squint-cljs/core.js';
import * as solid from 'solid-js';
import * as n from './normad.mjs';
import * as t from './transact.mjs';
import * as u from './utils.mjs';
var remotes = squint_core.atom(({  }));
var set_BANG_ = (() => {
const f2 = (function (var_args) {
const G__51 = arguments.length;
switch (G__51) {case 4:
return f2.cljs$core$IFn$_invoke$arity$4((arguments[0]), (arguments[1]), (arguments[2]), (arguments[3]));
break;
case 3:
return f2.cljs$core$IFn$_invoke$arity$3((arguments[0]), (arguments[1]), (arguments[2]));
break;
default:
throw new Error(squint_core.str("Invalid arity: ", squint_core.alength(arguments)))}
});
f2.cljs$core$IFn$_invoke$arity$4 = (function (this$, ident, field, event) {
return t.set_field_BANG_(this$._ctx, (() => {
const or__24281__auto__3 = u.e__GT_v(event);
if (squint_core.truth_(or__24281__auto__3)) {
return or__24281__auto__3;} else {
return event;}
})(), ({ "replace": squint_core.conj(ident, field) }));
});
f2.cljs$core$IFn$_invoke$arity$3 = (function (this$, field, event) {
return t.set_field_BANG_(this$._ctx, (() => {
const or__24281__auto__4 = u.e__GT_v(event);
if (squint_core.truth_(or__24281__auto__4)) {
return or__24281__auto__4;} else {
return event;}
})(), ({ "replace": squint_core.conj(this$.ident(), field) }));
});
f2.cljs$lang$maxFixedArity = 4;
return f2;
})();
var viewer_ident = function (this$) {
return t.viewer_ident(this$._ctx);
};
var viewer_QMARK_ = function (this$, acc_id) {
return t.viewer_QMARK_(this$._ctx, acc_id);
};
class Comp {
ctx;
static query;
_data;
ident;
  constructor(ctx_in) {
const self__ = this;
const this$ = this;
self__.ctx = ctx_in;
  }
static get_query() { 
const _ = this;
const self__ = this;return 1;
}
data() { 
const _ = this;
const self__ = this;return self__._data;
}
_query() { 
const this$ = this;
const self__ = this;return this$.query;
}
render(ident) { 
const this$ = this;
const self__ = this;return null;
}};
var comp_factory = function (p__6, ctx) {
const map__12 = p__6;
const comp3 = map__12;
const cla4 = squint_core.get(map__12, "cla");
const body5 = squint_core.get(map__12, "body");
return function () {
return null;
};
};
var new_data = function (this$) {
return this$.new_data();
};
var mutate_BANG_ = function (this$, mutate_map) {
const local1 = squint_core.get(mutate_map, "local");
const add2 = (() => {
const or__24281__auto__3 = squint_core.get(local1, "add");
if (squint_core.truth_(or__24281__auto__3)) {
return or__24281__auto__3;} else {
return squint_core.get(mutate_map, "add");}
})();
const remote4 = squint_core.get(mutate_map, "remote");
const remove5 = (() => {
const or__24281__auto__6 = squint_core.get(local1, "remove");
if (squint_core.truth_(or__24281__auto__6)) {
return or__24281__auto__6;} else {
return squint_core.get(mutate_map, "remove");}
})();
const opts7 = ({ "append": squint_core.get((() => {
const or__24281__auto__8 = local1;
if (squint_core.truth_(or__24281__auto__8)) {
return or__24281__auto__8;} else {
return mutate_map;}
})(), "append"), "replace": squint_core.get((() => {
const or__24281__auto__9 = local1;
if (squint_core.truth_(or__24281__auto__9)) {
return or__24281__auto__9;} else {
return mutate_map;}
})(), "replace") });
if (squint_core.truth_(add2)) {
squint_core.println("running add with data: ", this$.new_data());
t.add_BANG_(this$._ctx, ((add2) === ("new")) ? (this$.new_data()) : (add2), opts7)};
if (squint_core.truth_(remove5)) {
return t.remove_ident_BANG_(this$._ctx, squint_core.get(mutate_map, "from"), remove5);}
};
var default$ = Comp;
var useContext = solid.useContext;
var pull = n.pull;
var createMemo = solid.createMemo;
var createSignal = solid.createSignal;
var ident_QMARK_ = u.ident_QMARK_;
var uuid = u.uuid;

export { Comp, createMemo, new_data, set_BANG_, ident_QMARK_, createSignal, uuid, pull, remotes, useContext, viewer_ident, comp_factory, mutate_BANG_, viewer_QMARK_ }
export default default$
