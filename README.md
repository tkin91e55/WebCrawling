# WebCrawling

(1)The master branch is doing web crawling for phys, math condition on http://tutorgroup.hk/tutor/index.php
(2) The ecWeb branch is doing web crawling for another: ectutor
(3) Need to separate the config and model ,using bash csv parser

Installation:

(i)sudo apt-get install postfix mailutils libsasl2-2 ca-certificates libsasl2-modules
(ii)Then take reference from: http://askubuntu.com/questions/522431/how-to-send-an-email-using-command-line
(iii) create a "config.csv" file, the sample format is:

TYPE,VALUE
WC_RECIPIENT,"sample@sample.com"
WC_RECIPIENT,"sample2@sample.com
WC_SEARCH_CRIT,"someWord"
WC_SEARCH_CRIT,"someWord2"
WC_URL,"http://something2.com"
WC_URL,"http://something.com"
WC_WORKPATH,"/path/in/abs/to/the/working/directory"

(iv) install "awk-csv-parser": https://github.com/geoffroy-aubry/awk-csv-parser
(v) bash build.sh
(vi) bash run.sh
(vii)Then set "crobtab -e", if want to run the job periodically
