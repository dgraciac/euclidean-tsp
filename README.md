# euclidean-tsp

## TSPLIB input substitution steps

With decimals:

* `[1-9]([0-9]+)? `
* `[1-9][0-9]*.[0-9]+[ ]`
* `,[1-9][0-9]*.[0-9]+`

Without decimals:

* remove indices with multiline tool edit mode in intellij
* `[1-9]([0-9]+)?`
* `([1-9]([0-9]+)?).0 `
* `,[ ]*([1-9]([0-9]+)?).0`
