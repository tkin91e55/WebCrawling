#!/bin/bash
#let format be TutorNotification_{month}_{day}_{time}
TEMP_FILE="${WORKING_PATH}tmpFile"

RECIPIENT=""
RECIPIENT2=""

#java things
JAVA_FILE="CrawlTutorGroup"

#functions
function DoGreping() {

	echo "Starting grep"
	UPDATE="FALSE"
	#Looping
	for i in {0..29}; do
		HEADING="span[id$=cs$i]"
		CONTENT="div[id$=cdiv$i]"
		VAR=$(java -cp .:./jsoup-1.8.2.jar ${JAVA_FILE} ${WEBSITE_URL} ${HEADING} ${CONTENT})
		#echo "Grep result: "$VAR >> TutorLogger
		VAR=$(echo "$VAR" | sed -e s/自我介紹:\ //g)
		VAR=$(echo "$VAR" | sed -e s/時間:\ //g)
		VAR=$(echo "$VAR" | sed -e s/我同意所有有關導師條款//g)
			#echo $VAR >> TutorLogger
			echo "$VAR"
			if [ "${VAR/$SEARCH_CRIT}" != "$VAR" ] || [ "${VAR/$SEARCH_CRIT2}" != "$VAR" ] ; then
				#have the keyword
				#TEMP=$(echo $VAR | cut -d' ' -f1-2)
				TEMP="$(echo $VAR | cut -d' ' -f2) $(echo $VAR | cut -d' ' -f1)"
				echo "TEMP: "$TEMP >> TutorLogger
				if [ "$TODAY" = "$TEMP" ] ; then
					#echo "Is TODAY's DATE"
					UPDATE="TRUE"
					echo $VAR >> "$TEMP_FILE"
				#else
					#echo "Not TODAY's date"
				fi
			#else
				#doesn't Have the keyword
				#echo "to be filtered out"
			fi
	done

	#as Test case
	#HEADING="span[id$=cs3]"
	#CONTENT="div[id$=cdiv3]"
	#java -cp .:./jsoup-1.8.2.jar JsoupTest ${WEBSITE_URL} ${HEADING} ${CONTENT}
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
	LAST_FILE=$(ls -lt History/TutorNotification_*_*_* | head -1 | rev | cut -d' ' -f1 | rev)
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
	NEW_FILE="${WORKING_PATH}History/TutorNotification_${F_MONTH}_${F_DAY}_${F_TIME}"
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
	sed -i '1s/^/Dear Pang,\n/' $1
	echo "" >> $1
	echo "Regards," >> $1
	echo "Kwun" >> $1
	F_TIME=$(date | cut -d' ' -f4)
	cat $1 | mail -s "TutorNotification: $TODAY, $F_TIME" "$RECIPIENT"
	#cat $1 | mail -s "TutorNotification: $TODAY, $TIME" "$RECIPIENT2"
	rm $1
	mv "$1_backUp" $1
}

#=======================================================================================main part============================================================================
pushd ${WORKING_PATH}

echo "======================Debug Log Start :" >> TutorLogger
date >> TutorLogger

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

if DoGreping ; then 
	echo "DoGreping success" >> TutorLogger
	if DoDiffing ; then
		echo "DoDiffing sucess" >> TutorLogger
	else
		echo "DoDiffing no result" >> TutorLogger
	fi
else
	echo "DoGreping no result" >> TutorLogger
fi

popd
