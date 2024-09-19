{
    description = "A flake for chisel, verilator, and OpenSTA development";
    inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";

    outputs = { self, nixpkgs }: {
        devShell = {
            aarch64-darwin = with nixpkgs.legacyPackages.aarch64-darwin; mkShellNoCC {
                packages = with pkgs; [
                    # Chisel
                    (let
                        circtpkgs = import (builtins.fetchTree { 
                        type = "github"; 
                        owner = "nixos"; 
                        repo = "nixpkgs"; 
                        rev = "50a7139fbd1acd4a3d4cfa695e694c529dd26f3a"; }) 
                        { inherit (pkgs) system; };
                    in circtpkgs.circt)

                
                    # Scala 
                    sbt
                    scala-cli
                    scalafmt

                    # Verilator
                    verilator
                    ninja
                    cmake

                    # OpenSTA
                    (pkgs.stdenv.mkDerivation {
                        pname = "opensta";
                        version = "2024-09-09";
                        src =  pkgs.fetchFromGitHub {
                            owner = "The-OpenROAD-Project";
                            repo = "OpenSTA";
                            rev = "20925bb00965c1199c45aca0318c2baeb4042c5a";
                            sha256 = "sha256-gWAN+d6ioxQtxtgeq3vR+Zgq3nYRyn/u104L/xqumuY=";
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
                    yosys
                    gtkwave
                    verilog

                    # LaTeX
                    texliveFull

                    firefox
                ];
                shellHook = ''
                    export CXX=/usr/bin/c++
                    export CC=/usr/bin/cc
                '';
            };

            x86_64-linux = with nixpkgs.legacyPackages.x86_64-linux; mkShellNoCC {
                packages = with pkgs; [
                    # Chisel
                    (let
                        circtpkgs = import (builtins.fetchTree { 
                        type = "github"; 
                        owner = "nixos"; 
                        repo = "nixpkgs"; 
                        rev = "50a7139fbd1acd4a3d4cfa695e694c529dd26f3a"; }) 
                        { inherit (pkgs) system; };
                    in circtpkgs.circt)
                                    
                    # Scala 
                    sbt
                    scala-cli

                    # Verilator
                    verilator
                    ninja
                    cmake

                    # OpenSTA
                    (pkgs.stdenv.mkDerivation {
                        pname = "opensta";
                        version = "2024-09-09";
                        src =  pkgs.fetchFromGitHub {
                            owner = "The-OpenROAD-Project";
                            repo = "OpenSTA";
                            rev = "20925bb00965c1199c45aca0318c2baeb4042c5a";
                            sha256 = "sha256-gWAN+d6ioxQtxtgeq3vR+Zgq3nYRyn/u104L/xqumuY=";
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
                    yosys
                    gtkwave
                    verilog

                     # LaTeX
                    texliveFull

                    firefox
                    gcc
                ];
                shellHook = ''
                    export CHISEL_FIRTOOL_PATH="${pkgs.circt}/bin"
                '';
            };
        };
    };
}

