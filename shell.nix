with import <nixpkgs> {};

let

in stdenv.mkDerivation {
  name = "universe";

  src = null;

  buildInputs = [
    androidStudioPackages.stable
  ];

  shellHook = ''
  '';

  exitHook = ''
  '';

}
