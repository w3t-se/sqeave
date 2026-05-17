const p=require('../package.json');

for (const [k,v] of Object.entries(p.exports)) {
    const f=typeof v==='string'?v:v.import;
    require('fs').accessSync(f);
    console.log('ok', k, f)
}
