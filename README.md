# Price-Query-Engine
Query engine that supports high-level string query filtering.

## Application Flow And Overview
![Application Flow Diagram](images/Application%20Flow%20Diagram.png)

1. Price quotes in the form of a csv file is fed into the `PriceQueryEngine`
2. `PriceQueryEngine` parses the file and creates a `List<SidedPrice>`
3. `InputParser` validates the input queries and converts them into an intermediate `List<RawQuery>`
4. The `List<RawQuery>` is then further reduced into a `Predicate`, using the keys which maps to the fields that needs to be included in the `Predicate`.
   Each query maps to its own `Predicate`, and these individual query `Predicates` are joined by an `AND` operator to create a single `Predicate`.
   Multiple values of a single query are combined using an `OR` operator.

   >**Note**  
   This imposes a limitation on the application to only be able to support
   **Conjunctive** queries where `age >=lower bound; age<=upper bound` translates to (`age >= lower bound AND age <= upper bound`)
   **Disjunctive** queries ( `age <= lower bound OR age >= upper bound` ) are not supported by default and queries such as
   `age <= lower bound; age>= upper bound`) will not yield the expected results as this translates to `age <= lower bound AND age>= upper bound`

5. The Predicate is then applied to the `List<SidedPrice>`
6. Result is represented as an `Output` that can be configured to output its `toString` in CSV/Table format

## How to Run the Tests

Open the terminal and run the following command: `./gradlew test`.

Alternatively, you can use IntelliJ's Gradle taskbar to run the tests.

To run tests using IntelliJ's Gradle taskbar:

1. View the Gradle tool window (View > Tool Windows > Gradle).
2. Expand the Gradle tasks.
3. Expand the `verification` group.
4. Click on `test` to start the task

These are the location of the test files:

- Core logic test: `src/test/kotlin/SimplePriceQueryEngineTest`
- Input parsing and data loading tests:
    - `src/test/kotlin/DataLoaderTest`
    - `src/test/kotlin/InputParserTest`

## Assumptions

The engine assumes the following about the input data and logic expected:

1. The timestamp provided conforms to the fixed pattern of `yyyyMMddHHmmssSSS`.
2. The input file containing price quotes should be in csv format
3. The query string will only contain `;` to separate queries and `,` to separate multiple values in a single query.
4. The average price mentioned in the statement "Filter outliers that are more than x% off the average" refers to sided price averages instead of the total price average. </br>
   >**Note**  
   Therefore, The percentage off the average price for a bid `SidedPrice` is calculated using the following formula `((bidPx - avgBidPx) / avgBidPx) * 100` instead of `((bidPx - totalAvgPx) / totalAvgPx) * 100`
   and similarly for percentage off the average price for an ask `SidedPrice` is calculated using `((askPx - avgAskPx) / avgAskPx) * 100` instead of `((askPx - totalAvgPx) / totalAvgPx) * 100`.

## Features

1. Supports multi-valued queries:
    - Example: `source = citi,dbs,reuters; symbol = EURUSD, USDJPY`
2. Supports repeating groups to perform conjunctive queries:
    - Example: `source = citi; age > 10ms; age <= 100ms`
3. Supports outputting results in Table or CSV format.
4. Supports input validation on the query string.
5. Supports the generation of input `PriceQuote`'s programmatically.
6. Supports specifying a `TimeProvider` for Age queries for testing purposes.
7. Provides thread-safe implementation of `QueryEngine` with `SimplePriceQueryEngine`

## Limitations

1. Does not support disjunctive queries.
2. Does not support declarative `AND`, `OR`, `NOT` operators.
3. Only supports querying by milliseconds for price age.
4. Only certain fields support multi-value querying. For example, `Source` and `Symbol`.

                 
