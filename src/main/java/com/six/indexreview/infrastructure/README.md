# Infrastructure layer

Adapters that connect the domain/application layers to configuration, CSV files, and the database. The subpackages deliberately keep external formats and Spring Data/JPA types outside the domain model.

- `config`: binds `index-review` properties and provides index definitions, precision, and the UTC review clock.
- `csv`: parses imported dataset files into input rows.
- `persistence`: JPA entities, repositories, mappers, and the review snapshot loader.
