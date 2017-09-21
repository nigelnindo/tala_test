# Tala Scala 

Scala Project for Tala developer test. This document assumes that you already have Scala and `sbt` installed.

## Run Project

This will start the server locally on port `9000`:

```shell
$ sbt run
```

## Run Tests

```shell
$ sbt test
```

## Generate Test Coverage

Makes use of [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) to generate test coverage. Run the following commands to create a test coverage report:

```shell
$ sbt clean coverage test
$ sbt coverageReport
```

### Code Entry Point

[Boot.scala](src/main/scala/com/nigelnindo/tala/api/Boot.scala) is the main entry point for the application.


## API Examples

Examples below use [httpie](https://httpie.org/) to make API calls:

#### Get current balance

```shell 

$ http localhost:9000/balance

HTTP/1.1 200 OK
Content-Length: 15
Content-Type: application/json
Date: Thu, 21 Sep 2017 14:39:37 GMT
Server: akka-http/10.0.8

{
    "balance": 0.0
}
```

#### Make a deposit

```shell
$ http POST localhost:9000/deposit amount:=20000

HTTP/1.1 201 Created
Content-Length: 119
Content-Type: application/json
Date: Thu, 21 Sep 2017 14:41:19 GMT
Server: akka-http/10.0.8

{
    "amount": 20000.0,
    "refNo": "322f9897-c73a-4aea-98fb-b6c532ab1c45",
    "timestamp": 1506004879784,
    "transactionType": "deposit"
}
```

#### Make a withdrawal

```shell

$ http POST localhost:9000/withdraw amount:=15000

HTTP/1.1 201 Created
Content-Length: 120
Content-Type: application/json
Date: Thu, 21 Sep 2017 14:42:40 GMT
Server: akka-http/10.0.8

{
    "amount": 15000.0,
    "refNo": "6cbcf996-6c22-4a2f-b46c-8668dd5d38a6",
    "timestamp": 1506004960699,
    "transactionType": "withdraw"
}
```


