# WebCrawling

(1)The remaster branch is doing web crawling tutorgroup, ectutor, l4tutor

(2) Need to separate the config and model ,using bash csv parser

Installation for remaster branch:
====
1. sudo apt-get install postfix mailutils libsasl2-2 ca-certificates libsasl2-modules
2. Then take reference from: http://askubuntu.com/questions/522431/how-to-send-an-email-using-command-line
3. create a "config.csv" file, the **sample format** can be seen as the table below.
4. install "awk-csv-parser": https://github.com/geoffroy-aubry/awk-csv-parser (remember to add the alias as prompted in the installation guide)
5. install "ant": by "sudo apt-get install ant"
  6. Then set "crobtab -e" to run TutorNotification.sh, if want to run the job periodically
    Example: append "30 9-23 * * * /bin/bash /abs/path/to/working/path/TutorNotification.sh" to your cron config

    **Note** the first two steps are to enable you to send email by cmd line, you don't need it if you know you have already

    Sample of config file
    ====
WebKey|Type|Value
----|----|----
""|WC\_RECIPIENT|"sample1@sample.com"
""|WC\_RECIPIENT|"sample2@sample.com"
"ECTutor"|WC\_SEARCH\_CRIT|"物理"
"ECTutor"|WC\_SEARCH\_CRIT|"數學"
"ECTutor"|WC\_SEARCH\_CRIT|"生物"
"ECTutor"|WC\_SEARCH\_CRIT|"化學"
"ECTutor"|WC\_SEARCH\_CRIT|"科學"
"ECTutor"|WC\_SEARCH\_CRIT|"Math"
"ECTutor"|WC\_SEARCH\_CRIT|"math"
"ECTutor"|WC\_SEARCH\_CRIT|"Phy"
"ECTutor"|WC\_SEARCH\_CRIT|"phy"
"ECTutor"|WC\_SEARCH\_CRIT|"Chem"
"ECTutor"|WC\_SEARCH\_CRIT|"chem"
"ECTutor"|WC\_SEARCH\_CRIT|"M1"
"ECTutor"|WC\_SEARCH\_CRIT|"m1"
"ECTutor"|WC\_SEARCH\_CRIT|"M2"
"ECTutor"|WC\_SEARCH\_CRIT|"m2"
"ECTutor"|WC\_SEARCH\_CRIT|"Bio"
"ECTutor"|WC\_SEARCH\_CRIT|"bio"
"ECTutor"|WC\_SEARCH\_CRIT|"Science"
"ECTutor"|WC\_SEARCH\_CRIT|"science"
"ECTutor"|WC\_SEARCH\_CRIT|"IB"
"ECTutor"|WC\_SEARCH\_CRIT|"ib"
"ECTutor"|WC\_SEARCH\_CRIT|"GCSE"
"ECTutor"|WC\_SEARCH\_CRIT|"GCSE"
"ECTutor"|WC\_URL|"http://www.ectutor.com/popup\_case.php?id="
"ECTutor"|WC\_SEARCH\_COND\_PRICE\_ABOVE|130
"ECTutor"|WC\_INDEX\_URL|"http://www.ectutor.com/search.php?maxPage=10&cPage=1&infoType=2&infoSex=&subject\_1=&district\_1=&educ\_1=&tutor\_id="
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|觀塘
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|銅鑼灣
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|小西灣
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|沙田
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|將軍澳
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|馬鞍山
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|藍田
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|柴灣
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|北角
"ECTutor"|WC\_SEARCH\_OUT\_CRIT|鴨脷洲
"L4Tutor"|WC\_SEARCH\_CRIT|"物理"
"L4Tutor"|WC\_SEARCH\_CRIT|"數學"
"L4Tutor"|WC\_SEARCH\_CRIT|"生物"
"L4Tutor"|WC\_SEARCH\_CRIT|"化學"
"L4Tutor"|WC\_SEARCH\_CRIT|"科學"
"L4Tutor"|WC\_SEARCH\_CRIT|"Math"
"L4Tutor"|WC\_SEARCH\_CRIT|"math"
"L4Tutor"|WC\_SEARCH\_CRIT|"Phy"
"L4Tutor"|WC\_SEARCH\_CRIT|"phy"
"L4Tutor"|WC\_SEARCH\_CRIT|"Chem"
"L4Tutor"|WC\_SEARCH\_CRIT|"chem"
"L4Tutor"|WC\_SEARCH\_CRIT|"M1"
"L4Tutor"|WC\_SEARCH\_CRIT|"m1"
"L4Tutor"|WC\_SEARCH\_CRIT|"M2"
"L4Tutor"|WC\_SEARCH\_CRIT|"m2"
"L4Tutor"|WC\_SEARCH\_CRIT|"Bio"
"L4Tutor"|WC\_SEARCH\_CRIT|"bio"
"L4Tutor"|WC\_SEARCH\_CRIT|"Science"
"L4Tutor"|WC\_SEARCH\_CRIT|"science"
"L4Tutor"|WC\_SEARCH\_CRIT|"IB"
"L4Tutor"|WC\_SEARCH\_CRIT|"ib"
"L4Tutor"|WC\_SEARCH\_CRIT|"GCSE"
"L4Tutor"|WC\_SEARCH\_CRIT|"GCSE"
"L4Tutor"|WC\_URL|"http://www.looking4tutor.com/popup\_case.php?id="
"L4Tutor"|WC\_SEARCH\_COND\_PRICE\_ABOVE|130
"L4Tutor"|WC\_INDEX\_URL|"http://looking4tutor.com/search.php?maxPage=10&cPage=1&infoType=2&infoSex=&education\_1=&district\_1=&subject\_1=&tutor\_id="
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|觀塘
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|銅鑼灣
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|小西灣
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|沙田
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|將軍澳
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|馬鞍山
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|藍田
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|柴灣
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|北角
"L4Tutor"|WC\_SEARCH\_OUT\_CRIT|鴨脷洲
"TutorGroup"|WC\_SEARCH\_CRIT|"物理"
"TutorGroup"|WC\_SEARCH\_CRIT|"數學"
"TutorGroup"|WC\_SEARCH\_CRIT|"生物"
"TutorGroup"|WC\_SEARCH\_CRIT|"化學"
"TutorGroup"|WC\_SEARCH\_CRIT|"科學"
"TutorGroup"|WC\_SEARCH\_CRIT|"Math"
"TutorGroup"|WC\_SEARCH\_CRIT|"math"
"TutorGroup"|WC\_SEARCH\_CRIT|"Phy"
"TutorGroup"|WC\_SEARCH\_CRIT|"phy"
"TutorGroup"|WC\_SEARCH\_CRIT|"Chem"
"TutorGroup"|WC\_SEARCH\_CRIT|"chem"
"TutorGroup"|WC\_SEARCH\_CRIT|"M1"
"TutorGroup"|WC\_SEARCH\_CRIT|"m1"
"TutorGroup"|WC\_SEARCH\_CRIT|"M2"
"TutorGroup"|WC\_SEARCH\_CRIT|"m2"
"TutorGroup"|WC\_SEARCH\_CRIT|"Bio"
"TutorGroup"|WC\_SEARCH\_CRIT|"bio"
"TutorGroup"|WC\_SEARCH\_CRIT|"Science"
"TutorGroup"|WC\_SEARCH\_CRIT|"science"
"TutorGroup"|WC\_SEARCH\_CRIT|"IB"
"TutorGroup"|WC\_SEARCH\_CRIT|"ib"
"TutorGroup"|WC\_SEARCH\_CRIT|"GCSE"
"TutorGroup"|WC\_SEARCH\_CRIT|"GCSE"
"TutorGroup"|WC\_URL|"http://tutorgroup.hk/tutor/index.php"
"TutorGroup"|WC\_URL|"http://tutorgroup.hk/tutor/index.php?page=2"
"TutorGroup"|WC\_SEARCH\_COND\_PRICE\_ABOVE|130
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|觀塘
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|銅鑼灣
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|小西灣
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|沙田
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|將軍澳
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|馬鞍山
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|藍田
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|柴灣
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|北角
"TutorGroup"|WC\_SEARCH\_OUT\_CRIT|鴨脷洲

    **Note** at WC\_SEARCH\_COND\_PRICE\_ABOVE, value -1 is reserved

    Other
    ====
    * Make sure your system date is correct, synced
    * Make sure your mail system is operatin
