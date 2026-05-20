use crate::convert::{value_get_ident, value_to_attr};
use crate::model::{AttrValue, Db, Entity};
use serde_json::Value;
use std::collections::BTreeMap;

pub struct NormalizeResult {
    pub acc: Db,
    pub root: AttrValue,
}

pub fn normalize(input: Value) -> NormalizeResult {
    let mut acc = BTreeMap::new();
    let root = walk(input, &mut acc);
    NormalizeResult { acc, root }
}

fn walk(v: Value, acc: &mut Db) -> AttrValue {
    match v {
        Value::Array(items) => {
            // IMPORTANT: preserve ident vectors as AttrValue::Ident.
            let raw = Value::Array(items.clone());
            if let AttrValue::Ident(_) = value_to_attr(raw) {
                return value_to_attr(Value::Array(items));
            }

            AttrValue::List(items.into_iter().map(|x| walk(x, acc)).collect())
        }

        Value::Object(obj) => {
            let original = Value::Object(obj.clone());
            let ident = value_get_ident(&original);

            let mut attrs = BTreeMap::new();

            for (k, v) in obj {
                attrs.insert(k, walk(v, acc));
            }

            if let Some(ident) = ident {
                attrs.insert(
                    ident.table.clone(),
                    AttrValue::String(ident.id.clone()),
                );

                let entity = Entity {
                    ident: ident.clone(),
                    attrs,
                };

                put_entity(acc, entity);

                AttrValue::Ident(ident)
            } else {
                AttrValue::Map(attrs)
            }
        }

        other => value_to_attr(other),
    }
}

fn put_entity(acc: &mut Db, entity: Entity) {
    let table = entity.ident.table.clone();
    let id = entity.ident.id.clone();

    let tbl = acc.entry(table).or_default();

    let merged = match tbl.remove(&id) {
        Some(mut old) => {
            for (k, v) in entity.attrs {
                old.attrs.insert(k, v);
            }
            old
        }
        None => entity,
    };

    tbl.insert(id, merged);
}
