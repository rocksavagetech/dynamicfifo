MAKEFLAGS += --silent
	
SBT = sbt
	
# Run everything and scan for errors
all: clean publish docs test yosys
	@echo Checking for errors
	grep error */*.rpt */*/*.rpt */*/*.log | tee error.rpt
	grep Error */*.rpt */*/*.rpt */*/*.log | tee -a error.rpt
	grep fail */*.rpt */*/*.rpt */*/*.log | grep -v "failed 0" | tee -a error.rpt

# Start with a fresh directory
clean: 
	@echo Cleaning
	rm -rf generated project target *anno.json ./*.rpt doc/*.rpt syn/*.rpt

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
	$(SBT) "runMain tech.rocksavage.chiselware.DynamicFifo.GenVerilog" | tee generated/verilog.rpt
	rm *.anno.json    

# Run the tests
test:
	@echo Running tests
	mkdir -p generated
	$(SBT) "test" | tee generated/test.rpt

# Run synthesis on generated Verilog; generate timing and area reports
yosys:
	make verilog    
	cd syn && ./run.sh