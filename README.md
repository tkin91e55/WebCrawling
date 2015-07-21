# WebCrawling

(1)The master branch is doing web crawling for phys, math condition on http://tutorgroup.hk/tutor/index.php

(2) Need to separate the config and model ,using bash csv parser

Installation for master branch:
====
1. sudo apt-get install postfix mailutils libsasl2-2 ca-certificates libsasl2-modules
2. Then take reference from: http://askubuntu.com/questions/522431/how-to-send-an-email-using-command-line
3. create a "config.csv" file, the **sample format** can be seen as the table below. To see exact sample, go to **ecWeb** branch, sample\_config.csv
4. install "awk-csv-parser": https://github.com/geoffroy-aubry/awk-csv-parser (remember to add the alias as prompted in the installation guide)
5. bash build.sh
6. bash run.sh
7. edit the WORKING\_PATH variable in TutorGroupNotification.sh
  Example: **/abs/path/to/your/working/path/** (don't miss the last '/')
8. Then set "crobtab -e" to run TutorGroupNotification.sh, if want to run the job periodically
  Example: append "30 9-23 * * * /bin/bash /abs/path/to/working/path/TutorGroupNotification.sh" to your cron config

**Note** the first two steps are to enable you to send email by cmd line, you don't need it if you know you have already

Sample of config file
====
TYPE|VALUE
----|----
WC\_RECIPIENT|"sample@sample.com"
WC\_RECIPIENT|"sample2@sample.com"
WC\_SEARCH\_CRIT|"someWord"
WC\_SEARCH\_CRIT|"someWord2"
WC\_URL|"http://something2.com"
WC\_URL|"http://something.com"
WC\_WORKPATH|"/path/in/abs/to/the/working/directory/"
WC\_SEARCH\_COND\_PRICE\_ABOVE|225
WC\_SEARCH\_OUT\_CRIT|somewhere\_to\_delete

**Note** at WC\_WORKPATH row, don't miss the last '/' at end of path

**Note** at WC\_SEARCH\_COND\_PRICE\_ABOVE, value -1 is reserved

Installtion for ecWeb or L4Tweb branch
====
1. After step 3 in "Installtion for master branch", you need:
  * create a "last\_index.csv" file and append "(start),155700" for ecWeb branch
  or append "(start),106900" for L4TWeb
