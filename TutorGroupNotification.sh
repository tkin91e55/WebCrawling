#!/bin/bash
#let format be TutorNotification_{month}_{day}_{time}
WORKING_PATH=$(tail -n+2 config.csv | awk-csv-parser --output-separator=',' | grep WC_WORKPATH | awk -F"," '{print $2}')
TEMP_FILE="${WORKING_PATH}tmpFile"
RECIPIENTS=($(tail -n+2 config.csv | awk-csv-parser --output-separator=',' | grep WC_RECIPIENT | awk -F"," '{print $2}'))

echo "[Testing] , working path is: ${WORKING_PATH}"
echo "[Testing] , recipients are: ${RECIPIENTS[*]}"

#java things
JAVA_FILE="CrawlTutorGroup"
HISTORY="History"
IMPORT_FILE="result.csv"

#functions
function DoImporting() {

	echo "Starting Import"
		UPDATE="FALSE"
		if [ -f "${WORKING_PATH}${IMPORT_FILE}" ] ; then
			echo "${IMPORT_FILE} exists" >> TutorLogger
				LINE_NUM=$(wc --lines ${IMPORT_FILE} | cut -d' ' -f1)
				if [ "${LINE_NUM}" -gt 1 ] ; then
					UPDATE="TRUE"
						echo "DoImporting true" >> TutorLogger
						cp ${IMPORT_FILE} ${TEMP_FILE}
	fi
				else
					echo "${IMPORT_FILE} not exist"
						fi

						if [ ${UPDATE} = "TRUE" ] ; then
							return 0
						else
							return 1
								fi
}

function DoDiffing() {
#diff the previous greped result
	SEND="FALSE"

#get the last file name
		LAST_FILE=$(ls -lt ${HISTORY}/TutorNotification_*_*_* | head -1 | rev | cut -d' ' -f1 | rev)
		echo "The last file is: $LAST_FILE" >> TutorLogger

#if diff not blank, output the newest file and set SEND 'TRUE'
		DIFF_RESULT=""

		if [ "$LAST_FILE" != "" ] ; then
			DIFF_RESULT=$(diff $TEMP_FILE $LAST_FILE)
				echo $DIFF_RESULT >> TutorLogger
				fi

				if [ "$DIFF_RESULT" != "" ] || [ "$LAST_FILE" = "" ] ; then
					SEND="TRUE"
#rename the tmpFile
						F_MONTH=$(date -R | cut -d' ' -f3)
						F_DAY=$(date -R | cut -d' ' -f2)
						F_TIME=$(date -R | cut -d' ' -f5)
						NEW_FILE="${WORKING_PATH}${HISTORY}/TutorNotification_${F_MONTH}_${F_DAY}_${F_TIME}"
						mv $TEMP_FILE $NEW_FILE
						TEMP_FILE="$NEW_FILE"
						echo "TEMP_FILE new name: "$TEMP_FILE
						fi

						if [ ${SEND} = "TRUE" ] ; then
							SendMail $NEW_FILE
								return 0
						else
#SendMail $TEMP_FILE #debug use only to check mailing and crontab
							return 1
								fi
}

function SendMail() { #input arg 1 is the file path

	cp $1 "$1_backUp"
		sed -i '1s/^/\n/' $1
		sed -i '1s/^/Here is the update:\n/' $1
		sed -i '1s/^/\n/' $1
		sed -i '1s/^/Dear recipients,\n/' $1
		echo "" >> $1
		echo "Regards," >> $1
		echo "$(whoami)" >> $1
		F_TIME=$(date | cut -d' ' -f4)
		
		for recipient in "${RECIPIENTS[@]}"
			do
				echo "[Looping] recipient is : ${recipient}"
					cat $1 | mail -s "TutorNotification: $TODAY, $F_TIME" "${recipient}"
					done
					rm $1
					mv "$1_backUp" $1
}

#=======================================================================================main part============================================================================
pushd ${WORKING_PATH}

echo "======================Debug Log Start :" >> TutorLogger
date >> TutorLogger

if [ ! -f "${WORKINGPATH}${JAVA_FILE}.java" ] ; then
echo "CANNOT FIND THE THE JAVA FILE, CHECK WHETHER YOU HAS BUILT IT!!!" >> TutorLogger
exit 1
fi
if [ ! -d "${WORKINGPATH}${HISTORY}" ] ; then
echo "History folder not exists, going to create it..." >> TutorLogger
mkdir ${HISTORY}
fi

#confirm today date
#TODAY=$(date | cut -d' ' -f2-3)
#TODAY=$(date -R --date='-1 day'| cut -d' ' -f2-3 )
TODAY=$(date -R | cut -d' ' -f2-3 )
echo "TODAY is "${TODAY} >> TutorLogger

echo "TEMP_FILE is "$TEMP_FILE >> TutorLogger

#clear temp file
if [[ -e "$TEMP_FILE" ]] ; then
echo "Deleting $TEMP_FILE ..."
rm "$TEMP_FILE"
fi

if DoImporting ; then 
echo "DoImporting success" >> TutorLogger
if DoDiffing ; then
echo "DoDiffing sucess" >> TutorLogger
else
echo "DoDiffing no result" >> TutorLogger
fi
else
echo "DoImporting no result" >> TutorLogger
fi

popd
