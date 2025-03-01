{
    description = "A flake for chisel, verilator, and OpenSTA development";
    inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.11";

    outputs = { self, nixpkgs }: {
        
        
        devShell = 
            let
                opensta_rev = "20925bb00965c1199c45aca0318c2baeb4042c5a";
                opensta_sha256 = "sha256-gWAN+d6ioxQtxtgeq3vR+Zgq3nYRyn/u104L/xqumuY=";

                env_exports = ''

                '';
            in 
        {
            aarch64-darwin = with nixpkgs.legacyPackages.aarch64-darwin; mkShell {
                packages = with pkgs; [

                    # Scala
                    scala_2_13
                    sbt
                    scala-cli
                    scalafmt
                    scalafix

                    # Chisel
                    circt

                    # Simulation
                    verilog
                    verilator
                    ninja # for verilator
                    cmake # for verilator
                    gtkwave

                    # Synthesis
                    yosys

                    # Formal Verification
                    sby
                    yices
                    z3

                    # LaTeX
                    texliveFull
                    inkscape

                    # Other
                    python3
                    nodePackages_latest.wavedrom-cli


                    # OpenSTA
                    (pkgs.stdenv.mkDerivation {
                        pname = "opensta";
                        version = "2024-09-09";
                        src =  pkgs.fetchFromGitHub {
                            owner = "The-OpenROAD-Project";
                            repo = "OpenSTA";
                            rev = opensta_rev;
                            sha256 = opensta_sha256;
                        };
                        buildInputs = [ 
                            pkgs.cmake 
                            pkgs.gcc 
                            pkgs.tcl 

                            pkgs.bison
                            pkgs.flex
                            pkgs.eigen
                            pkgs.swig4
                            pkgs.cudd
                            pkgs.tclreadline
                            pkgs.zlib
                            pkgs.tcllib
                        ];
                        nativeBuildInputs = [ 
                            pkgs.cmake 
                            pkgs.tcl 
                        ];
                        cmakeFlags = [
                            "-DTCL_LIBRARY=${pkgs.tcl}/lib/libtcl8.6.dylib"
                            "-DCMAKE_INSTALL_PREFIX=${placeholder "out"}"
                        ];
                        installPhase = ''
                            mkdir -p ${placeholder "out"}
                            make install
                        '';
                    })

                ];
                shellHook = env_exports + ''
                    export CHISEL_FIRTOOL_PATH="${pkgs.circt}/bin"
                    export CXX=/usr/bin/c++
                    export CC=/usr/bin/cc
                    if [ -e config.sh ]; then
                        source config.sh
                    fi
                '';
            };

            x86_64-linux = with nixpkgs.legacyPackages.x86_64-linux; mkShellNoCC {
                packages = with pkgs; [

                      # Scala
                      scala_2_13
                      sbt
                      scala-cli
                      scalafmt
                      scalafix

                      # Chisel
                      circt

                      # Simulation
                      verilog
                      verilator
                      ninja # for verilator
                      cmake # for verilator
                      gtkwave

                      # Synthesis
                      yosys

                      # Formal Verification
                      sby
                      yices
                      z3

                      # LaTeX
                      texliveFull
                      inkscape

                      # Other
                      python3
                      nodePackages_latest.wavedrom-cli

                      # OpenSTA
                      (pkgs.stdenv.mkDerivation {
                          pname = "opensta";
                          version = "2024-09-09";
                          src =  pkgs.fetchFromGitHub {
                              owner = "The-OpenROAD-Project";
                              repo = "OpenSTA";
                              rev = opensta_rev;
                              sha256 = opensta_sha256;
                          };
                          buildInputs = [
                              pkgs.cmake
                              pkgs.gcc
                              pkgs.tcl

                              pkgs.bison
                              pkgs.flex
                              pkgs.eigen
                              pkgs.swig4
                              pkgs.cudd
                              pkgs.tclreadline
                              pkgs.zlib
                              pkgs.tcllib
                          ];
                          nativeBuildInputs = [
                              pkgs.cmake
                              pkgs.tcl
                        ];
                        cmakeFlags = [
                            "-DTCL_LIBRARY=${pkgs.tcl}/lib/libtcl8.6.so"
                            "-DCMAKE_INSTALL_PREFIX=${placeholder "out"}"
                        ];
                        installPhase = ''
                            mkdir -p ${placeholder "out"}
                            make install
                        '';
                    })

                ];
                shellHook = env_exports + ''
                    export CHISEL_FIRTOOL_PATH="${pkgs.circt}/bin"
                    if [ -e config.sh ]; then
                        source config.sh
                    fi
                '';
            };
            aarch64-linux = with nixpkgs.legacyPackages.aarch64-linux; mkShellNoCC {
                packages = with pkgs; [

                      # Scala
                      scala_2_13
                      sbt
                      scala-cli
                      scalafmt
                      scalafix

                      # Chisel
                      circt

                      # Simulation
                      verilog
                      verilator
                      ninja # for verilator
                      cmake # for verilator
                      gtkwave

                      # Synthesis
                      yosys

                      # Formal Verification
                      sby
                      yices
                      z3

                      # LaTeX
                      texliveFull
                      inkscape

                      # Other
                      python3
                      nodePackages_latest.wavedrom-cli

                      # OpenSTA
                      (pkgs.stdenv.mkDerivation {
                          pname = "opensta";
                          version = "2024-09-09";
                          src =  pkgs.fetchFromGitHub {
                              owner = "The-OpenROAD-Project";
                              repo = "OpenSTA";
                              rev = opensta_rev;
                              sha256 = opensta_sha256;
                          };
                          buildInputs = [
                              pkgs.cmake
                              pkgs.gcc
                              pkgs.tcl

                              pkgs.bison
                              pkgs.flex
                              pkgs.eigen
                              pkgs.swig4
                              pkgs.cudd
                              pkgs.tclreadline
                              pkgs.zlib
                              pkgs.tcllib
                          ];
                          nativeBuildInputs = [
                              pkgs.cmake
                              pkgs.tcl
                        ];
                        cmakeFlags = [
                            "-DTCL_LIBRARY=${pkgs.tcl}/lib/libtcl8.6.so"
                            "-DCMAKE_INSTALL_PREFIX=${placeholder "out"}"
                        ];
                        installPhase = ''
                            mkdir -p ${placeholder "out"}
                            make install
                        '';
                    })

                ];
                shellHook = env_exports + ''
                    export CHISEL_FIRTOOL_PATH="${pkgs.circt}/bin"
                    if [ -e config.sh ]; then
                        source config.sh
                    fi
                '';
            };
        };
    };
}

