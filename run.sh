#!/bin/bash
./mvnw compile quarkus:dev -Ddebug=5011 -Dquarkus.http.host=0.0.0.0
