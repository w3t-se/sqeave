mod convert;
mod execute;
mod graph;
mod model;
mod normalize;
mod pull;

use crate::convert::{attr_to_value, value_to_attr};
use crate::execute::execute_node;
use crate::graph::GraphIndex;
use crate::model::{AttrValue, Db, Entity, Ident};
use crate::normalize::{normalize, NormalizeResult};
use serde_json::Value;
use wasm_bindgen::prelude::*;

#[derive(Default)]
#[wasm_bindgen]
pub struct SeebraState {
    db: Db,
    graph: GraphIndex,
}

#[wasm_bindgen]
impl SeebraState {
    #[wasm_bindgen(constructor)]
    pub fn new() -> SeebraState {
        SeebraState::default()
    }

    pub fn add(&mut self, input_js: JsValue) -> Result<JsValue, JsValue> {
        let input: Value = serde_wasm_bindgen::from_value(input_js).map_err(js_err)?;
        let root = self.add_value(input);

        serde_wasm_bindgen::to_value(&attr_to_value(root)).map_err(js_err)
    }

    pub fn execute_changed(&mut self) -> Result<JsValue, JsValue> {
        let txs = self.execute_changed_values();
        serde_wasm_bindgen::to_value(&txs).map_err(js_err)
    }

    pub fn pull(&self, entity_js: JsValue, query_js: JsValue) -> Result<JsValue, JsValue> {
        let entity_v: Value = serde_wasm_bindgen::from_value(entity_js).map_err(js_err)?;
        let query_v: Value = serde_wasm_bindgen::from_value(query_js).map_err(js_err)?;

        let entity = value_to_attr(entity_v);
        let query = pull::parse_pull_query(query_v);
        let result = pull::pull(&self.db, entity, query);

        serde_wasm_bindgen::to_value(&attr_to_value(result)).map_err(js_err)
    }

    pub fn pull_debug(&self, entity_js: JsValue, query_js: JsValue) -> Result<JsValue, JsValue> {
        let entity_v: Value = serde_wasm_bindgen::from_value(entity_js).map_err(js_err)?;
        let query_v: Value = serde_wasm_bindgen::from_value(query_js).map_err(js_err)?;

        let entity = value_to_attr(entity_v);
        let query = pull::parse_pull_query(query_v);
        let result = pull::pull_with_debug(&self.db, entity, query);

        serde_wasm_bindgen::to_value(&result).map_err(js_err)
    }

    pub fn snapshot(&self) -> Result<JsValue, JsValue> {
        let mut out = serde_json::Map::new();

        for (table, rows) in &self.db {
            let mut t = serde_json::Map::new();

            for (id, entity) in rows {
                t.insert(
                    id.clone(),
                    crate::convert::entity_to_value(entity.clone(), true),
                );
            }

            out.insert(table.clone(), Value::Object(t));
        }

        serde_wasm_bindgen::to_value(&Value::Object(out)).map_err(js_err)
    }

    pub fn replace_state(&mut self, input_js: JsValue) -> Result<(), JsValue> {
	let input: serde_json::Value =
            serde_wasm_bindgen::from_value(input_js).map_err(js_err)?;

	self.db.clear();
	self.graph = GraphIndex::default();

	self.import_normalized_state(input);

	Ok(())
    }

}


impl SeebraState {

    fn import_normalized_state(&mut self, input: serde_json::Value) {
	let Some(obj) = input.as_object() else {
            return;
	};

	for (table, rows_v) in obj {
            if !crate::model::is_ident_key(table) {
		continue;
            }

            let Some(rows) = rows_v.as_object() else {
		continue;
            };

            for (id, row_v) in rows {
		let mut row = row_v.clone();

		if let serde_json::Value::Object(ref mut m) = row {
                    m.insert(table.clone(), serde_json::Value::String(id.clone()));
		}

		if let Some(entity) = crate::convert::value_to_entity(row) {
                    self.merge_entity(entity);
		}
            }
	}
    }

    pub fn add_value(&mut self, input: Value) -> AttrValue {
        let NormalizeResult { acc, root } = normalize(input);

        for (_, rows) in acc {
            for (_, entity) in rows {
                self.merge_entity(entity);
            }
        }

        root
    }

    fn merge_entity(&mut self, entity: Entity) {
        let ident = entity.ident.clone();

        let table = ident.table.clone();
        let id = ident.id.clone();

        let existing = self.db.entry(table.clone()).or_default().remove(&id);

        let merged = match existing {
            Some(mut old) => {
                for (k, v) in entity.attrs {
                    old.attrs.insert(k, v);
                }
                old
            }

            None => entity,
        };

        self.graph.reindex_entity(&merged);
        self.graph.bump_version(&ident);
        self.graph.mark_dirty_downstream(&ident);

        self.db.entry(table).or_default().insert(id, merged);
    }

