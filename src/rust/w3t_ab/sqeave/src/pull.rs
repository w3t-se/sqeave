use crate::model::{AttrValue, Db, Entity, Ident};
use serde_json::json;

#[derive(Default)]
pub struct PullDebug {
    pub enabled: bool,
    pub logs: Vec<String>,
}

impl PullDebug {
    pub fn on() -> Self {
        Self {
            enabled: true,
            logs: vec![],
        }
    }

    pub fn off() -> Self {
        Self::default()
    }

    pub fn log(&mut self, msg: impl Into<String>) {
        if self.enabled {
            let msg = msg.into();
            eprintln!("[pull] {msg}");
            self.logs.push(msg);
        }
    }
}

#[derive(Clone, Debug)]
pub enum PullQuery {
    Key(String),
    Vec(Vec<PullQuery>),
    Join(String, Box<PullQuery>),
    Raw,
}

pub fn parse_pull_query(v: serde_json::Value) -> PullQuery {
    match v {
        serde_json::Value::String(s) => PullQuery::Key(s),

        serde_json::Value::Array(items) => {
            PullQuery::Vec(items.into_iter().map(parse_pull_query).collect())
        }

        serde_json::Value::Object(mut obj) => {
            if let Some(k) = obj.keys().next().cloned() {
                let sub = obj.remove(&k).unwrap_or(serde_json::Value::Null);
                PullQuery::Join(k, Box::new(parse_pull_query(sub)))
            } else {
                PullQuery::Raw
            }
        }

        _ => PullQuery::Raw,
    }
}

pub fn pull(db: &Db, entity: AttrValue, query: PullQuery) -> AttrValue {
    let mut dbg = PullDebug::off();
    pull_inner(db, entity, query, &mut dbg)
}

pub fn pull_with_debug(
    db: &Db,
    entity: AttrValue,
    query: PullQuery,
) -> serde_json::Value {
    let mut dbg = PullDebug::on();

    dbg.log(format!("wanted pull entity={entity:?} query={query:?}"));

    let result = pull_inner(db, entity, query, &mut dbg);

    json!({
        "result": crate::convert::attr_to_value(result),
        "logs": dbg.logs
    })
}

fn pull_inner(
    db: &Db,
    entity: AttrValue,
    query: PullQuery,
    dbg: &mut PullDebug,
) -> AttrValue {
    let e = resolve_entity(db, entity, dbg);

    match e {
        AttrValue::Null => AttrValue::Null,

        AttrValue::Ident(i) => {
            let resolved = resolve_ident(db, &i, dbg);
            pull_inner(db, resolved, query, dbg)
        }

        AttrValue::List(items) => {
            AttrValue::List(
                items
                    .into_iter()
                    .map(|x| pull_inner(db, x, query.clone(), dbg))
                    .collect(),
            )
        }

        AttrValue::Map(map) => pull_map(db, map, query, dbg),

        other => other,
    }
}

fn pull_map(
    db: &Db,
    map: std::collections::BTreeMap<String, AttrValue>,
    query: PullQuery,
    dbg: &mut PullDebug,
) -> AttrValue {
    match query {
        PullQuery::Key(k) => map.get(&k).cloned().unwrap_or(AttrValue::Null),

        PullQuery::Vec(items) => {
            let mut out = std::collections::BTreeMap::new();

            for q in items {
                match q {
                    PullQuery::Key(k) => {
                        if let Some(v) = map.get(&k) {
                            out.insert(k, v.clone());
                        }
                    }

                    PullQuery::Join(k, sub) => {
                        if let Some(v) = map.get(&k) {
                            out.insert(k, pull_join(db, v.clone(), *sub, dbg));
                        }
                    }

                    PullQuery::Raw => {}
                    PullQuery::Vec(_) => {}
                }
            }

            AttrValue::Map(out)
        }

        PullQuery::Join(k, sub) => {
            if let Some(v) = map.get(&k) {
                let mut out = std::collections::BTreeMap::new();
                out.insert(k, pull_join(db, v.clone(), *sub, dbg));
                AttrValue::Map(out)
            } else {
                AttrValue::Null
            }
        }

        PullQuery::Raw => AttrValue::Map(map),
    }
}

fn pull_join(
    db: &Db,
    value: AttrValue,
    query: PullQuery,
    dbg: &mut PullDebug,
) -> AttrValue {
    match value {
        AttrValue::Ident(i) => {
            let resolved = resolve_ident(db, &i, dbg);
            pull_inner(db, resolved, query, dbg)
        }

        AttrValue::List(items) => {
            AttrValue::List(
                items
                    .into_iter()
                    .map(|x| pull_inner(db, x, query.clone(), dbg))
                    .collect(),
            )
        }

        other => pull_inner(db, other, query, dbg),
    }
}

fn resolve_entity(db: &Db, entity: AttrValue, dbg: &mut PullDebug) -> AttrValue {
    match entity {
        AttrValue::Ident(i) => resolve_ident(db, &i, dbg),
        other => other,
    }
}

fn resolve_ident(db: &Db, ident: &Ident, dbg: &mut PullDebug) -> AttrValue {
    dbg.log(format!(
        "resolve ident table={} id={}",
        ident.table, ident.id
    ));

    db.get(&ident.table)
        .and_then(|t| t.get(&ident.id))
        .map(entity_to_attr)
        .unwrap_or(AttrValue::Null)
}

fn entity_to_attr(entity: &Entity) -> AttrValue {
    AttrValue::Map(entity.attrs.clone())
}
