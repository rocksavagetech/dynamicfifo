MAKEFLAGS += --silent
	
SBT = sbt
	
# Run everything and scan for errors
list:
	@grep '^[^#[:space:]].*:' Makefile

all: clean publish docs cov yosys check

check: 
	@echo 
	@echo Checking for errors
	grep error */*.rpt */*/*/*.rpt */*/*/*.log | tee error.rpt
	grep Error */*.rpt */*/*/*.rpt */*/*/*.log | tee -a error.rpt
	grep fail */*.rpt */*/*/*.rpt */*/*/*.log | grep -v "failed 0" | tee -a error.rpt
	@echo; 
	if [ ! -s error.rpt ]; \
	then echo "\e[1;32mALL TESTS PASSED WITH NO ERRORS \e[0m"; \
	else echo "\e[1;31mTESTS COMPLETED WITH ERRORS \e[0m"; \
	fi; 
	@echo

# Start with a fresh directory
clean: 
	@echo Cleaning
	rm -rf generated target *anno.json ./*.rpt doc/*.rpt syn/*.rpt syn.log
	rm -rf project/build.properties project/project project/target

# Publish the documentation (locally)
publish: 
	@echo Publishing local
	rm -rf /home/tws/.ivy2/local/tech.rocksavage/dynamicfifo_2.13
	$(SBT) "publishLocal" | tee doc/publish.rpt

# Generate the documentation
docs:
	@echo Generating docs
	$(SBT) "doc" | tee doc/doc.rpt
#	google-chrome --new-window ./target/scala-2.13/api/index.html & 
	cd doc/user-guide && pdflatex DynamicFifo.tex | tee -a ../doc.rpt
# Rerun to generate TOC
	cd doc/user-guide && pdflatex DynamicFifo.tex | tee -a ../doc.rpt
# Clean up temp files
	cd doc/user-guide && rm *.aux *.toc *.out *.log
#	google-chrome --new-window doc/user-guide/DynamicFifo.pdf & 

# Generate Verilog and synthesize
verilog:
	@echo Generate Verilog for synthesis
	mkdir -p generated
	$(SBT) "runMain tech.rocksavage.chiselware.dynamicfifo.GenVerilog" | tee generated/verilog.rpt
	rm *.anno.json    

# Run the tests
test:
	@echo Running tests
	mkdir -p generated
	$(SBT) "test" | tee generated/test.rpt

# Run the tests with Scala code coverage enables
cov:
	@echo Running tests with coverage enabled
	mkdir -p generated
	mkdir -p generated/scalaCoverage
	$(SBT) clean \
	coverageOn \
	test \
	"runMain tech.rocksavage.chiselware.dynamicfifo.GenVerilog" \
	"runMain tech.rocksavage.chiselware.dynamicfifo.Main" \
	coverageReport | tee generated/test.rpt
	google-chrome --new-window generated/scalaCoverage/scoverage-report/index.html &

# Run synthesis on generated Verilog; generate timing and area reports
yosys:
	make verilog    
	cd syn && ./run.sh