    pub fn execute_changed_values(&mut self) -> Vec<Value> {
        let mut frontend_txs = vec![];

        loop {
            let runnable = self.graph.next_runnable();

            if runnable.is_empty() {
                break;
            }

            for ident in runnable {
                self.graph.mark_running(&ident);

                if let Some(entity) = self
                    .db
                    .get(&ident.table)
                    .and_then(|t| t.get(&ident.id))
                    .cloned()
                {
                    let txs = execute_node(&ident, &entity);

                    for tx in &txs {
                        self.add_value(tx.clone());
                    }

                    frontend_txs.extend(txs);
                }

                self.graph.mark_done(&ident);
            }
        }

        frontend_txs
    }
}

fn js_err<E: ToString>(e: E) -> JsValue {
    JsValue::from_str(&e.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;
    use serde_json::json;

    #[test]
    fn add_normalizes_nested_entities() {
        let mut state = SeebraState::default();

        let root = state.add_value(json!({
            "query/id": "q1",
            "query/name": "Query 1",
            "query/result": {
                "data/id": "d1",
                "data/name": "Raw data",
                "data/content": {"huge": true}
            }
        }));

        assert_eq!(attr_to_value(root), json!(["query/id", "q1"]));
        assert!(state.db["query/id"].contains_key("q1"));
        assert!(state.db["data/id"].contains_key("d1"));

        let q1 = &state.db["query/id"]["q1"];
        assert_eq!(
            attr_to_value(q1.attrs["query/result"].clone()),
            json!(["data/id", "d1"])
        );
    }

    #[test]
    fn add_merges_rows_into_db() {
        let mut state = SeebraState::default();

        state.add_value(json!({
            "data/id": "d1",
            "data/name": "A"
        }));

        state.add_value(json!({
            "data/id": "d1",
            "data/type": "json"
        }));

        let row = &state.db["data/id"]["d1"];

        assert_eq!(
            attr_to_value(row.attrs["data/name"].clone()),
            json!("A")
        );

        assert_eq!(
            attr_to_value(row.attrs["data/type"].clone()),
            json!("json")
        );
    }

    #[test]
    fn graph_indexes_ident_edges() {
        let mut state = SeebraState::default();

        state.add_value(json!({
            "query/id": "q1",
            "query/datasource": ["datasource/id", "ds1"]
        }));

        let q = Ident::new("query/id", "q1");
        let ds = Ident::new("datasource/id", "ds1");

        assert!(state.graph.deps[&q].contains(&ds));
        assert!(state.graph.rdeps[&ds].contains(&q));
    }

    #[test]
    fn pull_nested_ident_join() {
        let mut state = SeebraState::default();

        state.add_value(json!({
            "datasource/id": "ds1",
            "datasource/name": "Allen API"
        }));

        state.add_value(json!({
            "query/id": "q1",
            "query/name": "Long-square sweep records",
            "query/datasource": ["datasource/id", "ds1"]
        }));

        let result = pull::pull(
            &state.db,
            value_to_attr(json!(["query/id", "q1"])),
            pull::parse_pull_query(json!([
                "query/name",
                {"query/datasource": ["datasource/name"]}
            ])),
        );

        assert_eq!(
            attr_to_value(result),
            json!({
                "query/name": "Long-square sweep records",
                "query/datasource": {
                    "datasource/name": "Allen API"
                }
            })
        );
    }

    #[test]
    fn execute_changed_creates_result_data_for_transformation() {
        let mut state = SeebraState::default();

        state.add_value(json!({
            "data/id": "d1",
            "data/name": "Raw"
        }));

        state.execute_changed_values();

        state.add_value(json!({
            "transformation/id": "t1",
            "transformation/input": ["data/id", "d1"]
        }));

        let txs = state.execute_changed_values();

        assert!(
            txs.iter().any(|tx| {
                tx.get("data/id") == Some(&json!("result-for-t1"))
            })
        );

        assert!(state.db["data/id"].contains_key("result-for-t1"));
    }

    #[test]
    fn pull_debug_prints_requested_pull() {
        let mut state = SeebraState::default();

        state.add_value(json!({
            "query/id": "q1",
            "query/name": "Query"
        }));

        let result = pull::pull_with_debug(
            &state.db,
            value_to_attr(json!(["query/id", "q1"])),
            pull::parse_pull_query(json!(["query/name"])),
        );

        assert_eq!(
            result["result"],
            json!({
                "query/name": "Query"
            })
        );

        assert!(result["logs"]
            .as_array()
            .unwrap()
            .iter()
            .any(|x| x.as_str().unwrap().contains("wanted pull entity")));
    }
}
