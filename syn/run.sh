#!/bin/bash

nand2Area=0.798 # Nangate 45nm
designName="DynamicFifo"

# Loop through all the test cases
declare -a arr=(\
  "small_false_8_8" \
  "medium_false_32_64" \
  "large_false_64_256" \
  "small_true_64_256" \
  "medium_true_128_128" \
  "large_true_256_2048"\
  )

# Synthesize each of the test cases
for testCase in "${arr[@]}"
do
  cd ../generated/synTestCases/$testCase
  echo "*** Synthesizing test case:  " $testCase
  echo "" 
  rm -f *.log *.rpt ../../../syn/*.rpt
  yosys -Qv 1 -l yosys.log ../../../syn/$designName.yo.tcl  
  echo "" 
  cd ../../../syn
done
echo ""

# Extract area data from log 
echo "$designName gate count report" 
echo "---------------------------------------------------"
for testCase in "${arr[@]}"
do
  # Extract area
  file=../generated/synTestCases/$testCase/yosys.log
  areaLine=$(grep "Chip area" $file)
  floatArea=$(echo $areaLine| cut -d':' -f 2)
  intArea=$(echo ${floatArea%.*})
  gates=$(echo "$intArea/$nand2Area" | bc)
  echo -e "$testCase = \t $gates gates" >> ./area.rpt
  echo -e "$testCase = \t $gates gates" 
done
echo ""

# Run STA on netlists
for testCase in "${arr[@]}"
do
  cd ../generated/synTestCases/$testCase
#  echo "*** Running STA on " $testCase
  sta -no_init -no_splash -exit ../../../syn/$designName.sta.tcl | tee ./timing.rpt
  timing=`grep slack ./timing.rpt`
  echo -e "$testCase = \t $timing" >> ../../../syn/timing.rpt 
  cd ../../../syn
done

echo ""
cat area.rpt
echo ""
cat timing.rpt