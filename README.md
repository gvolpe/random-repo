# random-repo
-------------

Airports data from http://ourairports.com/data/ (released under public domain)

## Application

Run the following command to start the web server:
> sbt "runMain com.gvolpe.lunatech.http.HttpApi"

Before start using the API you will need to invoke the indexing service.

## API

### Resources
POST http://localhost:8080/index

GET  http://localhost:8080/report

GET  http://localhost:8080/search

    Optional parameters:

        * name: to search by country name
        * code: to search by country code

### Resources depending on Elasticsearch

GET  http://localhost:8080/es/search

    Optional parameters: Same as the standard search

### Examples:
GET  http://localhost:8080/search?name=argentina

GET  http://localhost:8080/search?code=AR

## Elasticsearch

This app uses the version 5.0.1 which you can get [here](https://www.elastic.co/downloads/past-releases/elasticsearch-5-0-1).

Uncompress the file, go to the container folder and run the following command to start elasticsearch:
> bin/elasticsearch

### Indexing files on Elasticsearch

This process will take about 8 minutes:
> sbt "runMain com.gvolpe.lunatech.elasticsearch.ESIndexingManager"

Once is done, you can start searching on ES.
