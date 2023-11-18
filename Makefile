MAKEFLAGS += --silent
	
SBT = sbt
	
# Run everything
all: clean publish docs verilog test yosys

# Start with a fresh directory
clean: 
	@echo Cleaning
	rm -rf generated project target

# Publish the documentation (locally)
publish: 
	@echo Publishing local
	rm -rf /home/tws/.ivy2/local/tech.rocksavage/dynamicfifo_2.13
	$(SBT) "publishLocal"

# View the published docs
docs:
	@echo Generating docs
	$(SBT) "doc"
	google-chrome --new-window ./target/scala-2.13/api/index.html & 

# Generate Verilog and synthesize
verilog:
	@echo Generate Verilog for synthesis
	$(SBT) "runMain tech.rocksavage.chiselware.DynamicFifo.GenVerilog"

# Run the tests
test:
	@echo Running tests
	$(SBT) "test"

# Run synthesis on generated Verilog
yosys:
	rm -rf generated/small* generated/medium* generated/large*
	$(SBT) "runMain tech.rocksavage.chiselware.DynamicFifo.GenVerilog"
	cd syn && ./run.sh