docker run --name mongo -p 27017:27017  -d mongo

truncate kafak: bin/kafka-consumer-groups.sh --bootstrap-server 193.26.156.80:9092 --group purple-tiger --reset-offsets --to-earliest --all-topics --execute

bin/kafka-consumer-groups.sh --bootstrap-server 193.26.156.80:9092 --all-groups --reset-offsets --to-earliest --all-topics --execute
