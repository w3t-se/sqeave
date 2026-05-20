use crate::model::{Entity, Ident};
use serde_json::json;

pub fn execute_node(ident: &Ident, entity: &Entity) -> Vec<serde_json::Value> {
    match ident.table.as_str() {
        "data/id" => execute_data_node(ident),
        "query/id" => execute_query_node(ident),
        "transformation/id" => execute_transformation_node(ident, entity),
        _ => vec![],
    }
}

fn execute_data_node(ident: &Ident) -> Vec<serde_json::Value> {
    vec![json!({
        "data/id": ident.id,
        "data/status": "ready",
        "data/runtime-ready?": true
    })]
}

fn execute_query_node(ident: &Ident) -> Vec<serde_json::Value> {
    vec![json!({
        "query/id": ident.id,
        "query/status": "ready"
    })]
}

fn execute_transformation_node(
    ident: &Ident,
    _entity: &Entity,
) -> Vec<serde_json::Value> {
    let result_id = format!("result-for-{}", ident.id);

    vec![json!({
        "data/id": result_id,
        "data/type": "table",
        "data/name": "transformation result",
        "data/parent": ["transformation/id", ident.id],
        "data/ref": {
            "runtime": "rust",
            "table": format!("{}_latest", ident.id)
        },
        "data/preview": [],
        "data/stats": {
            "row-count": 0
        }
    })]
}
