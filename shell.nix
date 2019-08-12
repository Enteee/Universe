with import <nixpkgs> {};
let
  mkStudio = opts: callPackage (import ./android-studio.nix opts) {
    fontsConf = makeFontsConf {
      fontDirectories = [];
    };
    inherit (gnome2) GConf gnome_vfs;
  };
  stableVersion = {
    version = "3.4.2.0"; # "Android Studio 3.4.2"
    build = "183.5692245";
    sha256Hash = "090rc307mfm0yw4h592l9307lq4aas8zq0ci49csn6kxhds8rsrm";
  };

in pkgs.mkShell {
  # Attributes are named by their corresponding release channels
  buildInputs = [
    (
      mkStudio (stableVersion // {
        channel = "stable";
        pname = "android-studio";
      })
    )
  ];

}
