{pkgs}: {
  deps = [
    pkgs.openjdk11
    pkgs.maven
    pkgs.postgresql
    pkgs.openssl
  ];
}
