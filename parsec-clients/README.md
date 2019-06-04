# Parsec Client
The Parsec Client Library provides an async HTTP client that supports the following:

+ Retry by response HTTP status code
+ Cookie and header back posting
+ Short duration session/response cache (for GET method only)
+ Profiling logs (connection time, and etc)
+ Splunk compatible log

## Basic Usage Example
Code example for basic usage example:
```java
// Initializing a client
ParsecAsyncHttpClient client = new ParsecAsyncHttpClient.Builder().build();

// Initialize a GET request
ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
    .setUrl("http://parsec.test.yahoo.com:4080")
    .addQueryParam("mid", "12345")  // Adding a query parameter
    .addHeader("X-ESI", "1")        // Adding a header
    .build();

// Executing a request
Future<Response> future = client.execute(request);

// Initialize a POST request
ParsecAsyncHttpRequest postRequest = new ParsecAsyncHttpRequest.Builder()
    .setMethod("POST")
    .setUrl("http://parsec.test.yahoo.com:4080")
    .addFormParam("title", "My title")    // Adding form parameters
    .build();

// Executing a blocking request
Response response = client.execute(postRequest).get();

```

## Asynchronous and Blocking Requests
Both ParsecAsyncHttpClient.execute and ParsecAsycHttpClient.criticalExecute returns CompletableFuture and is therefore
asynchronous / non-blocking by nature. If you need to make blocking calls, please use CompletableFuture.get method.

## Retrying Requests by Response HTTP Status Code
Requests can be retried based on the response's HTTP status code. To add or remove a retry HTTP status code, please
use ParsecAsyncHttpRequest.Builder.addRetryStatusCode and ParsecAsyncHttpRequest.Builder.removeRetryStatusCode method.
The maximum number of **total** retries for all status code can be controlled using ParsecAsyncHttpRequest.Builder.setMaxRetries method.

For example, the following code will create a request object that retries response status code 404 and 500 for a total of 2 times:
```java
ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
    .setUrl("http://tw.yahoo.com")
    .addRetryStatusCode(404)
    .addRetryStatusCode(500)
    .setMaxRetries(2)
    .build();
```

## In Memory Short Duration Response Cache
By default the client enables an in memory short duration loading cache for GET requests. This mean for all identical GET requests that occur in a 2 seconds window,
only the first request will be executed while all remaining requests will be responded from cache. If fresh copy of the data is required (for example,
during a get update scenario), please use ParsecAsyncHttpClient.criticalExecute method or ParsecAsyncHttpRequest.Builder.setCriticalGet method.
