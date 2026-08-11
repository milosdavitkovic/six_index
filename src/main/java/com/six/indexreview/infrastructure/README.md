# Infrastructure layer

## Overview

The infrastructure layer provides the adapters that connect the domain and application layers to configuration, CSV files, and the database.

## Components

- `config`: binds `index-review` properties and provides index definitions, precision, and the UTC review clock.
- `csv`: parses imported dataset files into typed input rows.
- `persistence`: contains JPA entities, repositories, mappers, and the review snapshot loader.

## Notes

- External formats and Spring Data/JPA types stay outside the domain model.
