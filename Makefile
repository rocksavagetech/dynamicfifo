# MAKEFLAGS += --silent

# Define SBT variable
SBT = sbt

.PHONY: clean docs update verilog synth sta test

# Default target
default: synth

docs:
	@echo Generating docs
	mkdir -p $(shell pwd)/out/doc
	cd doc/user-guide && pdflatex -shell-escape -output-directory=$(shell pwd)/out/doc timer.tex | tee -a $(shell pwd)/out/doc/doc.rpt

update:
	@echo Updating...
	sbt clean update

clean:
	@echo Cleaning...
	rm -rf generated target *anno.json ./*.rpt doc/*.rpt syn/*.rpt syn.log out test_run_dir target
	rm -rf project/project project/target
	rm -rf generated
	rm -rf ~/.sbt ~/.ivy2
	# filter all files with bad extensions
	find . -type f -name "*.aux" -delete
	find . -type f -name "*.toc" -delete
	find . -type f -name "*.out" -delete
	find . -type f -name "*.log" -delete
	find . -type f -name "*.fdb_latexmk" -delete
	find . -type f -name "*.fls" -delete
	find . -type f -name "*.synctex.gz" -delete
	find . -type f -name "*.pdf" -delete

verilog:
	@echo Generating Verilog...
	@$(SBT) "runMain tech.rocksavage.Main verilog --mode print --module tech.rocksavage.chiselware.timer.Timer --config-class tech.rocksavage.chiselware.timer.TimerConfig"

synth:
	@echo Synthesizing...
	@$(SBT) "runMain tech.rocksavage.Main synth --module tech.rocksavage.chiselware.timer.Timer --techlib synth/stdcells.lib --config-class tech.rocksavage.chiselware.timer.TimerConfig"

sta:
	@echo Running Timing Analysis...
	@$(SBT) "runMain tech.rocksavage.Main sta --module tech.rocksavage.chiselware.timer.Timer --techlib synth/stdcells.lib --config-class tech.rocksavage.chiselware.timer.TimerConfig --clock-period 5.0"

test:
	@echo Running tests...
	@$(SBT) test -DuseVerilator="true"

cov: 
	@$(SBT) coverageOn test coverageReport
