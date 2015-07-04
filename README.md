# WebCrawling

(1)The master branch is doing web crawling for phys, math condition on http://tutorgroup.hk/tutor/index.php

(2) Need to separate the config and model ,using bash csv parser

Installation:
====
1. sudo apt-get install postfix mailutils libsasl2-2 ca-certificates libsasl2-modules
2. Then take reference from: http://askubuntu.com/questions/522431/how-to-send-an-email-using-command-line
3. create a "config.csv" file, the **sample format** is:

  TYPE|VALUE
--|--
WC\*_\*RECIPIENT|"sample@sample.com"
WC_RECIPIENT|"sample2@sample.com"
WC_SEARCH_CRIT|"someWord"
WC_SEARCH_CRIT|"someWord2"
WC_URL|"http://something2.com"
WC_URL|"http://something.com"
WC_WORKPATH|"/path/in/abs/to/the/working/directory"

4. install "awk-csv-parser": https://github.com/geoffroy-aubry/awk-csv-parser (remember to add the alias as prompted in the installation guide)
5. bash build.sh
6. bash run.sh
7. edit the WORKING_PATH in TutorGroupNotification.sh
8. Then set "crobtab -e" to run TutorGroupNotification.sh, if want to run the job periodically
