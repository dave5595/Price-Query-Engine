# Price-Query-Engine
Query engine that supports high-level string query filtering.

## Application Flow And Overview
![Application Overview And Flow Diagram](images/Application%20Flow%20Diagram.png)

1. Price quotes in the form of a csv file is fed into the `PriceQueryEngine` 
2. `PriceQueryEngine` parses the file and creates a `List<SidedPrice>`
3. `InputParser` validates the input queries and converts them into an intermediate `List<RawQuery>`
4. The `List<RawQuery>` is then further reduced into a `Predicate`, using the keys which maps to the fields that needs to be included in the `Predicate`. Each query maps to its own `Predicate`, and these individual query `Predicates` are joined by an `AND` operator to create a single `Predicate`. Multiple values of a single query are combined using an OR operator. (Note: This imposes a limitation on the application to only be able to support conjunctive queries(e.g `age >= lower bound && age <= upper bound`), disjunctive queries(e.g: `age < lower bound || age > upper bound`) will not yield the expected results using this logic)
5. The Predicate is then applied to the `List<SidedPrice>` 
6. Result is represented as an `Output` that can be configured to output its `toString` in CSV/Table format

## How to Run the Tests

To run the tests, follow these steps:

1. Run the following command in the terminal: `./gradlew test`. Alternatively, you can use IntelliJ's Gradle taskbar to run the tests.

These are the location of the test files:

- Core logic test: `src/test/kotlin/SimplePriceQueryEngineTest`
- Input parsing and data loading tests:
   - `src/test/kotlin/DataLoaderTest`
   - `src/test/kotlin/InputParserTest`

## Assumptions

The engine assumes the following about the input data:

1. The timestamp provided conforms to the fixed pattern of `yyyyMMddHHmmssSSS`.
2. The query string will only contain `;` to separate queries and `,` to separate multiple values in a single query.
3. The average price mentioned in the statement "Filter outliers that are more than x% off the average" refers to sided price averages instead of the total price average. For example, the percentage off the average price for a bid sided price is calculated using the following formula `((bidPx - avgBidPx) / avgBidPx) * 100` and similarly for percentage off the average price for an ask sided price is calculated using `((askPx - avgAskPx) / avgAskPx) * 100`.

## Features

1. Supports multi-value query:
    - Example: `source = citi,dbs,reuters; symbol = EURUSD, USDJPY`
2. Supports repeating groups to perform conjunctive queries:
    - Example: `source = citi; age > 10ms; age <= 100ms`
3. Supports outputting results in Table or CSV form.
4. Supports input validation on the query string.
5. Supports the generation of input PriceQuotes programmatically.
6. Supports specifying a TimeProvider for Age queries for testing purposes.

## Limitations

1. Does not support disjunctive queries.
2. Does not support more declarative `AND`, `OR`, `NOT` operators.
3. Only supports querying by milliseconds for price age.
4. Only certain fields support multi-value querying. For example, Source and Symbol.

                 
