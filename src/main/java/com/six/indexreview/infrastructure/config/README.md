# Configuration

`IndexReviewProperties` binds `index-review.precision` and `index-review.indices`. Each `IndexConfiguration` defines index code/name, constituent count, maximum weight, ranking/selection/buffer rules, review period and dates, enablement, tie-breakers, and buffer retention rank.

`IndexDefinitionProvider` resolves an index case-insensitively, rejects disabled indexes or mismatched review periods, and creates the domain `IndexDefinition`. `IndexReviewConfiguration` supplies the configured `PrecisionPolicy` and a UTC `Clock`.

The sample configuration enables SMI for `Q3-2026` (20 constituents, maximum weight `0.18`) and defines SMIM as disabled. Precision defaults to internal scale 16, output scale 10, and `HALF_UP` rounding.
