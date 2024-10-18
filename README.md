Reproducer that shows that exceptions mapped by a `ResponseExceptionMapper` in Quarkus 3.15.1, will not be wrapped in a `ClientWebApplicationException`.

Run the tests and they will fail. If you change the Quarkus version to 3.12.1 the tests will pass.