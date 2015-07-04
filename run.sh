#!bin/bash

lastIdx=$(tail -n 1 last_index.csv | cut -f 2 -d',')
echo "[run.sh] The last time index is ${lastIdx}" >> TutorLogger
lastIdx=$(($lastIdx+1))
echo "[run.sh] The starting index is ${lastIdx}" >> TutorLogger

java -cp .:jsoup-1.8.2.jar:commons-collections4-4.0.jar:commons-csv-1.1.jar CrawlECTutor ${lastIdx}
