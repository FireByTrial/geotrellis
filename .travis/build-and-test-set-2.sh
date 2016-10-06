#!/bin/bash

./sbt -J-Xmx2G "++$TRAVIS_SCALA_VERSION" "project geowave" compile || { exit 1; }
./sbt -J-Xmx2G "++$TRAVIS_SCALA_VERSION" "project cassandra" test  || { exit 1; }
./sbt -J-Xmx2G "++$TRAVIS_SCALA_VERSION" "project vector-test" test || { exit 1; }
./sbt -J-Xmx2G "++$TRAVIS_SCALA_VERSION" "project raster-test" test || { exit 1; }
./sbt -J-Xmx2G "++$TRAVIS_SCALA_VERSION" "project s3" test  || { exit 1; }
./sbt -J-Xmx2G "++$TRAVIS_SCALA_VERSION" "project spark-etl" compile  || { exit 1; }
./sbt -J-Xmx2G "++$TRAVIS_SCALA_VERSION" "project slick" test || { exit 1; }
./sbt -J-Xmx2G "++$TRAVIS_SCALA_VERSION" "project vectortile" test || { exit 1; }
