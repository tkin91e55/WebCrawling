# WebCrawling

(1)The master branch is doing web crawling for phys, math condition on http://tutorgroup.hk/tutor/index.php
(2) Need to separate the config and model ,using bash csv parser

Installation:

(i)sudo apt-get install postfix mailutils libsasl2-2 ca-certificates libsasl2-modules
(ii)Then take reference from: http://askubuntu.com/questions/522431/how-to-send-an-email-using-command-line
(iii) create a "config.csv" file, the sample format is:

  1 TYPE,VALUE
  2 WC_RECIPIENT,"sample@sample.com"
  3 WC_RECIPIENT,"sample2@sample.com
  4 WC_SEARCH_CRIT,"someWord"
  5 WC_SEARCH_CRIT,"someWord2"
  6 WC_URL,"http://something2.com"
  7 WC_URL,"http://something.com"
  8 WC_WORKPATH,"/path/in/abs/to/the/working/directory"

()Then set "crobtab -e", if want to run the job periodically
