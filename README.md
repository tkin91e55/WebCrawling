# WebCrawling

(1)The remaster branch is doing web crawling tutorgroup, ectutor, l4tutor

(2) Need to separate the config and model ,using bash csv parser

Installation for remaster branch:
====
1. sudo apt-get install postfix mailutils libsasl2-2 ca-certificates libsasl2-modules
2. Then take reference from: http://askubuntu.com/questions/522431/how-to-send-an-email-using-command-line
3. create a "config.csv" file, the **sample format** can be seen as the table below. To see exact sample, go to **ecWeb** branch, sample\_config.csv
4. install "awk-csv-parser": https://github.com/geoffroy-aubry/awk-csv-parser (remember to add the alias as prompted in the installation guide)
5. install "ant": by "sudo apt-get install ant"
6. edit the WORKING\_PATH variable in TutorNotification.sh
  Example: **/abs/path/to/your/working/path/** (don't miss the last '/')
  7. Then set "crobtab -e" to run TutorNotification.sh, if want to run the job periodically
    Example: append "30 9-23 * * * /bin/bash /abs/path/to/working/path/TutorNotification.sh" to your cron config

    **Note** the first two steps are to enable you to send email by cmd line, you don't need it if you know you have already

    Sample of config file
    ====
    WEBSITE|TYPE|VALUE
    ----|----|----
    WEBSITE|WC\_RECIPIENT|"sample@sample.com"
    WEBSITE|WC\_RECIPIENT|"sample2@sample.com"
    WEBSITE|WC\_SEARCH\_CRIT|"someWord"
    WEBSITE|WC\_SEARCH\_CRIT|"someWord2"
    WEBSITE|WC\_URL|"http://something2.com"
    WEBSITE|WC\_URL|"http://something.com"
    WEBSITE|WC\_WORKPATH|"/path/in/abs/to/the/working/directory/"
    WEBSITE|WC\_SEARCH\_COND\_PRICE\_ABOVE|225
    WEBSITE|WC\_SEARCH\_OUT\_CRIT|somewhere\_to\_delete

    **Note** at WC\_WORKPATH row, don't miss the last '/' at end of path

    **Note** at WC\_SEARCH\_COND\_PRICE\_ABOVE, value -1 is reserved

    Other
    ====
    * Make sure your system date is correct, synced
    * Make sure your mail system is operatin
