#!/bin/bash
text="@prefix  : <http://aksw.org/> ."


FILES="/Users/ricardousbeck/Dropbox/Dissertation/git-workspace/DALI/java/resources/endpoint/*"
for f in $FILES
do	
    exec 3<> $f && awk -v TEXT="$text" 'BEGIN {print TEXT}{print}' $f >&3
done
 

