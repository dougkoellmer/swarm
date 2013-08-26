#!/bin/bash

# cygrename : A Program to overcome limitation of cygwin's "rename" function.
# Author: Adam Teller
#
# Cygwin's "rename" doesn't work same way as *Nix "rename"
# because it cannot use wildcards. So, what can we do?
## Get the basename of the file if it matches search pattern.
## If 4th argument is set, change file to that suffix.
## else, match pattern within file name and keep same suffix
#
## Arguments: [search pattern] [replace pattern] [in suffix] [to new suffix]


#rename s/bh(.*)\.java/sm$1.java/g *.java

LIST1=$(find . -type f -name "*${1}*\.${3}")
if [ -z "${LIST1}" ];
 then
   printf "Did not find any file matches, program will exit\n"
 exit 1
fi

for found in $LIST1;
 do
	
   MATCHEDFILE=$(basename $found);
   DIRECTORY="$(dirname $found)/";
   NEWFILENAME=$(echo $MATCHEDFILE | sed -e "s/${1}/${2}/");
   NEWFILENAMENOEXTENSION=$(echo $NEWFILENAME | sed -r "s/\.${3}//");
   
   echo "HERE1: $DIRECTORY"
	echo "HERE2: $NEWFILENAME"

   ## TEST if $4 has been set, it means want a new file suffix
   if [ -z "${4}" ];
     then
       printf "$MATCHEDFILE :to be renamed as: $NEWFILENAME for files of type ${3}\n";
       mv $found "${DIRECTORY}$NEWFILENAME";
     else
       printf "${NEWFILENAMENOEXTENSION}.${3} will get $4 as its suffix, ";
       printf "and be renamed to ${NEWFILENAMENOEXTENSION}.${4} \n";
       mv $found "${DIRECTORY}${NEWFILENAMENOEXTENSION}.${4}"
  fi
 done

 exit 0